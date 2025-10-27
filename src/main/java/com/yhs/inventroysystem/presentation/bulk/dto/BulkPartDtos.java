package com.yhs.inventroysystem.presentation.bulk.dto;

import java.util.List;

import static com.yhs.inventroysystem.application.bulk.command.PartBulkRegisterCommand.*;

public class BulkPartDtos {
    public record BulkRegisterResponse(
            int totalCount,
            int successCount,
            int failureCount,
            List<BulkFailureDetail> failures
    ) {
        public static BulkRegisterResponse from(Result result) {
            List<BulkFailureDetail> failureDetails = result.failures().stream()
                    .map(BulkFailureDetail::from)
                    .toList();

            return new BulkRegisterResponse(
                    result.totalCount(),
                    result.successCount(),
                    result.failureCount(),
                    failureDetails
            );
        }
    }

    public record BulkFailureDetail(
            int rowNumber,
            String partCode,
            String name,
            String errorMessage
    ) {
        public static BulkFailureDetail from(FailureDetail detail) {
            return new BulkFailureDetail(
                    detail.rowNumber(),
                    detail.partCode(),
                    detail.name(),
                    detail.errorMessage()
            );
        }
    }
}
