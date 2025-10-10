package com.yhs.inventroysystem.application.sales;

import com.yhs.inventroysystem.domain.delivery.Delivery;
import com.yhs.inventroysystem.domain.delivery.DeliveryRepository;
import com.yhs.inventroysystem.presentation.sales.SalesStatsDtos.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
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

        List<ProductSalesData> productSales = deliveryRepository
                .findWeeklySalesByProduct(startDateTime, endDateTime);

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

            // 첫 번째 납품에서 거래처 정보 가져오기
            Delivery firstDelivery = clientDeliveries.get(0);

            // 거래처별 제품 판매 조회
            List<ProductSalesData> productSales = deliveryRepository
                    .findYearlySalesByClientAndProduct(clientId, year);

            // 총액 계산 (외화 기준)
            BigDecimal totalAmount = clientDeliveries.stream()
                    .map(Delivery::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 총액 계산 (원화 기준)
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


        // 원화 환산 금액 기준으로 정렬
        clientSalesDataList.sort((a, b) -> b.totalAmountKRW().compareTo(a.totalAmountKRW()));

        return new YearlySalesByClientResponse(year, clientSalesDataList);
    }
}