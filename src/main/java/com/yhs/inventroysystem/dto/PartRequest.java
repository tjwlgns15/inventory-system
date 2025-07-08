package com.yhs.inventroysystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PartRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    @Min(0)
    private int qty;

    @NotBlank
    private String category;
}
