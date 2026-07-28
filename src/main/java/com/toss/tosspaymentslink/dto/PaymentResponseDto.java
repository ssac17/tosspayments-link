package com.toss.tosspaymentslink.dto;

import com.toss.tosspaymentslink.entity.Payment;
import com.toss.tosspaymentslink.entity.PaymentStatus;
import lombok.Getter;
import lombok.ToString;

import java.time.OffsetDateTime;

@Getter
@ToString
public class PaymentResponseDto {
    private final String orderId;
    private final String paymentKey;
    private final int totalAmount;
    private final String method;
    private final PaymentStatus status;
    private final String orderName;
    private final OffsetDateTime approvedAt;
    private final String receiptUrl;
    private final String cardInfo;

    private PaymentResponseDto(String orderId, String paymentKey, int totalAmount, String method, PaymentStatus status,
                              String orderName, OffsetDateTime approvedAt, String receiptUrl, String cardInfo) {
        this.orderId = orderId;
        this.paymentKey = paymentKey;
        this.totalAmount = totalAmount;
        this.method = method;
        this.status = status;
        this.orderName = orderName;
        this.approvedAt = approvedAt;
        this.receiptUrl = receiptUrl;
        this.cardInfo = cardInfo;
    }

    public static PaymentResponseDto from(Payment payment) {
        return new PaymentResponseDto(
                payment.getOrderId(),
                payment.getPaymentKey(),
                payment.getTotalAmount(),
                payment.getMethod(),
                payment.getStatus(),
                payment.getOrderName(),
                payment.getApprovedAt(),
                payment.getReceiptUrl(),
                payment.getCardInfo()
        );
    }
}
