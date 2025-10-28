package com.yhs.inventroysystem.application.product;

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

public class ProductCommands {

    public record ProductRegisterCommand(
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
