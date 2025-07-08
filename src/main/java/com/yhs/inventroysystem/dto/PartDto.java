package com.yhs.inventroysystem.dto;

import lombok.Data;

@Data
public class PartDto {
    private String code;
    private String name;
    private int stock;
    private Long categoryId;
}
