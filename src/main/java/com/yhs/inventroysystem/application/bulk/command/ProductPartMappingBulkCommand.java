package com.yhs.inventroysystem.application.bulk.command;

import lombok.Builder;

import java.util.List;

public class ProductPartMappingBulkCommand {

    @Builder
    public record BulkMappingData(
            String productCode,
            String partCode,
            Integer requiredQuantity
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
            String partCode,
            String errorMessage
    ) {}
}