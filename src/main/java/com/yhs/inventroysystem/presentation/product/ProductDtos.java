package com.yhs.inventroysystem.presentation.product;

import com.yhs.inventroysystem.domain.product.entity.Product;
import com.yhs.inventroysystem.domain.product.entity.ProductCategory;
import com.yhs.inventroysystem.domain.product.entity.ProductPart;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class ProductDtos {

    public record ProductRegisterRequest(
            @NotNull(message = "제품 카테고리는 필수입니다")
            ProductCategory productCategory,

            Long productLineId,

            @NotBlank(message = "제품 코드는 필수입니다")
            String productCode,

            @NotBlank(message = "제품명은 필수입니다")
            String name,

            BigDecimal defaultUnitPrice,

            String description,

            @NotNull(message = "초기 재고는 필수입니다")
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
            ProductCategory productCategory,
            ProductLineInfo productLine,
            String productCode,
            String name,
            BigDecimal defaultUnitPrice,
            String description,
            Integer stockQuantity,
            boolean isFeatured,
            boolean isFeatured2,
            Integer displayOrder
    ) {
        public static ProductResponse from(Product product) {
            return new ProductResponse(
                    product.getId(),
                    product.getProductCategory(),
                    product.getProductLine() != null
                            ? new ProductLineInfo(
                            product.getProductLine().getId(),
                            product.getProductLine().getName()
                    )
                            : null,
                    product.getProductCode(),
                    product.getName(),
                    product.getDefaultUnitPrice(),
                    product.getDescription(),
                    product.getStockQuantity(),
                    product.getIsFeatured(),
                    product.getIsFeatured2(),
                    product.getDisplayOrder()
            );
        }
    }

    public record PageProductResponse(
            List<ProductResponse> content,
            int pageNumber,
            int pageSize,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last,
            boolean empty
    ) {
        public static PageProductResponse from(Page<Product> page) {
            List<ProductResponse> content = page.getContent().stream()
                    .map(ProductResponse::from)
                    .toList();

            return new PageProductResponse(
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
    public record ProductLineInfo(
            Long id,
            String name
    ) {}

    public record ProductDetailResponse(
            Long id,
            ProductCategory productCategory,
            ProductLineInfo productLine,
            String productCode,
            String name,
            BigDecimal defaultUnitPrice,
            String description,
            Integer stockQuantity,
            List<PartMappingResponse> partMappings,
            boolean isFeatured,
            boolean isFeatured2,
            Integer displayOrder

    ) {
        public static ProductDetailResponse from(Product product) {
            return new ProductDetailResponse(
                    product.getId(),
                    product.getProductCategory(),
                    product.getProductLine() != null
                            ? new ProductLineInfo(
                            product.getProductLine().getId(),
                            product.getProductLine().getName()
                    )
                            : null,
                    product.getProductCode(),
                    product.getName(),
                    product.getDefaultUnitPrice(),
                    product.getDescription(),
                    product.getStockQuantity(),
                    product.getPartMappings().stream()
                            .map(PartMappingResponse::from)
                            .collect(Collectors.toList()),
                    product.getIsFeatured(),
                    product.getIsFeatured2(),
                    product.getDisplayOrder()
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
            Integer quantity,

            String note
    ) {}

    public record ProductUpdateRequest(
            ProductCategory productCategory,

            Long productLineId,

            @NotBlank(message = "제품명은 필수입니다")
            String name,

            BigDecimal defaultUnitPrice,

            String description,

            List<PartMappingRequest> partMappings
    ) {}
    public record StockQuantityUpdateRequest(
            @NotNull(message = "재고 수량은 필수입니다")
            Integer adjustmentQuantity,

            String note
    ) {}

    // 순서 변경 요청 DTO
    public record DisplayOrderUpdateRequest(
            @NotNull(message = "표시 순서는 필수입니다")
            Integer displayOrder
    ) {}

    // 여러 제품 순서 일괄 변경 요청 DTO
    public record BulkDisplayOrderUpdateRequest(
            @NotNull(message = "제품 순서 목록은 필수입니다")
            List<ProductOrderInfo> orders
    ) {}

    public record ProductOrderInfo(
            @NotNull(message = "제품 ID는 필수입니다")
            Long productId,

            @NotNull(message = "표시 순서는 필수입니다")
            Integer displayOrder
    ) {}

    public record MaxProducibleResponse(
            Long productId,
            Integer maxProducibleQuantity
    ) {}

    public record ProductionValidationRequest(
            Integer quantity
    ) {}

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
