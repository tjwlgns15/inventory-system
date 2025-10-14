package com.yhs.inventroysystem.domain.exception;

import org.springframework.http.HttpStatus;

public class UserDisabledException extends BusinessException {

    private static final String ERROR_CODE = "USER_DISABLED";

    public UserDisabledException(String username) {
        super("비활성화된 계정입니다: " + username, HttpStatus.FORBIDDEN, ERROR_CODE);
    }
}