package com.yhs.inventroysystem.application.bulk.command;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

public class ProductBulkRegisterCommand {

    @Builder
    public record BulkProductData(
            String productCode,
            String name,
            BigDecimal defaultUnitPrice,
            String description,
            Integer stockQuantity
    ) {}

    public record Result(
            int totalCount,
            int successCount,
            int failureCount,
            List<FailureDetail> failures
    ) {}

    @Builder
    public record FailureDetail(
            int rowNumber,
            String productCode,
            String name,
            String errorMessage
    ) {}
}