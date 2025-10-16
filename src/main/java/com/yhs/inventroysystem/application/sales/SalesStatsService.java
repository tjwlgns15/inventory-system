package com.yhs.inventroysystem.application.sales;

import com.yhs.inventroysystem.domain.delivery.Delivery;
import com.yhs.inventroysystem.domain.delivery.DeliveryItem;
import com.yhs.inventroysystem.domain.delivery.DeliveryRepository;
import com.yhs.inventroysystem.presentation.sales.SalesStatsDtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
}