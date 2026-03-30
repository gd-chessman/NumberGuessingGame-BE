package org.example.numberguessinggame.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "vnpay")
public class VnpayProperties {

    /** Merchant terminal code (TMN). */
    private String tmnCode = "";

    /** Secret key for HMAC SHA512. */
    private String hashSecret = "";

    /** VNPay payment gateway URL (sandbox or production). */
    private String paymentUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";

    /** Public base URL of this API (no trailing slash), used for vnp_ReturnUrl and IPN. */
    private String backendPublicBaseUrl = "http://localhost:8080";

    /** Frontend base URL for redirect after payment (no trailing slash). */
    private String frontendBaseUrl = "http://localhost:3000";

    /** Price per pack in VND. */
    private long packAmountVnd = 10_000L;

    /** Turns granted per successful payment. */
    private int packTurns = 5;

    private String locale = "vn";
    private String version = "2.1.0";
    private String command = "pay";
    private String orderType = "other";

    public String getTmnCode() {
        return tmnCode;
    }

    public void setTmnCode(String tmnCode) {
        this.tmnCode = tmnCode;
    }

    public String getHashSecret() {
        return hashSecret;
    }

    public void setHashSecret(String hashSecret) {
        this.hashSecret = hashSecret;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

    public String getBackendPublicBaseUrl() {
        return backendPublicBaseUrl;
    }

    public void setBackendPublicBaseUrl(String backendPublicBaseUrl) {
        this.backendPublicBaseUrl = backendPublicBaseUrl;
    }

    public String getFrontendBaseUrl() {
        return frontendBaseUrl;
    }

    public void setFrontendBaseUrl(String frontendBaseUrl) {
        this.frontendBaseUrl = frontendBaseUrl;
    }

    public long getPackAmountVnd() {
        return packAmountVnd;
    }

    public void setPackAmountVnd(long packAmountVnd) {
        this.packAmountVnd = packAmountVnd;
    }

    public int getPackTurns() {
        return packTurns;
    }

    public void setPackTurns(int packTurns) {
        this.packTurns = packTurns;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }
}
