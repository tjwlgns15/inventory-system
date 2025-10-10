package com.yhs.inventroysystem.domain.exception;

import com.yhs.inventroysystem.domain.delivery.DeliveryStatus;

public class InvalidDeliveryStateException extends RuntimeException {
    public InvalidDeliveryStateException(DeliveryStatus currentStatus, String attemptedAction) {
        super(String.format("납품 상태가 '%s'일 때는 '%s' 작업을 수행할 수 없습니다.",
                currentStatus, attemptedAction));
    }
}