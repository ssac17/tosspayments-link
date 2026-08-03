package com.toss.tosspaymentslink.domain.embeded;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
public class Transfer {
    private String bankCode; //이체 은행 코드
    private String settlementStatus; //정산 상태
}
