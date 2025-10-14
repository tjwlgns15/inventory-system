package com.yhs.inventroysystem.domain.delivery;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DeliveryStatus {
    PENDING("대기"),
    COMPLETED("완료"),
    CANCELLED("취소");

    private final String displayName;
}
