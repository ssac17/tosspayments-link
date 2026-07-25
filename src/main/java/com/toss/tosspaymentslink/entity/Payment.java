package com.toss.tosspaymentslink.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Setter @Getter @Builder @ToString
@NoArgsConstructor @AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderId; //주문 번호
    private String paymentKey; //결제 고유키
    private int totalAmount; //총 결제금액
    private String method; //결제수단

    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // 결제 상태
    private String orderName; // 주문 내용
    private LocalDateTime requestedAt; // 결제 요청 시간
    private LocalDateTime approvedAt; // 결제 승인 시간
    private String receiptUrl; //매출 전표 url
    private long transferBankCode; //은행
    private String cardInfo; //카드 정보

}
