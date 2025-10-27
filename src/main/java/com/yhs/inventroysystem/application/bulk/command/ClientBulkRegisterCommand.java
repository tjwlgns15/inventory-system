package com.yhs.inventroysystem.application.bulk.command;

import com.yhs.inventroysystem.domain.exchange.Currency;
import lombok.Builder;

import java.util.List;

public class ClientBulkRegisterCommand {

    @Builder
    public record BulkClientData(
            String clientCode,
            String countryCode,
            String name,
            String address,
            String contactNumber,
            String email,
            String currency,
            String parentClientCode  // 상위 거래처 코드 (선택)
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
            String name,
            String errorMessage
    ) {}
}