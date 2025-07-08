package com.yhs.inventroysystem.entity.enumerate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductTransactionType {
    PRODUCE("생산"),
    REMOVE("제거"),
    ADJUSTMENT("조정"),
    INITIAL("초기재고");

    private final String displayName;
}