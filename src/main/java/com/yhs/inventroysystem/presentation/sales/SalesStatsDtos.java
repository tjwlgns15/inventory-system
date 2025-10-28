package com.yhs.inventroysystem.presentation.sales;

import com.yhs.inventroysystem.domain.exchange.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

public class SalesStatsDtos {

    public record WeeklySalesResponse(
            LocalDate weekStart,
            LocalDate weekEnd,
            List<ProductSalesData> productSales
    ) {}

    public record ProductSalesData(
            Long productId,
            String productCode,
            String productName,
            Integer quantity,
            BigDecimal totalAmount,
            BigDecimal totalAmountKRW  // 추가: 원화 환산 금액
    ) {}

    public record MonthlySalesResponse(
            YearMonth startMonth,      // 시작 월 (12개월 전)
            YearMonth endMonth,        // 종료 월 (현재 월)
            List<ProductMonthlySales> productSales
    ) {}

    public record ProductMonthlySales(
            Long productId,
            String productCode,
            String productName,
            List<MonthlySalesData> monthlySales,  // 12개월 데이터
            Integer totalQuantity,                 // 총 판매량
            BigDecimal totalAmount,                // 총 판매액
            BigDecimal totalAmountKRW              // 총 판매액 (원화)
    ) {}

    public record MonthlySalesData(
            YearMonth yearMonth,       // 2024-01
            Integer quantity,          // 해당 월 판매량
            BigDecimal amount,         // 해당 월 판매액
            BigDecimal amountKRW       // 해당 월 판매액 (원화)
    ) {}

    public record YearlySalesByClientResponse(
            Integer year,
            List<ClientSalesData> clientSales
    ) {}

    public record ClientSalesData(
            Long clientId,
            Long parentClientId,
            String clientCode,
            String clientName,
            String countryName,
            Currency currency,
            String currencySymbol,
            List<ProductSalesData> productSales,
            BigDecimal totalAmount,
            BigDecimal totalAmountKRW
    ) {}
}