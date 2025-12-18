package com.yhs.inventroysystem.application.bulk.command;

import com.yhs.inventroysystem.domain.delivery.entity.DeliveryStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class DeliveryBulkRegisterCommand {

    @Builder
    public record BulkDeliveryData(
            String deliveryNumber,
            String clientCode,
            LocalDate orderedAt,
            LocalDate requestedAt,
            DeliveryStatus status,
            LocalDateTime deliveredAt,
            BigDecimal totalDiscountAmount,
            String discountNote,
            String memo
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
            String clientCode,
            String errorMessage
    ) {}
}
