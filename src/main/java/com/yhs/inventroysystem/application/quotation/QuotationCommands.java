package com.yhs.inventroysystem.application.quotation;

import com.yhs.inventroysystem.domain.exchange.Currency;
import com.yhs.inventroysystem.domain.quotation.QuotationType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class QuotationCommands {

    public record CreateCommand(
            QuotationType quotationType,
            String companyName,
            String representativeName,
            List<ItemCommand> items,
            boolean isTax,
            Currency currency,
            String note,
            LocalDate orderedAt
    ) {}

    public record ItemCommand(
            String productName,
            Integer quantity,
            BigDecimal unitPrice
    ) {}

    public record UpdateCommand(
            QuotationType quotationType,
            String companyName,
            String representativeName,
            List<ItemCommand> items,
            boolean isTax,
            Currency currency,
            String note,
            LocalDate orderedAt
    ) {}
}
