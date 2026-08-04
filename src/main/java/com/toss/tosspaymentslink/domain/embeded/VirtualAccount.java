package com.toss.tosspaymentslink.domain.embeded;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import lombok.*;

import java.time.OffsetDateTime;

@Embeddable
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@ToString
public class VirtualAccount {
    private String accountType; //가상계좌 타입
    private String accountNumber; //가상계좌 번호
    private String backCode; //은행 코드
    private String customerName; //예금주명
    private String depositorName; //입금자명
    private OffsetDateTime dueDate; //입금기한
    private String refundStatus; //환불처리 상태
    private Boolean expired; //만료 여부
    private String settlementStatus; //정산 상태
    private OffsetDateTime settlementDate;

    @Embedded
    private RefundReceiveAccount refundReceiveAccount;
}
