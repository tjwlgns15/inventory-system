package com.yhs.inventroysystem.application.sales.filter;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.util.List;

/**
 * 제품 표시 정책
 */
@Getter
@Builder
public class ProductDisplayPolicy {

    /**
     * 최대 표시 개수 (null이면 제한 없음)
     */
    private final Integer maxDisplayCount;

    /**
     * 거래 발생 제품만 표시할지 여부
     */
    @Builder.Default
    private final boolean onlyTransactedProducts = false;

    /**
     * 주요 제품 키워드 (제품명에 포함되어야 할 키워드들)
     * 이 키워드가 포함된 제품은 거래가 없어도 항상 표시
     */
    @Singular
    private final List<String> keyProductKeywords;

    /**
     * 정렬 기준
     */
    @Builder.Default
    private final SortCriteria sortCriteria = SortCriteria.QUANTITY_DESC;

    /**
     * 정렬 기준
     */
    public enum SortCriteria {
        QUANTITY_DESC,      // 판매량 내림차순
        QUANTITY_ASC,       // 판매량 오름차순
        AMOUNT_DESC,        // 판매액 내림차순
        AMOUNT_ASC          // 판매액 오름차순
    }

    /**
     * 주요 제품 키워드가 설정되어 있는지 확인
     */
    public boolean hasKeyProductKeywords() {
        return keyProductKeywords != null && !keyProductKeywords.isEmpty();
    }
}