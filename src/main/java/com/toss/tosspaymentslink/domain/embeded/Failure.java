package com.toss.tosspaymentslink.domain.embeded;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@ToString
public class Failure {
    private String code; //에러 코드
    private String message; //에러 메시지
}
