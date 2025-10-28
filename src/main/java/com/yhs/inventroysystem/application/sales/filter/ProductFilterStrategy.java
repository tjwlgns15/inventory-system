package com.yhs.inventroysystem.application.sales.filter;

import com.yhs.inventroysystem.presentation.sales.SalesStatsDtos;

import java.util.List;

import static com.yhs.inventroysystem.presentation.sales.SalesStatsDtos.*;

/**
 * 제품 필터링 인터페이스
 */
public interface ProductFilterStrategy {

    /**
     * 제품 목록을 필터링
     *
     * @param products 필터링할 제품 목록
     * @return 필터링된 제품 목록
     */
    List<ProductSalesData> filter(List<ProductSalesData> products);

    /**
     * 전략 이름 반환
     */
    String getStrategyName();
}
