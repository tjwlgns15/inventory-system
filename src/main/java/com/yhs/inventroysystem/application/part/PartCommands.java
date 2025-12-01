package com.yhs.inventroysystem.application.part;

import com.yhs.inventroysystem.domain.part.Part;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
}
