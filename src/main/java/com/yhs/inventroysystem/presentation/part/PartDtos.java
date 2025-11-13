package com.yhs.inventroysystem.presentation.part;

import com.yhs.inventroysystem.domain.part.Part;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.domain.Page;

import java.util.List;

public class PartDtos {

    public record PartRegisterRequest(
            @NotBlank(message = "부품 코드는 필수입니다")
            String partCode,

            @NotBlank(message = "부품명은 필수입니다")
            String name,

            String specification,

            @NotNull(message = "초기 재고는 필수입니다")
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
            String unit,
            String imagePath,
            String originalImageName
    ) {
        public static PartResponse from(Part part) {
            return new PartResponse(
                    part.getId(),
                    part.getPartCode(),
                    part.getName(),
                    part.getSpecification(),
                    part.getStockQuantity(),
                    part.getUnit(),
                    part.getImagePath(),
                    part.getOriginalImageName()
            );
        }
    }

    public record PagedPartResponse(
            List<PartResponse> content,
            int pageNumber,
            int pageSize,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last,
            boolean empty
    ) {
        public static PagedPartResponse from(Page<Part> page) {
            List<PartResponse> content = page.getContent().stream()
                    .map(PartResponse::from)
                    .toList();

            return new PagedPartResponse(
                    content,
                    page.getNumber(),
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.isFirst(),
                    page.isLast(),
                    page.isEmpty()
            );
        }
    }

    public record StockUpdateRequest(
            @NotNull(message = "수량은 필수입니다")
            @Positive(message = "수량은 양수여야 합니다")
            Integer quantity
    ) {}

    public record PartUpdateRequest(
            String name,
            String specification,
            Integer stockQuantity,
            String unit
    ) {}
}