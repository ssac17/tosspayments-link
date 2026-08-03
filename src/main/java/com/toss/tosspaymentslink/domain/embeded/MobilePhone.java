package com.toss.tosspaymentslink.domain.embeded;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@ToString
public class MobilePhone {
    private String customerMobilePhone; //고객 휴대폰 번호
    private String settlementStatus; //정산 상태
    private String receiptUrl; //휴대폰 결제 영수증 URLƒ
}
