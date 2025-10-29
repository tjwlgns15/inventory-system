package com.yhs.inventroysystem.application.product;

import java.math.BigDecimal;
import java.util.List;

public class ProductLineCommands {

    public record PLRegisterCommand(
            String name
    ) {}

    public record PLUpdateCommand(
            String name
    ) {}

    public record PartMappingInfo(
            Long partId,
            Integer requiredQuantity
    ) {}

    public record ProductProduceCommand(
            Long productId,
            Integer quantity
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
