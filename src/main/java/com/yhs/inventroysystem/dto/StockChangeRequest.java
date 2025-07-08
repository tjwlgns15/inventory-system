package com.yhs.inventroysystem.dto;

import lombok.Data;

@Data
public class StockChangeRequest {
    private int quantity;   // 입출고 수량
    private String type;    // "INBOUND" 또는 "OUTBOUND"
}
