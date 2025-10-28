package com.yhs.inventroysystem.application.sales.filter;

import java.util.ArrayList;
import java.util.List;

public class ProductFilterFactory {

    public static ProductFilterStrategy createFilter(ProductDisplayPolicy policy) {
        List<ProductFilterStrategy> strategies = new ArrayList<>();

        if (policy.hasKeyProductKeywords()) { // 주요 제품 + 거래 발생한 제품
            KeyProductOrTransactionFilter keyFilter =
                    KeyProductOrTransactionFilter.withKeywords(policy.getKeyProductKeywords().toArray(new String[0]));
            strategies.add(keyFilter);
        } else if (policy.isOnlyTransactedProducts()) { // 거래 발생 제품만
            strategies.add(new TransactionOccurredFilter());
        }

        // 필터가 없으면 모든 제품 통과시키는 필터 반환
        if (strategies.isEmpty()) {
            return new NoOpFilter();
        }

        // 필터가 1개면 그대로 반환
        if (strategies.size() == 1) {
            return strategies.get(0);
        }

        // 여러 필터를 조합
        return CompositeProductFilter.of(strategies.toArray(new ProductFilterStrategy[0]));
    }

    /**
     * 아무 필터링도 하지 않는 필터 (모든 제품 통과)
     */
    private static class NoOpFilter implements ProductFilterStrategy {
        @Override
        public List<com.yhs.inventroysystem.presentation.sales.SalesStatsDtos.ProductSalesData> filter(
                List<com.yhs.inventroysystem.presentation.sales.SalesStatsDtos.ProductSalesData> products) {
            return products;
        }

        @Override
        public String getStrategyName() {
            return "NoOpFilter";
        }
    }
}
