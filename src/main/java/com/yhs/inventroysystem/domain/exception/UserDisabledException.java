package com.yhs.inventroysystem.domain.exception;

public class UserDisabledException extends RuntimeException {
    public UserDisabledException(String username) {
        super("비활성화된 계정입니다: " + username);
    }
}