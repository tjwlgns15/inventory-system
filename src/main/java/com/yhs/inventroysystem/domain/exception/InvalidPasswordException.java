package com.yhs.inventroysystem.domain.exception;

import org.springframework.http.HttpStatus;

public class InvalidPasswordException extends BusinessException {

    private static final String ERROR_CODE = "INVALID_PASSWORD";

    public InvalidPasswordException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, ERROR_CODE);
    }

    public InvalidPasswordException() {
        super("현재 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED, ERROR_CODE);
    }

}