package com.yhs.inventroysystem.domain.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {

    private static final String ERROR_CODE = "RESOURCE_NOT_FOUND";

    private ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ERROR_CODE);
    }


    public static ResourceNotFoundException part(Long partId) {
        return new ResourceNotFoundException("부품을 찾을 수 없습니다. ID: " + partId);
    }

    public static ResourceNotFoundException product(Long productId) {
        return new ResourceNotFoundException("제품을 찾을 수 없습니다. ID: " + productId);
    }

    public static ResourceNotFoundException client(Long clientId) {
        return new ResourceNotFoundException("거래처를 찾을 수 없습니다. ID: " + clientId);
    }

    public static ResourceNotFoundException delivery(Long deliveryId) {
        return new ResourceNotFoundException("납품을 찾을 수 없습니다. ID: " + deliveryId);
    }

    public static ResourceNotFoundException user(Long userId) {
        return new ResourceNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId);
    }

    public static ResourceNotFoundException user(String username) {
        return new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + username);
    }
    public static ResourceNotFoundException country(Long countryId) {
        return new ResourceNotFoundException("국가를 찾을 수 없습니다. ID: " + countryId);
    }

    public static ResourceNotFoundException price(Long clientId, Long productId) {
        return new ResourceNotFoundException(
                String.format("가격 정보를 찾을 수 없습니다. 거래처 ID: %d, 제품 ID: %d", clientId, productId)
        );
    }

    public static ResourceNotFoundException productLine(Long productLineId) {
        return new ResourceNotFoundException("제품라인을 찾을 수 없습니다. ID: " + productLineId);
    }
}
