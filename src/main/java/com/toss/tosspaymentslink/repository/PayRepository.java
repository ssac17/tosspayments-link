package com.toss.tosspaymentslink.repository;

import com.toss.tosspaymentslink.domain.entity.Payment;
import com.toss.tosspaymentslink.dto.PaymentResponseDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface PayRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findPaymentByPaymentKey(String paymentKey);
    Optional<Payment> findPaymentByOrderId(String orderId);

    //todo: paycount, cancelcount 다시 해야함
    List<Payment> findTop10ByApprovedAtGreaterThanEqualOrderByApprovedAtDesc(@Param("todayStart") OffsetDateTime todayStart);

}
