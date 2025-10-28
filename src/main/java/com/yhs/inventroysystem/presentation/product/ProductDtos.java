package com.yhs.inventroysystem.presentation.product;

import com.yhs.inventroysystem.domain.product.Product;
import com.yhs.inventroysystem.domain.product.ProductPart;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class ProductDtos {

    public record ProductRegisterRequest(
            @NotBlank(message = "제품 코드는 필수입니다")
            String productCode,

            @NotBlank(message = "제품명은 필수입니다")
            String name,

            BigDecimal defaultUnitPrice,

            String description,

            @NotNull(message = "초기 재고는 필수입니다")
            @Positive(message = "초기 재고는 0보다 커야 합니다")
            Integer initialStock,

            List<PartMappingRequest> partMappings
    ) {}


    public record PartMappingRequest(
            @NotNull(message = "부품 ID는 필수입니다")
            Long partId,

            @NotNull(message = "필요 수량은 필수입니다")
            @Positive(message = "필요 수량은 0보다 커야 합니다")
            Integer requiredQuantity
    ) {}


    public record ProductResponse(
            Long id,
            String productCode,
            String name,
            BigDecimal defaultUnitPrice,
            String description,
            Integer stockQuantity
    ) {
        public static ProductResponse from(Product product) {
            return new ProductResponse(
                    product.getId(),
                    product.getProductCode(),
                    product.getName(),
                    product.getDefaultUnitPrice(),
                    product.getDescription(),
                    product.getStockQuantity()
            );
        }
    }


    public record ProductDetailResponse(
            Long id,
            String productCode,
            String name,
            BigDecimal defaultUnitPrice,
            String description,
            Integer stockQuantity,
            List<PartMappingResponse> partMappings
    ) {
        public static ProductDetailResponse from(Product product) {
            return new ProductDetailResponse(
                    product.getId(),
                    product.getProductCode(),
                    product.getName(),
                    product.getDefaultUnitPrice(),
                    product.getDescription(),
                    product.getStockQuantity(),
                    product.getPartMappings().stream()
                            .map(PartMappingResponse::from)
                            .collect(Collectors.toList())
            );
        }
    }


    public record PartMappingResponse(
            Long partId,
            String partCode,
            String partName,
            Integer requiredQuantity,
            Integer partStockQuantity,
            String imagePath,
            String originalImageName
    ) {
        public static PartMappingResponse from(ProductPart mapping) {
            return new PartMappingResponse(
                    mapping.getPart().getId(),
                    mapping.getPart().getPartCode(),
                    mapping.getPart().getName(),
                    mapping.getRequiredQuantity(),
                    mapping.getPart().getStockQuantity(),
                    mapping.getPart().getImagePath(),
                    mapping.getPart().getOriginalImageName()
            );
        }
    }

    public record ProductionRequest(
            Long id,
            @NotNull(message = "생산 수량은 필수입니다")
            @Positive(message = "생산 수량은 0보다 커야 합니다")
            Integer quantity
    ) {}

    public record ProductUpdateRequest(
            @NotBlank(message = "제품명은 필수입니다")
            String name,

            BigDecimal defaultUnitPrice,

            String description,

            List<PartMappingRequest> partMappings
    ) {}

    public record MaxProducibleResponse(Long productId, Integer maxProducibleQuantity) {}

    public record ProductionValidationRequest(Integer quantity) {}

    public record ProductionValidationResponse(
            boolean canProduce,
            Integer requestedQuantity,
            Integer maxProducibleQuantity,
            List<InsufficientPartInfo> insufficientParts
    ) {}

    public record InsufficientPartInfo(
            Long partId,
            String partName,
            String partCode,
            Integer requiredPerProduct,
            Integer totalRequired,
            Integer availableStock,
            Integer shortage
    ) {}
}
