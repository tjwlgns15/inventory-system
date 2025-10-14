package com.yhs.inventroysystem.domain.exception;

import org.springframework.http.HttpStatus;

/**
 * 자원 정보 중복 시 발생
 */
public class DuplicateResourceException extends BusinessException {

    private static final String ERROR_CODE = "DUPLICATE_RESOURCE";

    private DuplicateResourceException(String message) {
        super(message, HttpStatus.CONFLICT, ERROR_CODE);
    }

    // 부품
    public static DuplicateResourceException partCode(String partCode) {
        return new DuplicateResourceException("이미 존재하는 부품 코드입니다: " + partCode);
    }

    public static DuplicateResourceException partName(String partName) {
        return new DuplicateResourceException("이미 존재하는 부품명입니다: " + partName);
    }

    // 제품
    public static DuplicateResourceException productCode(String productCode) {
        return new DuplicateResourceException("이미 존재하는 제품 코드입니다: " + productCode);
    }

    public static DuplicateResourceException productName(String productName) {
        return new DuplicateResourceException("이미 존재하는 제품명입니다: " + productName);
    }

    // 거래처
    public static DuplicateResourceException clientCode(String clientCode) {
        return new DuplicateResourceException("이미 존재하는 거래처 코드입니다: " + clientCode);
    }

    // 가격
    public static DuplicateResourceException price(Long clientId, Long productId) {
        return new DuplicateResourceException(
                String.format("이미 등록된 가격 정보입니다. 거래처 ID: %d, 제품 ID: %d", clientId, productId)
        );
    }

    // 사용자
    public static DuplicateResourceException username(String username) {
        return new DuplicateResourceException("이미 존재하는 사용자명입니다: " + username);
    }

    public static DuplicateResourceException email(String email) {
        return new DuplicateResourceException("이미 존재하는 이메일입니다: " + email);
    }
}