package com.yhs.inventroysystem.presentation.part;

import com.yhs.inventroysystem.domain.part.Part;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public class PartDtos {

    public record PartRegisterRequest(
            @NotBlank(message = "부품 코드는 필수입니다")
            String partCode,

            @NotBlank(message = "부품명은 필수입니다")
            String name,

            String specification,

            @NotNull(message = "초기 재고는 필수입니다")
            @PositiveOrZero(message = "초기 재고는 0 이상이어야 합니다")
            Integer initialStock,

            @NotBlank(message = "단위는 필수입니다")
            String unit
    ) {}

    public record PartResponse(
            Long id,
            String partCode,
            String name,
            String specification,
            Integer stockQuantity,
            String unit
    ) {
        public static PartResponse from(Part part) {
            return new PartResponse(
                    part.getId(),
                    part.getPartCode(),
                    part.getName(),
                    part.getSpecification(),
                    part.getStockQuantity(),
                    part.getUnit()
            );
        }
    }

    public record StockUpdateRequest(
            @NotNull(message = "수량은 필수입니다")
            @Positive(message = "수량은 양수여야 합니다")
            Integer quantity
    ) {}

    public record PartUpdateRequest(
            String partCode,
            String name,
            String specification,
            String unit
    ) {}
}