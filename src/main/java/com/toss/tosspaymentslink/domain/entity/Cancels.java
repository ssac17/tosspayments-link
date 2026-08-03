package com.toss.tosspaymentslink.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@ToString(exclude = "payment")
public class Cancels {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    private Integer cancelAmount; //취소 금액
    private String cancelReason; //취소 사유
    private Integer taxFreeAmount; //취소 금액 중 면세 금액
    private Integer taxExemptionAmount; //취소 금액 중 과세를 제외한 금액(ex, 컵 보증금)
    private Integer refundableAmount; //결제 취소 후 환불 가능한 잔액
    private Integer cardDiscountAmount; //카드사에서 할인해준 금액
    private Integer transferDiscountAmount; //이체사에서 할인해준 금액
    private Integer easyPayDiscountAmount; //간편결제사에서 할인해준 금액
    private OffsetDateTime canceledAt; //취소 완료 시각
    private String transactionKey; //취소 거래의 고유 키값
    private String receiptKey; //취소 건의 현금영수증 키값
    private String cancelStatus; //취소 상태
    private String cancelRequestId; //취소 요청 고유 ID
}

