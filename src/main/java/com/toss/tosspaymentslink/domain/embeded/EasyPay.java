package com.toss.tosspaymentslink.domain.embeded;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
public class EasyPay {
    private String provider; //간편결제사 코드
    private Integer amount; //간편결제 서비스에 등록된 계좌,현금성 포인트로 결제한 금액
    private Integer discountAmount; //간편결제 서비스의 적립 포인트나 쿠폰 등으로 즉시 할인된 금액


}
