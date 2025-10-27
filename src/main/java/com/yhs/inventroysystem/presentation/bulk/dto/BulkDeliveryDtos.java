package com.yhs.inventroysystem.presentation.bulk.dto;

import com.yhs.inventroysystem.application.bulk.command.DeliveryBulkRegisterCommand;

import java.util.List;

import static com.yhs.inventroysystem.application.bulk.command.DeliveryBulkRegisterCommand.*;

public class BulkDeliveryDtos {

    public record BulkDeliveryRegisterResponse(
            int totalCount,
            int successCount,
            int failureCount,
            List<BulkDeliveryFailureDetail> failures
    ) {
        public static BulkDeliveryRegisterResponse from(Result result) {
            List<BulkDeliveryFailureDetail> failureDetails = result.failures().stream()
                    .map(BulkDeliveryFailureDetail::from)
                    .toList();

            return new BulkDeliveryRegisterResponse(
                    result.totalCount(),
                    result.successCount(),
                    result.failureCount(),
                    failureDetails
            );
        }
    }

    public record BulkDeliveryFailureDetail(
            int rowNumber,
            String deliveryNumber,
            String clientCode,
            String errorMessage
    ) {
        public static BulkDeliveryFailureDetail from(FailureDetail detail) {
            return new BulkDeliveryFailureDetail(
                    detail.rowNumber(),
                    detail.deliveryNumber(),
                    detail.clientCode(),
                    detail.errorMessage()
            );
        }
    }
}
