package com.toss.tosspaymentslink.domain.embeded;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
public class CashReceipt {
    private String type; //현금영수증 종류
    private String receiptKey; //현금영수증의 키값
    private String issueNumber; //현금영수증 승인번호
    private String receiptUrl; //현금영수증 조회 URL
    private Integer amount; //현금영수증 처리 금액
    private Integer taxFreeAmount; //면세처리 금액
}
