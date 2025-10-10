package com.yhs.inventroysystem.domain.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
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

    public static ResourceNotFoundException price(Long clientId, Long productId) {
        return new ResourceNotFoundException(
                String.format("가격 정보를 찾을 수 없습니다. 거래처 ID: %d, 제품 ID: %d", clientId, productId)
        );
    }
}
