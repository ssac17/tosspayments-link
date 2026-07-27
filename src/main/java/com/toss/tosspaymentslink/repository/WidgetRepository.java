package com.toss.tosspaymentslink.repository;

import com.toss.tosspaymentslink.dto.PaymentResponseDto;
import com.toss.tosspaymentslink.entity.Payment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WidgetRepository extends JpaRepository<Payment, Long> {
}
