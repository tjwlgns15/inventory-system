package com.yhs.inventroysystem.presentation.product;

import com.yhs.inventroysystem.domain.product.ProductStockTransaction;
import com.yhs.inventroysystem.domain.product.ProductTransactionType;

import java.time.LocalDateTime;

public class ProductTransactionDtos {

    public record ProductTransactionResponse(
            Long id,
            Long productId,
            String productCode,
            String productName,
            ProductTransactionType type,
            String typeDisplayName,
            Integer beforeStock,
            Integer changeQuantity,
            Integer afterStock,
            LocalDateTime createdAt
    ) {
        public static ProductTransactionResponse from(ProductStockTransaction productStockTransaction) {
            return new ProductTransactionResponse(
                    productStockTransaction.getId(),
                    productStockTransaction.getProduct().getId(),
                    productStockTransaction.getProduct().getProductCode(),
                    productStockTransaction.getProduct().getName(),
                    productStockTransaction.getType(),
                    productStockTransaction.getType().getDisplayName(),
                    productStockTransaction.getBeforeStock(),
                    productStockTransaction.getChangeQuantity(),
                    productStockTransaction.getAfterStock(),
                    productStockTransaction.getCreatedAt()
            );
        }
    }
}
