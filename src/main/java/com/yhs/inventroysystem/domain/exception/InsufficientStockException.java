package com.yhs.inventroysystem.domain.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String itemName, Integer required, Integer available) {
        super(String.format("%s의 재고가 부족합니다. 필요: %d, 현재: %d", itemName, required, available));
    }
}
