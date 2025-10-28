package com.yhs.inventroysystem.presentation.sales;

import com.yhs.inventroysystem.application.sales.SalesStatsService;
import com.yhs.inventroysystem.application.sales.filter.ProductDisplayPolicy;
import com.yhs.inventroysystem.application.sales.filter.ProductDisplayPolicy.ProductDisplayPolicyBuilder;
import com.yhs.inventroysystem.presentation.sales.SalesStatsDtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales-stats")
@RequiredArgsConstructor
public class SalesStatsRestController {

    private final SalesStatsService salesStatsService;

//    @GetMapping("/weekly/current")
//    public ResponseEntity<WeeklySalesResponse> getThisWeekSales() {
//        return ResponseEntity.ok(salesStatsService.getThisWeekSales());
//    }
//
//    @GetMapping("/weekly/last")
//    public ResponseEntity<WeeklySalesResponse> getLastWeekSales() {
//        return ResponseEntity.ok(salesStatsService.getLastWeekSales());
//    }
//
//    @GetMapping("/yearly/{year}")
//    public ResponseEntity<YearlySalesByClientResponse> getYearlySalesByClient(@PathVariable int year) {
//        return ResponseEntity.ok(salesStatsService.getYearlySalesByClient(year));
//    }

    /**
     * 이번 주 판매 현황 조회
     *
     * @param maxCount 최대 표시 개수 (null이면 제한 없음)
     * @param keyProducts 항상 표시할 제품 키워드 (기본값: U7, U8, U9)
     * @param onlyTransacted 거래 발생 제품만 표시 (keyProducts가 없을 때만 적용)
     */
    @GetMapping("/weekly/current")
    public ResponseEntity<WeeklySalesResponse> getThisWeekSales(
            @RequestParam(required = false) Integer maxCount,
            @RequestParam(required = false) List<String> keyProducts,
            @RequestParam(defaultValue = "false") boolean onlyTransacted) {

        ProductDisplayPolicy policy = buildPolicy(maxCount, keyProducts, onlyTransacted);
        return ResponseEntity.ok(salesStatsService.getThisWeekSales(policy));
    }

    /**
     * 지난 주 판매 현황 조회
     *
     * @param maxCount 최대 표시 개수 (null이면 제한 없음)
     * @param keyProducts 항상 표시할 제품 키워드 (기본값: U7, U8, U9)
     * @param onlyTransacted 거래 발생 제품만 표시 (keyProducts가 없을 때만 적용)
     */
    @GetMapping("/weekly/last")
    public ResponseEntity<WeeklySalesResponse> getLastWeekSales(
            @RequestParam(required = false) Integer maxCount,
            @RequestParam(required = false) List<String> keyProducts,
            @RequestParam(defaultValue = "false") boolean onlyTransacted) {

        ProductDisplayPolicy policy = buildPolicy(maxCount, keyProducts, onlyTransacted);
        return ResponseEntity.ok(salesStatsService.getLastWeekSales(policy));
    }

    @GetMapping("/monthly")
    public ResponseEntity<MonthlySalesResponse> getMonthlySales() {
        ProductDisplayPolicy policy = defaultPolicy();
        return ResponseEntity.ok(salesStatsService.getMonthlySales(policy));
    }

    /**
     * 연간 거래처별 판매 현황 조회
     *
     * @param year 조회 연도
     * @param maxCount 최대 표시 개수 (null이면 제한 없음)
     * @param keyProducts 항상 표시할 제품 키워드 (기본값: U7, U8, U9)
     * @param onlyTransacted 거래 발생 제품만 표시 (keyProducts가 없을 때만 적용)
     */
    @GetMapping("/yearly/{year}")
    public ResponseEntity<YearlySalesByClientResponse> getYearlySalesByClient(
            @PathVariable int year,
            @RequestParam(required = false) Integer maxCount,
            @RequestParam(required = false) List<String> keyProducts,
            @RequestParam(defaultValue = "false") boolean onlyTransacted) {

        ProductDisplayPolicy policy = buildPolicy(maxCount, keyProducts, onlyTransacted);
        return ResponseEntity.ok(salesStatsService.getYearlySalesByClient(year, policy));
    }

    /**
     * ProductDisplayPolicy 빌더 헬퍼 메서드
     */
    private ProductDisplayPolicy buildPolicy(Integer maxCount, List<String> keyProducts, boolean onlyTransacted) {
        ProductDisplayPolicyBuilder builder = ProductDisplayPolicy.builder()
                .maxDisplayCount(maxCount)
                .sortCriteria(ProductDisplayPolicy.SortCriteria.QUANTITY_DESC);

        boolean hasValidKeyProducts = keyProducts != null
                && !keyProducts.isEmpty()
                && keyProducts.stream().anyMatch(k -> k != null && !k.trim().isEmpty());

        if (hasValidKeyProducts) {
            keyProducts.stream()
                    .filter(k -> k != null && !k.trim().isEmpty())
                    .forEach(builder::keyProductKeyword);
        } else {
            builder.keyProductKeyword("U7")
                    .keyProductKeyword("U8")
                    .keyProductKeyword("U9");
        }

        if (!hasValidKeyProducts) {
            builder.onlyTransactedProducts(onlyTransacted);
        }

        return builder.build();
    }

    private ProductDisplayPolicy defaultPolicy() {
        return ProductDisplayPolicy.builder()
                .sortCriteria(ProductDisplayPolicy.SortCriteria.QUANTITY_DESC)
                .onlyTransactedProducts(true) // 거래 발생 제품만
                .keyProductKeyword("U7")
                .keyProductKeyword("U8")
                .keyProductKeyword("U9")
                .build();
    }
}