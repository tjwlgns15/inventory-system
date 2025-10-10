package com.yhs.inventroysystem.presentation.sales;

import com.yhs.inventroysystem.domain.exchange.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
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
            BigDecimal totalAmount
    ) {}

    public record YearlySalesByClientResponse(
            Integer year,
            List<ClientSalesData> clientSales
    ) {}

    public record ClientSalesData(
            Long clientId,
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