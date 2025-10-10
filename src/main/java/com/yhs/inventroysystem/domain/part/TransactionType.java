package com.yhs.inventroysystem.domain.part;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionType {
    INBOUND("입고"),
    OUTBOUND("출고"),
    ADJUSTMENT("조정"),
    INITIAL("초기재고");

    private final String displayName;
}