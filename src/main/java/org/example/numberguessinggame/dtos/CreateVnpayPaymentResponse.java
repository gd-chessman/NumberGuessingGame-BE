package org.example.numberguessinggame.dtos;

public record CreateVnpayPaymentResponse(String paymentUrl, String txnRef, long amountVnd, int turns) {}
