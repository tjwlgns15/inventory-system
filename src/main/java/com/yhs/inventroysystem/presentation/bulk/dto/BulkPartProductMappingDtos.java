package com.yhs.inventroysystem.presentation.bulk.dto;

import com.yhs.inventroysystem.application.bulk.command.ProductPartMappingBulkCommand;

import java.util.List;

public class BulkPartProductMappingDtos {

    public record BulkMappingRegisterResponse(
            int totalCount,
            int successCount,
            int failureCount,
            List<BulkMappingFailureDetail> failures
    ) {
        public static BulkMappingRegisterResponse from(ProductPartMappingBulkCommand.Result result) {
            List<BulkMappingFailureDetail> failureDetails = result.failures().stream()
                    .map(BulkMappingFailureDetail::from)
                    .toList();

            return new BulkMappingRegisterResponse(
                    result.totalCount(),
                    result.successCount(),
                    result.failureCount(),
                    failureDetails
            );
        }
    }

    public record BulkMappingFailureDetail(
            int rowNumber,
            String productCode,
            String partCode,
            String errorMessage
    ) {
        public static BulkMappingFailureDetail from(ProductPartMappingBulkCommand.FailureDetail detail) {
            return new BulkMappingFailureDetail(
                    detail.rowNumber(),
                    detail.productCode(),
                    detail.partCode(),
                    detail.errorMessage()
            );
        }
    }
}
