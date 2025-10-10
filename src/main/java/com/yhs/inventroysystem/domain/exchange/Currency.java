package com.yhs.inventroysystem.domain.exchange;

import lombok.Getter;

@Getter
public enum Currency {
    KRW("KRW", "원화", "₩"),
    USD("USD", "달러", "$"),
    JPY("JPY", "엔화", "¥"),
    EUR("EUR", "유로", "€"),
    CNY("CNY", "위안", "¥"),
    GBP("GBP", "파운드", "£");

    private final String code;
    private final String name;
    private final String symbol;

    Currency(String code, String name, String symbol) {
        this.code = code;
        this.name = name;
        this.symbol = symbol;
    }
}