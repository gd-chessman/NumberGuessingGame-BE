package org.example.numberguessinggame.vnpay;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** VNPay signing (HMAC SHA512) as per integration guide. */
public final class VnpayCrypto {

    private VnpayCrypto() {}

    public static String hmacSha512Hex(String secretKey, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec =
                    new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(secretKeySpec);
            byte[] result = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("HMAC SHA512 failed", e);
        }
    }

    /**
     * Data string for HMAC: sorted keys, skip empty, {@code key=encodedValue&} — values URL-encoded
     * (same as VNPAY Java demo: {@code fieldName + '=' + URLEncoder.encode(fieldValue)}).
     */
    public static String buildSignData(Map<String, String> params) {
        TreeMap<String, String> sorted = new TreeMap<>(params);
        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> e : sorted.entrySet()) {
            String k = e.getKey();
            String v = e.getValue();
            if (skipSignField(k) || v == null || v.isEmpty()) {
                continue;
            }
            if (hashData.length() > 0) {
                hashData.append('&');
            }
            hashData.append(k).append('=').append(encodeValueForVnpay(v));
        }
        return hashData.toString();
    }

    /** URL-encode parameter values for signing and query (UTF-8, {@code +} for space like PHP urlencode). */
    public static String encodeValueForVnpay(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public static boolean skipSignField(String key) {
        return "vnp_SecureHash".equalsIgnoreCase(key) || "vnp_SecureHashType".equalsIgnoreCase(key);
    }

    /** Parse query params from servlet request into map (first value wins). */
    public static Map<String, String> toSortedMap(Map<String, String[]> parameterMap) {
        TreeMap<String, String> sorted = new TreeMap<>();
        for (Map.Entry<String, String[]> e : parameterMap.entrySet()) {
            String k = e.getKey();
            if (e.getValue() != null && e.getValue().length > 0 && e.getValue()[0] != null) {
                sorted.put(k, e.getValue()[0]);
            }
        }
        return sorted;
    }

    public static String buildQueryString(Map<String, String> params) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        StringBuilder sb = new StringBuilder();
        for (String k : keys) {
            String v = params.get(k);
            if (v == null || v.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(k).append('=').append(encodeValueForVnpay(v));
        }
        return sb.toString();
    }
}
