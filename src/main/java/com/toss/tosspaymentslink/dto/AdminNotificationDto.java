package com.toss.tosspaymentslink.dto;

import com.toss.tosspaymentslink.domain.enums.NotificationType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class AdminNotificationDto {
    private NotificationType type;
    private String orderName;
    private String reason;
    private Integer amount;
}
