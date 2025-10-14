package com.yhs.inventroysystem.domain.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductTransactionType {
    PRODUCE("생산"),
    DELIVERY("납품"),
    ADJUSTMENT("조정"),
    INITIAL("초기 재고");

    private final String displayName;
}