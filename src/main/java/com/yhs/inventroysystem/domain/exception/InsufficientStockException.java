package com.yhs.inventroysystem.domain.exception;

import org.springframework.http.HttpStatus;

/**
 * 재고 수량 부족 시 발생
 */
public class InsufficientStockException extends BusinessException {

    private static final String ERROR_CODE = "INSUFFICIENT_STOCK";

    private InsufficientStockException(String message) {
        super(message, HttpStatus.CONFLICT, ERROR_CODE);

    }
    public static InsufficientStockException insufficientStock(String itemName, Integer required, Integer available) {
        return new InsufficientStockException(
                String.format("%s의 재고가 부족합니다. 필요: %d, 현재: %d", itemName, required, available)
        );
    }
}
