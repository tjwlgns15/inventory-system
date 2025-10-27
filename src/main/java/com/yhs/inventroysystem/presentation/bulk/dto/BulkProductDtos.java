package com.yhs.inventroysystem.presentation.bulk.dto;

import com.yhs.inventroysystem.application.bulk.command.ProductBulkRegisterCommand;

import java.util.List;

import static com.yhs.inventroysystem.application.bulk.command.ProductBulkRegisterCommand.*;

public class BulkProductDtos {

    public record BulkProductRegisterResponse(
            int totalCount,
            int successCount,
            int failureCount,
            List<BulkProductFailureDetail> failures
    ) {
        public static BulkProductRegisterResponse from(Result result) {
            List<BulkProductFailureDetail> failureDetails = result.failures().stream()
                    .map(BulkProductFailureDetail::from)
                    .toList();

            return new BulkProductRegisterResponse(
                    result.totalCount(),
                    result.successCount(),
                    result.failureCount(),
                    failureDetails
            );
        }
    }

    public record BulkProductFailureDetail(
            int rowNumber,
            String productCode,
            String name,
            String errorMessage
    ) {
        public static BulkProductFailureDetail from(FailureDetail detail) {
            return new BulkProductFailureDetail(
                    detail.rowNumber(),
                    detail.productCode(),
                    detail.name(),
                    detail.errorMessage()
            );
        }
    }
}
