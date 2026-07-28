package com.toss.tosspaymentslink.dto;

public record PaymentConfirmRequestDto(
        String paymentKey,
        String orderId,
        Long amount,
        Long productId,
        String name
) {}
