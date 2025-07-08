package com.yhs.inventroysystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ProductStockHistoryDto {
    private String productName;
    private String type;           // PRODUCE
    private int beforeStock;
    private int delta;
    private int afterStock;
    private LocalDateTime createdAt;
}