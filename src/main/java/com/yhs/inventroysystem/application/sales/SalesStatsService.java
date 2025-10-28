package com.yhs.inventroysystem.application.sales;

import com.yhs.inventroysystem.application.sales.filter.ProductDisplayPolicy;
import com.yhs.inventroysystem.application.sales.filter.ProductFilterFactory;
import com.yhs.inventroysystem.application.sales.filter.ProductFilterStrategy;
import com.yhs.inventroysystem.domain.delivery.Delivery;
import com.yhs.inventroysystem.domain.delivery.DeliveryItem;
import com.yhs.inventroysystem.domain.delivery.DeliveryRepository;
import com.yhs.inventroysystem.domain.product.Product;
import com.yhs.inventroysystem.domain.product.ProductRepository;
import com.yhs.inventroysystem.presentation.sales.SalesStatsDtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalesStatsService {

    private final DeliveryRepository deliveryRepository;
    private final ProductRepository productRepository;

    /**
     * 이번 주 판매 현황 조회 (커스텀 정책 적용)
     */
    public WeeklySalesResponse getThisWeekSales(ProductDisplayPolicy policy) {
        LocalDate now = LocalDate.now();
        LocalDate weekStart = now.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = now.with(DayOfWeek.SUNDAY);

        return getWeeklySales(weekStart, weekEnd, policy);
    }

    /**
     * 지난 주 판매 현황 조회 (커스텀 정책 적용)
     */
    public WeeklySalesResponse getLastWeekSales(ProductDisplayPolicy policy) {
        LocalDate now = LocalDate.now();
        LocalDate weekStart = now.minusWeeks(1).with(DayOfWeek.MONDAY);
        LocalDate weekEnd = now.minusWeeks(1).with(DayOfWeek.SUNDAY);

        return getWeeklySales(weekStart, weekEnd, policy);
    }

    /**
     * 주간 판매 현황 조회 (필터링 적용)
     */
    private WeeklySalesResponse getWeeklySales(LocalDate weekStart, LocalDate weekEnd, ProductDisplayPolicy policy) {
        LocalDateTime startDateTime = weekStart.atStartOfDay();
        LocalDateTime endDateTime = weekEnd.atTime(LocalTime.MAX);

        List<Delivery> deliveries = deliveryRepository.findWeeklySales(startDateTime, endDateTime);

        // 제품별로 그룹핑하여 집계
        Map<Long, ProductSalesAggregation> productMap = new HashMap<>();

        for (Delivery delivery : deliveries) {
            for (DeliveryItem item : delivery.getItems()) {
                Long productId = item.getProduct().getId();

                ProductSalesAggregation agg = productMap.computeIfAbsent(productId,
                        k -> new ProductSalesAggregation(
                                productId,
                                item.getProduct().getProductCode(),
                                item.getProduct().getName()
                        )
                );

                agg.addQuantity(item.getQuantity());
                agg.addTotalAmount(item.getTotalPrice());

                // 원화 환산 금액 계산
                BigDecimal itemAmountKRW = calculateItemAmountKRW(item, delivery);
                agg.addTotalAmountKRW(itemAmountKRW);
            }
        }

        List<ProductSalesData> productSales = productMap.values().stream()
                .map(agg -> new ProductSalesData(
                        agg.productId,
                        agg.productCode,
                        agg.productName,
                        agg.quantity,
                        agg.totalAmount,
                        agg.totalAmountKRW
                ))
                .sorted((a, b) -> b.quantity().compareTo(a.quantity()))
                .collect(Collectors.toList());

        List<String> keyKeywords = policy.getKeyProductKeywords();
        if (keyKeywords != null && !keyKeywords.isEmpty()) {
            for (String keyword : keyKeywords) {
                List<Product> keyProducts = productRepository.findByNameContainingIgnoreCase(keyword);
                for (Product p : keyProducts) {
                    boolean exists = productSales.stream()
                            .anyMatch(ps -> ps.productId().equals(p.getId()));
                    if (!exists) {
                        productSales.add(new ProductSalesData(
                                p.getId(),
                                p.getProductCode(),
                                p.getName(),
                                0,
                                BigDecimal.ZERO,
                                BigDecimal.ZERO
                        ));
                    }
                }
            }
        }

        // 필터링 적용
        ProductFilterStrategy filter = ProductFilterFactory.createFilter(policy);
        List<ProductSalesData> filteredProducts = filter.filter(productSales);

        return new WeeklySalesResponse(weekStart, weekEnd, filteredProducts);
    }

    public MonthlySalesResponse getMonthlySales(ProductDisplayPolicy policy) {
        YearMonth currentMonth = YearMonth.now();
        YearMonth startMonth = currentMonth.minusMonths(11);

        LocalDateTime startDate = startMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = currentMonth.atEndOfMonth().atTime(LocalTime.MAX);

        List<Delivery> deliveries = deliveryRepository.findCompletedDeliveriesByPeriod(startDate, endDate);
        Map<Long, ProductMonthlySalesAggregation> productMap = new HashMap<>();

        for (Delivery delivery : deliveries) {
            YearMonth deliveryMonth = YearMonth.from(delivery.getDeliveredAt());

            for (DeliveryItem item : delivery.getItems()) {
                Long productId = item.getProduct().getId();

                ProductMonthlySalesAggregation agg = productMap.computeIfAbsent(productId,
                        k -> new ProductMonthlySalesAggregation(
                                productId,
                                item.getProduct().getProductCode(),
                                item.getProduct().getName(),
                                startMonth,
                                currentMonth
                        )
                );

                BigDecimal itemAmountKRW = calculateItemAmountKRW(item, delivery);
                agg.addMonthlySales(deliveryMonth, item.getQuantity(), item.getTotalPrice(), itemAmountKRW);
            }
        }

        final List<String> keywords;
        if (policy.getKeyProductKeywords() != null && !policy.getKeyProductKeywords().isEmpty()) {
            keywords = policy.getKeyProductKeywords();
        } else {
            keywords = List.of("U7", "U8", "U9");  // 기본값
        }

        // 3. DB에서 주요 제품 전체 조회
        List<Product> keyProducts = productRepository.findAll().stream()
                .filter(product -> {
                    String productName = product.getName();
                    if (productName == null) return false;

                    String upperProductName = productName.toUpperCase();
                    return keywords.stream()
                            .anyMatch(keyword -> upperProductName.contains(keyword.toUpperCase()));
                })
                .toList();

        List<ProductMonthlySales> productMonthlySalesList = new ArrayList<>();

        for (Product product : keyProducts) {
            ProductMonthlySalesAggregation agg = productMap.get(product.getId());

            if (agg == null) {
                agg = new ProductMonthlySalesAggregation(
                        product.getId(),
                        product.getProductCode(),
                        product.getName(),
                        startMonth,
                        currentMonth
                );
            }

            productMonthlySalesList.add(new ProductMonthlySales(
                    agg.productId,
                    agg.productCode,
                    agg.productName,
                    agg.getMonthlySalesDataList(),
                    agg.getTotalQuantity(),
                    agg.getTotalAmount(),
                    agg.getTotalAmountKRW()
            ));
        }

        productMonthlySalesList.sort((a, b) -> b.totalQuantity().compareTo(a.totalQuantity()));

        return new MonthlySalesResponse(startMonth, currentMonth, productMonthlySalesList);
    }

    /**
     * 연간 거래처별 판매 현황 조회 (커스텀 정책 적용)
     */
    public YearlySalesByClientResponse getYearlySalesByClient(int year, ProductDisplayPolicy policy) {
        List<Delivery> deliveries = deliveryRepository.findCompletedDeliveriesByYear(year);

        Map<Long, List<Delivery>> deliveriesGroupByClient = deliveries.stream()
                .collect(Collectors.groupingBy(d -> d.getClient().getId()));

        List<ClientSalesData> clientSalesDataList = new ArrayList<>();

        ProductFilterStrategy filter = ProductFilterFactory.createFilter(policy);

        for (Map.Entry<Long, List<Delivery>> entry : deliveriesGroupByClient.entrySet()) {
            Long clientId = entry.getKey();
            List<Delivery> clientDeliveries = entry.getValue();

            Delivery firstDelivery = clientDeliveries.get(0);

            // 거래처별 제품 판매 집계
            Map<Long, ProductSalesAggregation> productMap = new HashMap<>();

            for (Delivery delivery : clientDeliveries) {
                for (DeliveryItem item : delivery.getItems()) {
                    Long productId = item.getProduct().getId();

                    ProductSalesAggregation agg = productMap.computeIfAbsent(productId,
                            k -> new ProductSalesAggregation(
                                    productId,
                                    item.getProduct().getProductCode(),
                                    item.getProduct().getName()
                            )
                    );

                    agg.addQuantity(item.getQuantity());
                    agg.addTotalAmount(item.getTotalPrice());

                    BigDecimal itemAmountKRW = calculateItemAmountKRW(item, delivery);
                    agg.addTotalAmountKRW(itemAmountKRW);
                }
            }

            List<ProductSalesData> productSales = productMap.values().stream()
                    .map(agg -> new ProductSalesData(
                            agg.productId,
                            agg.productCode,
                            agg.productName,
                            agg.quantity,
                            agg.totalAmount,
                            agg.totalAmountKRW
                    ))
                    .sorted((a, b) -> b.quantity().compareTo(a.quantity()))
                    .toList();

            // 필터링 적용
            List<ProductSalesData> filteredProducts = filter.filter(productSales);

            // 총액 계산
            BigDecimal totalAmount = clientDeliveries.stream()
                    .map(Delivery::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal totalAmountKRW = clientDeliveries.stream()
                    .map(d -> d.getTotalAmountKRW() != null ? d.getTotalAmountKRW() : d.getTotalAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            ClientSalesData clientData = new ClientSalesData(
                    clientId,
                    firstDelivery.getClient().getParentClient() != null
                            ? firstDelivery.getClient().getParentClient().getId()
                            : null,
                    firstDelivery.getClient().getClientCode(),
                    firstDelivery.getClient().getName(),
                    firstDelivery.getClient().getCountry() != null
                            ? firstDelivery.getClient().getCountry().getName()
                            : "-",
                    firstDelivery.getClient().getCurrency(),
                    firstDelivery.getClient().getCurrency().getSymbol(),
                    filteredProducts,
                    totalAmount,
                    totalAmountKRW
            );

            clientSalesDataList.add(clientData);
        }

        clientSalesDataList.sort((a, b) -> b.totalAmountKRW().compareTo(a.totalAmountKRW()));

        return new YearlySalesByClientResponse(year, clientSalesDataList);
    }

    /**
     * DeliveryItem의 원화 환산 금액 계산
     */
    private BigDecimal calculateItemAmountKRW(DeliveryItem item, Delivery delivery) {
        if (delivery.getTotalAmountKRW() != null &&
                delivery.getTotalAmount() != null &&
                delivery.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {

            // 비율 계산: item.totalPrice / delivery.totalAmount
            BigDecimal ratio = item.getTotalPrice()
                    .divide(delivery.getTotalAmount(), 10, RoundingMode.HALF_UP);

            // 원화 환산: delivery.totalAmountKRW * ratio
            return delivery.getTotalAmountKRW()
                    .multiply(ratio)
                    .setScale(0, RoundingMode.HALF_UP);
        }

        // 환율 정보가 없으면 원래 금액 반환
        return item.getTotalPrice();
    }

    /**
     * 제품별 판매 집계를 위한 내부 클래스
     */
    private static class ProductSalesAggregation {
        Long productId;
        String productCode;
        String productName;
        Integer quantity = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalAmountKRW = BigDecimal.ZERO;

        ProductSalesAggregation(Long productId, String productCode, String productName) {
            this.productId = productId;
            this.productCode = productCode;
            this.productName = productName;
        }

        void addQuantity(Integer qty) {
            this.quantity += qty;
        }

        void addTotalAmount(BigDecimal amount) {
            this.totalAmount = this.totalAmount.add(amount);
        }

        void addTotalAmountKRW(BigDecimal amountKRW) {
            this.totalAmountKRW = this.totalAmountKRW.add(amountKRW);
        }
    }

    /**
     * 제품별 월별 판매 집계 헬퍼 클래스
     */
    private static class ProductMonthlySalesAggregation {
        private final Long productId;
        private final String productCode;
        private final String productName;
        private final Map<YearMonth, MonthlySalesAggregation> monthlyMap;
        private final YearMonth startMonth;
        private final YearMonth endMonth;

        public ProductMonthlySalesAggregation(Long productId, String productCode, String productName,
                                              YearMonth startMonth, YearMonth endMonth) {
            this.productId = productId;
            this.productCode = productCode;
            this.productName = productName;
            this.startMonth = startMonth;
            this.endMonth = endMonth;
            this.monthlyMap = new HashMap<>();

            // 12개월 초기화 (데이터 없는 월도 0으로 표시)
            YearMonth current = startMonth;
            while (!current.isAfter(endMonth)) {
                monthlyMap.put(current, new MonthlySalesAggregation());
                current = current.plusMonths(1);
            }
        }

        public void addMonthlySales(YearMonth month, Integer quantity, BigDecimal amount, BigDecimal amountKRW) {
            MonthlySalesAggregation monthData = monthlyMap.get(month);
            if (monthData != null) {
                monthData.addQuantity(quantity);
                monthData.addAmount(amount);
                monthData.addAmountKRW(amountKRW);
            }
        }

        public Integer getTotalQuantity() {
            return monthlyMap.values().stream()
                    .mapToInt(m -> m.quantity)
                    .sum();
        }

        public BigDecimal getTotalAmount() {
            return monthlyMap.values().stream()
                    .map(m -> m.amount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        public BigDecimal getTotalAmountKRW() {
            return monthlyMap.values().stream()
                    .map(m -> m.amountKRW)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        public List<MonthlySalesData> getMonthlySalesDataList() {
            List<MonthlySalesData> result = new ArrayList<>();
            YearMonth current = startMonth;

            while (!current.isAfter(endMonth)) {
                MonthlySalesAggregation data = monthlyMap.get(current);
                result.add(new MonthlySalesData(
                        current,
                        data.quantity,
                        data.amount,
                        data.amountKRW
                ));
                current = current.plusMonths(1);
            }

            return result;
        }
    }

    /**
     * 월별 판매 집계 헬퍼 클래스
     */
    private static class MonthlySalesAggregation {
        private Integer quantity = 0;
        private BigDecimal amount = BigDecimal.ZERO;
        private BigDecimal amountKRW = BigDecimal.ZERO;

        public void addQuantity(Integer qty) {
            this.quantity += qty;
        }

        public void addAmount(BigDecimal amt) {
            this.amount = this.amount.add(amt);
        }

        public void addAmountKRW(BigDecimal amt) {
            this.amountKRW = this.amountKRW.add(amt);
        }
    }
}