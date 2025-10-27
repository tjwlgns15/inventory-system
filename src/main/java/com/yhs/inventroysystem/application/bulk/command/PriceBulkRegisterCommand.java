package com.yhs.inventroysystem.application.bulk.command;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

public class PriceBulkRegisterCommand {

    @Builder
    public record BulkPriceData(
            String clientCode,
            String productCode,
            BigDecimal unitPrice
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
            String clientCode,
            String productCode,
            String errorMessage
    ) {}
}