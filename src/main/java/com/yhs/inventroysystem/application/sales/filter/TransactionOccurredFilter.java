package com.yhs.inventroysystem.application.sales.filter;

import com.yhs.inventroysystem.presentation.sales.SalesStatsDtos;
import com.yhs.inventroysystem.presentation.sales.SalesStatsDtos.ProductSalesData;

import java.math.BigDecimal;
import java.util.List;

/**
 * 실거래 발생한 제품만 필터링
 */
public class TransactionOccurredFilter implements ProductFilterStrategy {
    @Override
    public List<ProductSalesData> filter(List<ProductSalesData> products) {
        if (products == null || products.isEmpty()) {
            return List.of();
        }

        return products.stream()
                .filter(this::hasTransaction)
                .toList();
    }

    // 수량, 판매액의 변화
    private boolean hasTransaction(ProductSalesData product) {
        return product.quantity() != null && product.quantity() > 0
                && product.totalAmountKRW() != null
                && product.totalAmountKRW().compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public String getStrategyName() {
        return this.getClass().getSimpleName();
    }
}
