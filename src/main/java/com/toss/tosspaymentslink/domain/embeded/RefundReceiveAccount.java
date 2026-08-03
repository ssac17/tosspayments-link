package com.toss.tosspaymentslink.domain.embeded;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@ToString
public class RefundReceiveAccount {
    private String bankCode; //환불계좌 은행 코드
    private String accountNumber; //환불계좌 번호
    private String holderName; //환불계좌 예금주명
}
