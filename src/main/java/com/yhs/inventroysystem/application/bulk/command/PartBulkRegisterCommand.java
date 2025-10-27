package com.yhs.inventroysystem.application.bulk.command;

import lombok.Builder;

import java.util.List;

public class PartBulkRegisterCommand {

    @Builder
    public record BulkPartData(
            String partCode,
            String name,
            String specification,
            Integer initialStock,
            String unit
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
            String partCode,
            String name,
            String errorMessage
    ) {}
}