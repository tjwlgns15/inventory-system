package com.yhs.inventroysystem.domain.exception;

import org.springframework.http.HttpStatus;

public class PartInUseException extends BusinessException {

    private static final String ERROR_CODE = "PART_IN_USE";

    private PartInUseException(String message) {
        super(message, HttpStatus.CONFLICT, ERROR_CODE);
    }

    public static PartInUseException usedInProducts(String partCode, int productCount) {
        return new PartInUseException(
                String.format("부품 '%s'은(는) %d개의 제품에서 사용 중이므로 삭제할 수 없습니다.", partCode, productCount)
        );
    }

    public static PartInUseException usedInProducts(Long partId) {
        return new PartInUseException(
                String.format("부품 ID %d은(는) 제품에서 사용 중이므로 삭제할 수 없습니다.", partId)
        );
    }
}
