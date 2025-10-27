package com.yhs.inventroysystem.presentation.bulk.dto;

import com.yhs.inventroysystem.application.bulk.command.PriceBulkRegisterCommand;

import java.util.List;

import static com.yhs.inventroysystem.application.bulk.command.PriceBulkRegisterCommand.*;

public class BulkPriceDtos {

    public record BulkPriceRegisterResponse(
            int totalCount,
            int successCount,
            int failureCount,
            List<BulkPriceFailureDetail> failures
    ) {
        public static BulkPriceRegisterResponse from(Result result) {
            List<BulkPriceFailureDetail> failureDetails = result.failures().stream()
                    .map(BulkPriceFailureDetail::from)
                    .toList();

            return new BulkPriceRegisterResponse(
                    result.totalCount(),
                    result.successCount(),
                    result.failureCount(),
                    failureDetails
            );
        }
    }

    public record BulkPriceFailureDetail(
            int rowNumber,
            String clientCode,
            String productCode,
            String errorMessage
    ) {
        public static BulkPriceFailureDetail from(FailureDetail detail) {
            return new BulkPriceFailureDetail(
                    detail.rowNumber(),
                    detail.clientCode(),
                    detail.productCode(),
                    detail.errorMessage()
            );
        }
    }
}
