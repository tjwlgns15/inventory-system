package com.yhs.inventroysystem.domain.exception;

import com.yhs.inventroysystem.domain.delivery.DeliveryStatus;
import org.springframework.http.HttpStatus;

public class InvalidDeliveryStateException extends BusinessException {

    private static final String ERROR_CODE = "INVALID_DELIVERY_STATE";

    public InvalidDeliveryStateException(DeliveryStatus currentStatus, String attemptedAction) {
        super(String.format( "납품 상태가 '%s'일 때는 '%s' 작업을 수행할 수 없습니다.", currentStatus.getDisplayName(), attemptedAction), HttpStatus.CONFLICT, ERROR_CODE);
    }
}