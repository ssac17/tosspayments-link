package com.toss.tosspaymentslink.domain.entity;

import com.toss.tosspaymentslink.domain.embeded.Failure;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class CashReceipts {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Payment payment;

    private String receiptKey; //현금영수증의 키값
    private String orderId; //주문 번호
    private String orderName; // 구매 상품
    private String type; //현금영수증 종류
    private String issueNumber; //현금영수증 승인번호
    private String receiptUrl; //현금영수증 조회 URL
    private String businessNumber; //사업자 등록번호
    private String transactionType; //현금영수증 발급 종류
    private Integer amount; //현금영수증 처리된 금액
    private Integer taxFreeAmount; //면세 금액
    private String issueStatus; //현금영수증 발급 상태

    @Embedded
    private Failure failure; //현금영수증 발급 실패 정보

    private String customerIdentityNumber; //현금영수증 발급에 필요한 소비자 인증수단
    private OffsetDateTime requestedAt; //현금영수증 발급 요청 시각
}
