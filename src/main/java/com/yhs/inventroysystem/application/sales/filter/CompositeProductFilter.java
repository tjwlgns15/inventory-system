package com.yhs.inventroysystem.application.sales.filter;

import com.yhs.inventroysystem.presentation.sales.SalesStatsDtos;
import com.yhs.inventroysystem.presentation.sales.SalesStatsDtos.ProductSalesData;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

public class CompositeProductFilter implements ProductFilterStrategy {

    private final List<ProductFilterStrategy> strategies;

    private CompositeProductFilter(List<ProductFilterStrategy> strategies) {
        this.strategies = strategies;
    }

    /**
     * 여러 필터를 순차 적용하는 복합 필터
     */
    public static CompositeProductFilter of(ProductFilterStrategy... strategies) {
        return new CompositeProductFilter(Arrays.asList(strategies));
    }


    @Override
    public List<ProductSalesData> filter(List<ProductSalesData> products) {
        List<ProductSalesData> result = products;

        for (ProductFilterStrategy strategy : strategies) {
            result = strategy.filter(result);

            // 중간에 결과가 비어버리면 더 이상 필터링 불필요
            if (result.isEmpty()) {
                break;
            }
        }

        return result;
    }

    @Override
    public String getStrategyName() {
        return this.getClass().getSimpleName() +
                "[" +
                String.join(" -> ", strategies.stream()
                        .map(ProductFilterStrategy::getStrategyName)
                        .toList()) +
                "]";
    }
}
