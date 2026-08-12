package com.toss.tosspaymentslink.repository;

import com.toss.tosspaymentslink.domain.entity.Payment;
import com.toss.tosspaymentslink.domain.enums.PaymentStatus;
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

    //관리자 소켓통신용
    //금일 최근 주문건 10조회
    List<Payment> findTop10ByApprovedAtGreaterThanEqualOrderByApprovedAtDesc(@Param("todayStart") OffsetDateTime todayStart);
    //금일 최근 취소건 10조회
    @Query("""
        SELECT DISTINCT p
                FROM Payment p
                JOIN p.cancels c
                WHERE c.canceledAt >= :todayStart
    """)
    List<Payment> findTodayTimelineTop10(@Param("todayStart") OffsetDateTime todayStart);
    //관리자 소켓통신용 금일 결제 완료 건수
    long countByStatusAndApprovedAtGreaterThanEqual(PaymentStatus status, OffsetDateTime todayStart);
    //관리자 소켓통신용 금일 결제 취소 건수
    long countDistinctByCancels_CanceledAtGreaterThanEqual(OffsetDateTime todayStart);
}
