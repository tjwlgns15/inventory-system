package com.yhs.inventroysystem.application.sales;

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

    public WeeklySalesResponse getThisWeekSales() {
        LocalDate now = LocalDate.now();
        LocalDate weekStart = now.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = now.with(DayOfWeek.SUNDAY);

        return getWeeklySales(weekStart, weekEnd);
    }

    public WeeklySalesResponse getLastWeekSales() {
        LocalDate now = LocalDate.now();
        LocalDate weekStart = now.minusWeeks(1).with(DayOfWeek.MONDAY);
        LocalDate weekEnd = now.minusWeeks(1).with(DayOfWeek.SUNDAY);

        return getWeeklySales(weekStart, weekEnd);
    }
    public WeeklySalesResponse getBeforeLastWeekSales() {
        LocalDate now = LocalDate.now();
        LocalDate weekStart = now.minusWeeks(2).with(DayOfWeek.MONDAY);
        LocalDate weekEnd = now.minusWeeks(2).with(DayOfWeek.SUNDAY);

        return getWeeklySales(weekStart, weekEnd);
    }

    private WeeklySalesResponse getWeeklySales(LocalDate weekStart, LocalDate weekEnd) {
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
                .toList();

        return new WeeklySalesResponse(weekStart, weekEnd, productSales);
    }


    public MonthlySalesResponse getMonthlySales() {
        YearMonth currentMonth = YearMonth.now();
        YearMonth startMonth = currentMonth.minusMonths(11); // 최근 12개월

        LocalDateTime startDate = startMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = currentMonth.atEndOfMonth().atTime(LocalTime.MAX);

        List<Delivery> deliveries = deliveryRepository.findCompletedDeliveriesByPeriod(startDate, endDate);

        // 제품별 월별 판매 데이터 집계
        Map<Long, Map<YearMonth, MonthlySalesAggregation>> productMonthlyMap = new HashMap<>();

        for (Delivery delivery : deliveries) {
            YearMonth deliveryMonth = YearMonth.from(delivery.getDeliveredAt());

            for (DeliveryItem item : delivery.getItems()) {
                Long productId = item.getProduct().getId();

                productMonthlyMap.putIfAbsent(productId, new HashMap<>());
                Map<YearMonth, MonthlySalesAggregation> monthlyMap = productMonthlyMap.get(productId);

                MonthlySalesAggregation agg = monthlyMap.computeIfAbsent(deliveryMonth,
                        k -> new MonthlySalesAggregation(
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

        // 모든 제품 정보 가져오기
        List<Product> allProducts = productRepository.findAllActive();

        // 그룹별 월별 데이터 집계
        Map<String, Map<YearMonth, MonthlySalesAggregation>> groupMonthlyMap = new HashMap<>();

        for (Product product : allProducts) {
            if (product.getIsFeatured()) {
                String groupKey;

                if (product.getProductLine() != null) {
                    // productLine이 있으면 그룹화
                    groupKey = product.getProductLine().getName();
                } else {
                    // productLine이 없으면 개별 표시
                    groupKey = "INDIVIDUAL_" + product.getId();
                }

                groupMonthlyMap.putIfAbsent(groupKey, new HashMap<>());
                Map<YearMonth, MonthlySalesAggregation> monthlyMap = groupMonthlyMap.get(groupKey);

                // 해당 제품의 월별 데이터를 그룹에 합산
                if (productMonthlyMap.containsKey(product.getId())) {
                    Map<YearMonth, MonthlySalesAggregation> productData = productMonthlyMap.get(product.getId());

                    for (Map.Entry<YearMonth, MonthlySalesAggregation> entry : productData.entrySet()) {
                        YearMonth month = entry.getKey();
                        MonthlySalesAggregation productAgg = entry.getValue();

                        MonthlySalesAggregation groupAgg = monthlyMap.computeIfAbsent(month,
                                k -> new MonthlySalesAggregation(null, groupKey, groupKey)
                        );

                        groupAgg.addQuantity(productAgg.quantity);
                        groupAgg.addTotalAmount(productAgg.totalAmount);
                        groupAgg.addTotalAmountKRW(productAgg.totalAmountKRW);
                    }
                }

                // 판매가 없는 월도 0으로 추가
                for (int i = 0; i < 12; i++) {
                    YearMonth month = startMonth.plusMonths(i);
                    monthlyMap.putIfAbsent(month,
                            new MonthlySalesAggregation(null, groupKey, groupKey));
                }
            }
        }

        // "기타" 그룹 계산
        Map<YearMonth, MonthlySalesAggregation> othersMonthlyMap = new HashMap<>();

        for (Map.Entry<Long, Map<YearMonth, MonthlySalesAggregation>> entry : productMonthlyMap.entrySet()) {
            Long productId = entry.getKey();
            Product product = allProducts.stream()
                    .filter(p -> p.getId().equals(productId))
                    .findFirst()
                    .orElse(null);

            // 주요 제품이 아니면 "기타"로 합산
            if (product == null || !product.getIsFeatured()) {
                for (Map.Entry<YearMonth, MonthlySalesAggregation> monthEntry : entry.getValue().entrySet()) {
                    YearMonth month = monthEntry.getKey();
                    MonthlySalesAggregation productAgg = monthEntry.getValue();

                    MonthlySalesAggregation othersAgg = othersMonthlyMap.computeIfAbsent(month,
                            k -> new MonthlySalesAggregation(null, "기타", "기타")
                    );

                    othersAgg.addQuantity(productAgg.quantity);
                    othersAgg.addTotalAmount(productAgg.totalAmount);
                    othersAgg.addTotalAmountKRW(productAgg.totalAmountKRW);
                }
            }
        }

        // "기타" 그룹 추가
        if (!othersMonthlyMap.isEmpty()) {
            groupMonthlyMap.put("기타", othersMonthlyMap);
        }

        // ProductMonthlySales 리스트 생성
        List<ProductMonthlySales> productSalesList = new ArrayList<>();

        for (Map.Entry<String, Map<YearMonth, MonthlySalesAggregation>> entry : groupMonthlyMap.entrySet()) {
            String groupKey = entry.getKey();
            Map<YearMonth, MonthlySalesAggregation> monthlyData = entry.getValue();

            // 12개월 데이터 생성
            List<MonthlySalesData> monthlySales = new ArrayList<>();
            for (int i = 0; i < 12; i++) {
                YearMonth month = startMonth.plusMonths(i);
                MonthlySalesAggregation agg = monthlyData.getOrDefault(month,
                        new MonthlySalesAggregation(null, groupKey, groupKey));

                monthlySales.add(new MonthlySalesData(
                        month,
                        agg.quantity,
                        agg.totalAmount,
                        agg.totalAmountKRW
                ));
            }

            // displayName 결정
            String displayName;
            boolean isGroup;

            if (groupKey.startsWith("INDIVIDUAL_")) {
                // 개별 제품
                Long productId = Long.parseLong(groupKey.replace("INDIVIDUAL_", ""));
                Product product = allProducts.stream()
                        .filter(p -> p.getId().equals(productId))
                        .findFirst()
                        .orElse(null);
                displayName = product != null ? product.getName() : groupKey;
                isGroup = false;
            } else {
                // 그룹 (productLine 또는 "기타")
                displayName = groupKey;
                isGroup = true;
            }

            productSalesList.add(new ProductMonthlySales(
                    displayName,
                    isGroup,
                    monthlySales
            ));
        }

        // "기타"를 마지막으로 정렬
        productSalesList.sort((a, b) -> {
            if (a.displayName().equals("기타")) return 1;
            if (b.displayName().equals("기타")) return -1;
            return a.displayName().compareTo(b.displayName());
        });

        return new MonthlySalesResponse(startMonth, currentMonth, productSalesList);
    }


    public YearlySalesByClientResponse getYearlySalesByClient(int year) {
        List<Delivery> deliveries = deliveryRepository.findCompletedDeliveriesByYear(year);

        Map<Long, List<Delivery>> deliveriesGroupByClient = deliveries.stream()
                .collect(Collectors.groupingBy(d -> d.getClient().getId()));

        List<ClientSalesData> clientSalesDataList = new ArrayList<>();

        for (Map.Entry<Long, List<Delivery>> entry : deliveriesGroupByClient.entrySet()) {
            Long clientId = entry.getKey();
            List<Delivery> clientDeliveries = entry.getValue();

            Delivery firstDelivery = clientDeliveries.get(0);

            // 거래처별 제품 판매 집계
            Map<Long, ProductSalesAggregation> productMap = new java.util.HashMap<>();

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
                    productSales,
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
     * 월별 판매 집계를 위한 내부 클래스
     */
    private static class MonthlySalesAggregation {
        Long productId;
        String productCode;
        String productName;
        Integer quantity = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalAmountKRW = BigDecimal.ZERO;

        MonthlySalesAggregation(Long productId, String productCode, String productName) {
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
}