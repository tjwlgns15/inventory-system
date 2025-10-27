package com.yhs.inventroysystem.application.bulk.command;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

public class DeliveryItemBulkRegisterCommand {

    @Builder
    public record BulkDeliveryItemData(
            String deliveryNumber,
            String productCode,
            Integer quantity,
            BigDecimal actualUnitPrice,
            String priceNote,
            Boolean isFreeItem
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
            String deliveryNumber,
            String productCode,
            String errorMessage
    ) {}
}