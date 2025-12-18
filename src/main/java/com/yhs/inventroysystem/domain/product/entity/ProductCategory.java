package com.yhs.inventroysystem.domain.product.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductCategory {
    HARDWARE("하드웨어"),
    SOFTWARE("소프트웨어"),
    SERVICE("서비스")

    ;
    private final String displayName;
}
