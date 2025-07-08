package com.yhs.inventroysystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PartResponse {
    private Long id;
    private String code;
    private String name;
    private int stock;
    private int initialQty;
    private String category;
}
