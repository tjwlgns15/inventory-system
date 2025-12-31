package com.yhs.inventroysystem.domain.shipment.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShipmentType {

    EXPORT("정식수출", "Export"),
    SAMPLE("무상샘플", "Free Sample");

    private final String korean;
    private final String english;
}
