package com.yhs.inventroysystem.application.sales.filter;

import com.yhs.inventroysystem.presentation.sales.SalesStatsDtos;
import com.yhs.inventroysystem.presentation.sales.SalesStatsDtos.ProductSalesData;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 주요 제품(U7~U9) 표시
 */
public class KeyProductOrTransactionFilter implements ProductFilterStrategy {

    private final Set<String> keyProductKeywords;
    private final TransactionOccurredFilter transactionFilter;

    private KeyProductOrTransactionFilter(Set<String> keyProductKeywords) {
        this.keyProductKeywords = keyProductKeywords;
        this.transactionFilter = new TransactionOccurredFilter();
    }

    /**
     * 주요 제품 키워드를 지정하여 필터 생성
     *
     * @param keywords 제품명에 포함되어야 할 키워드들 (대소문자 구분 없음)
     */
    public static KeyProductOrTransactionFilter withKeywords(String... keywords) {
        Set<String> uppercaseKeywords = Arrays.stream(keywords)
                .map(String::toUpperCase)
                .collect(Collectors.toSet());
        return new KeyProductOrTransactionFilter(uppercaseKeywords);
    }

    @Override
    public List<ProductSalesData> filter(List<ProductSalesData> products) {
        if (products == null || products.isEmpty()) {
            return List.of();
        }

        return products.stream()
                .filter(this::shouldInclude)
                .toList();
    }

    /**
     * 제품 포함 여부 판단
     * - 주요 제품 키워드가 포함되어 있거나
     * - 거래가 발생한 제품이면 포함
     */
    private boolean shouldInclude(ProductSalesData product) {
        // 1. 주요 제품인지 확인
        if (isKeyProduct(product)) {
            return true;
        }

        // 2. 거래가 발생했는지 확인
        return hasTransaction(product);
    }

    /**
     * 주요 제품 여부 확인 (제품명에 키워드 포함 여부)
     */
    private boolean isKeyProduct(ProductSalesData product) {
        if (product.productName() == null) {
            return false;
        }

        String upperProductName = product.productName().toUpperCase();

        return keyProductKeywords.stream()
                .anyMatch(upperProductName::contains);
    }

    /**
     * 거래 발생 여부 확인
     */
    private boolean hasTransaction(ProductSalesData product) {
        return product.quantity() != null
                && product.quantity() > 0
                && product.totalAmountKRW() != null
                && product.totalAmountKRW().compareTo(java.math.BigDecimal.ZERO) > 0;
    }

    @Override
    public String getStrategyName() {
        return "KeyProductOrTransactionFilter(keywords=" + keyProductKeywords + ")";
    }
}
