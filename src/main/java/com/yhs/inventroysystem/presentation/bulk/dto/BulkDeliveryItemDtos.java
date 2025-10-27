package com.yhs.inventroysystem.presentation.bulk.dto;

import com.yhs.inventroysystem.application.bulk.command.DeliveryItemBulkRegisterCommand;

import java.util.List;

import static com.yhs.inventroysystem.application.bulk.command.DeliveryItemBulkRegisterCommand.*;

public class BulkDeliveryItemDtos {

    public record BulkDeliveryItemRegisterResponse(
            int totalCount,
            int successCount,
            int failureCount,
            List<BulkDeliveryItemFailureDetail> failures
    ) {
        public static BulkDeliveryItemRegisterResponse from(Result result) {
            List<BulkDeliveryItemFailureDetail> failureDetails = result.failures().stream()
                    .map(BulkDeliveryItemFailureDetail::from)
                    .toList();

            return new BulkDeliveryItemRegisterResponse(
                    result.totalCount(),
                    result.successCount(),
                    result.failureCount(),
                    failureDetails
            );
        }
    }

    public record BulkDeliveryItemFailureDetail(
            int rowNumber,
            String deliveryNumber,
            String productCode,
            String errorMessage
    ) {
        public static BulkDeliveryItemFailureDetail from(FailureDetail detail) {
            return new BulkDeliveryItemFailureDetail(
                    detail.rowNumber(),
                    detail.deliveryNumber(),
                    detail.productCode(),
                    detail.errorMessage()
            );
        }
    }
}
