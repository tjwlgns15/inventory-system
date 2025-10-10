package com.yhs.inventroysystem.domain.exception;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }

    public static DuplicateResourceException partCode(String partCode) {
        return new DuplicateResourceException("이미 존재하는 부품 코드입니다: " + partCode);
    }

    public static DuplicateResourceException productCode(String productCode) {
        return new DuplicateResourceException("이미 존재하는 제품 코드입니다: " + productCode);
    }

    public static DuplicateResourceException clientCode(String clientCode) {
        return new DuplicateResourceException("이미 존재하는 거래처 코드입니다: " + clientCode);
    }

    public static DuplicateResourceException price(Long clientId, Long productId) {
        return new DuplicateResourceException(
                String.format("이미 등록된 가격 정보입니다. 거래처 ID: %d, 제품 ID: %d", clientId, productId)
        );
    }
}
