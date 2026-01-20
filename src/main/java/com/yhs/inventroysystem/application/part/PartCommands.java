package com.yhs.inventroysystem.application.part;

import org.springframework.web.multipart.MultipartFile;

public class PartCommands {

    public record PartRegisterCommand(
            String partCode,
            String name,
            String specification,
            Integer initialStock,
            String unit,
            MultipartFile imageFile
    ) {}

    public record PartUpdateCommand(
            String name,
            String specification,
            Integer stockQuantity,
            String unit,
            MultipartFile imageFile
    ) {}

    public record PartStockUpdateCommand(
            Integer adjustmentQuantity,
            String note
    ) {}

    public record PartStockIncreaseCommand(
            Integer quantity,
            String note
    ) {}

    public record PartStockDecreaseCommand(
            Integer quantity,
            String note
    ) {}
}
