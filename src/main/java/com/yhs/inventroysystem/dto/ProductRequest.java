package com.yhs.inventroysystem.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ProductRequest {
    @NotBlank
    private String name;

    private int stock;

    @NotEmpty
    private List<PartInput> parts;

    @Data
    public static class PartInput {
        @NotBlank
        private String code;
        private int count;
    }
}
