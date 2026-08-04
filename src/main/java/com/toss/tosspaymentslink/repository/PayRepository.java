package com.toss.tosspaymentslink.repository;

import com.toss.tosspaymentslink.domain.entity.Payment;
import com.toss.tosspaymentslink.dto.PaymentResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PayRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentKey(String paymentKey);
}
