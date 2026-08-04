package com.toss.tosspaymentslink.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponseDto<T>(
    List<T> content,
    PageInfo page
) {

    public record PageInfo(
            int size,
            long totalElements,
            int totalPages,
            int number
    ) {
        public static PageInfo from(Page<?> page) {
            return new PageInfo(
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.getNumber()
            );
        }
    }
    public static <T> PageResponseDto<T> from(Page<T> page) {
        return new PageResponseDto<>(
                page.getContent(),
                PageInfo.from(page)
        );
    }
}
