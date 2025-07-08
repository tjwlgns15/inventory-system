package com.yhs.inventroysystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private List<PartInfo> parts;
    private int stock;

    @Data
    @AllArgsConstructor
    public static class PartInfo {
        private String code;
        private String name;
        private int count;
    }
}
