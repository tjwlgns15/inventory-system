package com.yhs.inventroysystem.domain.shipment.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TradeTerms {

    FOB("본선인도", "FOB Korea"),
    CNF("정식수출", "CNF"),
    EXW("공장인도", "EXW Solmitech factory"),
    NA("해당없음", "N/A")

    ;

    private final String korean;
    private final String english;
}
