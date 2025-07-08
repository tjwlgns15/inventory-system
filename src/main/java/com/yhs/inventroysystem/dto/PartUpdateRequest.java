package com.yhs.inventroysystem.dto;

import lombok.Data;

@Data
public class PartUpdateRequest {
    private String name;
    private Integer stock;
    private String category;
}
