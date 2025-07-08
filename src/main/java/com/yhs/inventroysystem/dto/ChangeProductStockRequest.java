package com.yhs.inventroysystem.dto;

import lombok.Data;

@Data
public class ChangeProductStockRequest {
    private Long productId;
    private int delta;
}
