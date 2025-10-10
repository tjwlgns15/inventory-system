package com.yhs.inventroysystem.application.part;

import com.yhs.inventroysystem.domain.part.Part;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class PartCommands {

    public record PartRegisterCommand(
            String partCode,
            String name,
            String specification,
            Integer initialStock,
            String unit
    ) {}

    public record PartUpdateCommand(
            String partCode,
            String name,
            String specification,
            String unit
    ) {}
}
