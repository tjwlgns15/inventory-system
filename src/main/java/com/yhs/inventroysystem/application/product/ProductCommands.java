package com.yhs.inventroysystem.application.product;

import com.yhs.inventroysystem.domain.product.ProductCategory;

import java.math.BigDecimal;
import java.util.List;

public class ProductCommands {

    public record ProductRegisterCommand(
            ProductCategory productCategory,
            Long productLineId,
            String productCode,
            String name,
            BigDecimal defaultUnitPrice,
            String description,
            Integer initialStock,
            List<PartMappingInfo> partMappings
    ) {}


    public record PartMappingInfo(
            Long partId,
            Integer requiredQuantity
    ) {}

    public record ProductProduceCommand(
            Long productId,
            Integer quantity
    ) {}

    public record ProductUpdateCommand(
            ProductCategory productCategory,
            Long productLineId,
            String name,
            BigDecimal defaultUnitPrice,
            String description,
            List<PartMappingInfo> partMappings
    ) {}


    public record InsufficientPartDetail(
            Long partId,
            String partName,
            String partCode,
            Integer requiredPerProduct,
            Integer totalRequired,
            Integer availableStock,
            Integer shortage
    ) {}
}
