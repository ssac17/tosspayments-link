package com.toss.tosspaymentslink.contorller;

import com.toss.tosspaymentslink.domain.entity.Payment;
import com.toss.tosspaymentslink.domain.enums.PaymentStatus;
import com.toss.tosspaymentslink.dto.PaymentResponseDto;

import java.util.List;

public record AdminTodaySummaryDto(
        Integer totalAmount,
        Long paymentCount,
        Long cancelCount,
        List<PaymentResponseDto> recentPayments
) {
    public static AdminTodaySummaryDto from(List<Payment> payments) {
        int sumAmount = payments.stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.DONE)
                .mapToInt(payment -> payment.getTotalAmount()).sum();

        long paymentCount = payments.stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.DONE)
                .count();

        long cancelCount = payments.stream()
                .filter(payment -> payment.getStatus() == PaymentStatus.CANCELED)
                .count();

        List<PaymentResponseDto> recentPayments = payments.stream()
                .map(PaymentResponseDto::from)
                .toList();

        return new AdminTodaySummaryDto(
             sumAmount,
             paymentCount,
             cancelCount,
             recentPayments
        );
    }
}
