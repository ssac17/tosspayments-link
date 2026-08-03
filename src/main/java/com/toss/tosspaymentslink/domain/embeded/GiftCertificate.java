package com.toss.tosspaymentslink.domain.embeded;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@ToString
public class GiftCertificate {
    private String approveNo; //결제 승인번호
    private String settlementStatus; //정산 상태
}
