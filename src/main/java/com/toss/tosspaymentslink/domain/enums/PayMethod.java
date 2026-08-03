package com.toss.tosspaymentslink.domain.enums;

import java.util.Arrays;

public enum PayMethod {
    CARD("카드"),
    VIRTUAL_ACCOUNT("가상계좌"),
    EASY_PAY("간편결제"),
    MOBILE_PHONE("휴대폰"),
    TRANSFER("계좌이체"),
    CULTURE_COUPON("문화상품권"),
    BOOK_COUPON("도서문화상품권"),
    GAME_COUPON("게임문화상품권");

    private String description;

    PayMethod(String description) {
        this.description = description;
    }
    public String getDescription() {
        return description;
    }

    public static PayMethod from(String value) {
        if (value == null) return null;

        return Arrays.stream(PayMethod.values())
                .filter(method -> method.getDescription().equals(value) || method.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 결제 수단입니다: " + value));
    }
}
