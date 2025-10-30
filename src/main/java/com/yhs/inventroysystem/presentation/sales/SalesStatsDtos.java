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

    public record MonthlySalesData(
            YearMonth yearMonth,       // 2024-01
            Integer quantity,          // 해당 월 판매량
            BigDecimal amount,         // 해당 월 판매액
            BigDecimal amountKRW       // 해당 월 판매액 (원화)
    ) {}

    public record MonthlySalesResponse(
            YearMonth startMonth,
            YearMonth endMonth,
            List<ProductMonthlySales> productSales
    ) {}

    public record ProductMonthlySales(
            String displayName,        // 표시 이름 (제품명 또는 productLine 이름)
            boolean isGroup,           // 그룹 여부
            List<MonthlySalesData> monthlySales
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