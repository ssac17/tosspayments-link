package com.toss.tosspaymentslink.contorller;

import com.toss.tosspaymentslink.domain.entity.Payment;
import com.toss.tosspaymentslink.dto.PaymentResponseDto;

import java.util.List;

public record AdminTodaySummaryDto(
        Integer totalAmount,
        Long paymentCount,
        Long cancelCount,
        List<PaymentResponseDto> recentPayments
) {
    public static AdminTodaySummaryDto from(
            int totalAmount,
            long doneCount,
            long cancelCount,
            List<Payment> timelinePayments
    ) {
        List<PaymentResponseDto> recentPayments = timelinePayments.stream()
                .map(PaymentResponseDto::from)
                .toList();

        return new AdminTodaySummaryDto(
                totalAmount,
                doneCount,
                cancelCount,
                recentPayments
        );
    }
}
