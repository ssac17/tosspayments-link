package com.toss.tosspaymentslink.dto;

import com.toss.tosspaymentslink.domain.enums.NotificationType;

public class AdminNotificationDto {
    private NotificationType type;
    private String orderName;
    private String reason;
    private Integer amount;
}
