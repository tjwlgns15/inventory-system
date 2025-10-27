package com.yhs.inventroysystem.presentation.bulk.dto;

import com.yhs.inventroysystem.application.bulk.command.ClientBulkRegisterCommand;

import java.util.List;

public class BulkClientDtos {

    public record BulkClientRegisterResponse(
            int totalCount,
            int successCount,
            int failureCount,
            List<BulkClientFailureDetail> failures
    ) {
        public static BulkClientRegisterResponse from(ClientBulkRegisterCommand.Result result) {
            List<BulkClientFailureDetail> failureDetails = result.failures().stream()
                    .map(BulkClientFailureDetail::from)
                    .toList();

            return new BulkClientRegisterResponse(
                    result.totalCount(),
                    result.successCount(),
                    result.failureCount(),
                    failureDetails
            );
        }
    }

    public record BulkClientFailureDetail(
            int rowNumber,
            String clientCode,
            String name,
            String errorMessage
    ) {
        public static BulkClientFailureDetail from(ClientBulkRegisterCommand.FailureDetail detail) {
            return new BulkClientFailureDetail(
                    detail.rowNumber(),
                    detail.clientCode(),
                    detail.name(),
                    detail.errorMessage()
            );
        }
    }
}
