package org.example.numberguessinggame.services;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import org.example.numberguessinggame.configs.VnpayProperties;
import org.example.numberguessinggame.dtos.CreateVnpayPaymentResponse;
import org.example.numberguessinggame.entities.User;
import org.example.numberguessinggame.entities.VnpayPayment;
import org.example.numberguessinggame.entities.VnpayPaymentStatus;
import org.example.numberguessinggame.repositories.UserRepository;
import org.example.numberguessinggame.repositories.VnpayPaymentRepository;
import org.example.numberguessinggame.vnpay.VnpayCrypto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VnpayService {

    private static final DateTimeFormatter CREATE_DATE_FMT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));

    private final VnpayProperties props;
    private final VnpayPaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final GameService gameService;

    public VnpayService(
            VnpayProperties props,
            VnpayPaymentRepository paymentRepository,
            UserRepository userRepository,
            GameService gameService) {
        this.props = props;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.gameService = gameService;
    }

    @Transactional
    public CreateVnpayPaymentResponse createPayment(User user, HttpServletRequest request) {
        if (props.getTmnCode() == null
                || props.getTmnCode().isBlank()
                || props.getHashSecret() == null
                || props.getHashSecret().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "VNPay is not configured (tmnCode / hashSecret).");
        }

        long amount = props.getPackAmountVnd();
        int turns = props.getPackTurns();
        if (amount <= 0 || turns <= 0) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid VNPay pack config.");
        }

        String txnRef = "NGG" + user.getId() + "_" + UUID.randomUUID().toString().replace("-", "");
        if (txnRef.length() > 100) {
            txnRef = txnRef.substring(0, 100);
        }

        VnpayPayment order = new VnpayPayment();
        order.setTxnRef(txnRef);
        order.setUser(
                userRepository.findById(user.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
        order.setAmountVnd(amount);
        order.setTurnsToGrant(turns);
        order.setStatus(VnpayPaymentStatus.PENDING);
        paymentRepository.save(order);

        long amountMinor = amount * 100L;
        String createDate = CREATE_DATE_FMT.format(ZonedDateTime.now());
        String ip = clientIp(request);

        Map<String, String> vnp = new TreeMap<>();
        vnp.put("vnp_Version", props.getVersion());
        vnp.put("vnp_Command", props.getCommand());
        vnp.put("vnp_TmnCode", props.getTmnCode());
        vnp.put("vnp_Amount", String.valueOf(amountMinor));
        vnp.put("vnp_CurrCode", "VND");
        vnp.put("vnp_TxnRef", txnRef);
        // Alphanumeric / no special chars per VNPAY field rules (avoids encoding edge cases)
        vnp.put("vnp_OrderInfo", "Buy " + turns + " turns Number Guessing Game");
        vnp.put("vnp_OrderType", props.getOrderType());
        vnp.put("vnp_Locale", props.getLocale());
        vnp.put("vnp_ReturnUrl", props.getBackendPublicBaseUrl() + "/api/v1/payment/vnpay/return");
        vnp.put("vnp_IpAddr", ip);
        vnp.put("vnp_CreateDate", createDate);

        String signData = VnpayCrypto.buildSignData(vnp);
        String secureHash = VnpayCrypto.hmacSha512Hex(props.getHashSecret(), signData);
        vnp.put("vnp_SecureHash", secureHash);

        String query = VnpayCrypto.buildQueryString(vnp);
        String paymentUrl = props.getPaymentUrl() + "?" + query;

        return new CreateVnpayPaymentResponse(paymentUrl, txnRef, amount, turns);
    }

    /** Browser return: verify, fulfill, redirect to frontend. */
    @Transactional
    public URI handleReturn(HttpServletRequest request) {
        Map<String, String> params = flattenParams(request);
        String redirectBase = props.getFrontendBaseUrl() + "/payment/result";
        try {
            boolean paidOk = verifyAndFulfill(params);
            return URI.create(redirectBase + "?status=" + (paidOk ? "success" : "fail"));
        } catch (Exception ex) {
            return URI.create(redirectBase + "?status=fail");
        }
    }

    /** VNPay IPN: verify, fulfill, JSON ack. */
    @Transactional
    public String handleIpn(HttpServletRequest request) {
        Map<String, String> params = flattenParams(request);
        try {
            verifyAndFulfill(params);
            return "{\"RspCode\":\"00\",\"Message\":\"Confirm Success\"}";
        } catch (Exception ex) {
            return "{\"RspCode\":\"97\",\"Message\":\"Confirm Failed\"}";
        }
    }

    /**
     * Verifies signature, updates payment. Returns {@code true} only when the transaction
     * succeeded (or was already success); {@code false} for cancel/fail/mismatch — still
     * does not throw so IPN can ack with RspCode 00 after processing.
     */
    private boolean verifyAndFulfill(Map<String, String> params) {
        verifySignature(params);

        String txnRef = params.get("vnp_TxnRef");
        if (txnRef == null || txnRef.isBlank()) {
            throw new IllegalArgumentException("Missing vnp_TxnRef");
        }

        VnpayPayment payment =
                paymentRepository.findByTxnRef(txnRef).orElseThrow(() -> new IllegalArgumentException("Unknown txn"));

        if (payment.getStatus() == VnpayPaymentStatus.SUCCESS) {
            return true;
        }

        String responseCode = params.get("vnp_ResponseCode");
        String transStatus = params.get("vnp_TransactionStatus");
        String amountStr = params.get("vnp_Amount");

        if (amountStr != null) {
            long paidMinor = Long.parseLong(amountStr);
            if (paidMinor != payment.getAmountVnd() * 100L) {
                payment.setStatus(VnpayPaymentStatus.FAILED);
                payment.setResponseCode("AMOUNT_MISMATCH");
                return false;
            }
        }

        if ("00".equals(responseCode) && "00".equals(transStatus)) {
            User user = userRepository
                    .findById(payment.getUser().getId())
                    .orElseThrow(() -> new IllegalArgumentException("User missing"));
            gameService.grantTurnsFromVnpay(user, payment.getTurnsToGrant());
            payment.setStatus(VnpayPaymentStatus.SUCCESS);
            payment.setResponseCode(responseCode);
            payment.setTransactionNo(params.get("vnp_TransactionNo"));
            payment.setBankCode(params.get("vnp_BankCode"));
            return true;
        }
        payment.setStatus(VnpayPaymentStatus.FAILED);
        payment.setResponseCode(responseCode != null ? responseCode : "NA");
        return false;
    }

    private void verifySignature(Map<String, String> params) {
        String received = params.get("vnp_SecureHash");
        if (received == null || received.isBlank()) {
            throw new IllegalArgumentException("Missing vnp_SecureHash");
        }
        Map<String, String> signParams = new TreeMap<>(params);
        String signData = VnpayCrypto.buildSignData(signParams);
        String expected = VnpayCrypto.hmacSha512Hex(props.getHashSecret(), signData);
        if (!expected.equalsIgnoreCase(received)) {
            throw new IllegalArgumentException("Invalid signature");
        }
    }

    private static Map<String, String> flattenParams(HttpServletRequest request) {
        Map<String, String[]> raw = request.getParameterMap();
        Map<String, String> m = new HashMap<>();
        for (Map.Entry<String, String[]> e : raw.entrySet()) {
            if (e.getValue() != null && e.getValue().length > 0) {
                m.put(e.getKey(), e.getValue()[0]);
            }
        }
        return m;
    }

    private static String clientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        String ip = request.getRemoteAddr();
        if (ip == null || ip.isBlank()) {
            return "127.0.0.1";
        }
        if (ip.length() > 45) {
            return ip.substring(0, 45);
        }
        return ip;
    }
}
