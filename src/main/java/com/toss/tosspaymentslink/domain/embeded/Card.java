package com.toss.tosspaymentslink.domain.embeded;

import com.toss.tosspaymentslink.domain.enums.AcquireStatus;
import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Card {
    private Integer amount;
    private String issuerCode; //카드 발급사 두 자리 코드
    private String acquirerCode; //카드 매입사 두 자리 코드
    private String number; //카드 번호
    private Integer installmentPlanMonths; //할부 개월 수, 일시불 0
    private String approveNo; //카드사 승인번호
    private Boolean useCardPoint; //포인트 사용 여부
    private String cardType; //카드 종류
    private String ownerType; //카드 소유자 유형
    @Enumerated(EnumType.STRING)
    private AcquireStatus acquireStatus; //카드 매입 상태
    private Boolean isInterestFree; //무이자 여부
    private String interestPayer; //할부가 적용된 결제에서 할부 수수료를 부담하는 주체
}
