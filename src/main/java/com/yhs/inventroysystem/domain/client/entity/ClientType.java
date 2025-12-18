package com.yhs.inventroysystem.domain.client.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ClientType {
    PARENT("상위", "최상위 거래처"),
    CHILD("하위", "하위 거래처");

    private final String displayName;
    private final String description;
}
