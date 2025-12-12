package com.yhs.inventroysystem.domain.quotation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum QuotationType {

    RECEIPT("접수"),
    ISSUANCE("발행")
    ;

    private final String description;
}
