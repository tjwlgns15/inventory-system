package com.yhs.inventroysystem.domain.delivery;

import lombok.Getter;

@Getter
public enum DeliveryStatus {
    PENDING,    // 대기
    COMPLETED,  // 완료
    CANCELLED   // 취소
}
