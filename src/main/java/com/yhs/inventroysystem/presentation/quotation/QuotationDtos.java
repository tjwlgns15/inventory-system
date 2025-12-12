package com.yhs.inventroysystem.presentation.quotation;

import com.yhs.inventroysystem.application.quotation.QuotationCommands;
import com.yhs.inventroysystem.domain.exchange.Currency;
import com.yhs.inventroysystem.domain.quotation.Quotation;
import com.yhs.inventroysystem.domain.quotation.QuotationItem;
import com.yhs.inventroysystem.domain.quotation.QuotationType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class QuotationDtos {

    public record QuotationCreate(
            @NotNull(message = "구분 항목은 필수 입니다.")
            QuotationType quotationType,

            String companyName,
            String representativeName,

            @NotEmpty(message = "납품 항목은 필수입니다")
            @Valid
            List<QuotationItemDto> items,
            boolean isTax,
            Currency currency,
            String note,
            LocalDate orderedAt
    ) {
        public QuotationCommands.CreateCommand toCommand() {
            List<QuotationCommands.ItemCommand> itemCommands = items.stream()
                    .map(QuotationItemDto::toCommand)
                    .toList();

            return new QuotationCommands.CreateCommand(
                    quotationType,
                    companyName,
                    representativeName,
                    itemCommands,
                    isTax,
                    currency,
                    note,
                    orderedAt
            );
        }
    }

    public record QuotationItemDto(
            @NotNull(message = "제품명은 필수입니다")
            String productName,

            @NotNull(message = "수량은 필수입니다")
            @Positive(message = "수량은 0보다 커야 합니다")
            Integer quantity,

            @PositiveOrZero(message = "단가는 0 이상이어야 합니다")
            BigDecimal unitPrice
    ) {
        public QuotationCommands.ItemCommand toCommand() {
            return new QuotationCommands.ItemCommand(
                    productName,
                    quantity,
                    unitPrice
            );
        }
    }

    public record QuotationUpdate(
            QuotationType quotationType,
            String companyName,
            String representativeName,
            List<QuotationItemDto> items,
            boolean isTax,
            Currency currency,
            String note,
            LocalDate orderedAt
    ) {
        public QuotationCommands.UpdateCommand toCommand() {
            List<QuotationCommands.ItemCommand> itemCommands = items.stream()
                    .map(QuotationItemDto::toCommand)
                    .toList();

            return new QuotationCommands.UpdateCommand(
                    quotationType,
                    companyName,
                    representativeName,
                    itemCommands,
                    isTax,
                    currency,
                    note,
                    orderedAt
            );
        }

    }






    // ===== 응답 객체 ===== //
    public record PageQuotationResponse(
            List<QuotationResponse> content,
            int pageNumber,
            int pageSize,
            long totalElements,
            int totalPages,
            boolean first,
            boolean last,
            boolean empty
    ) {
        public static PageQuotationResponse from(Page<Quotation> page) {
            List<QuotationResponse> content = page.getContent().stream()
                    .map(QuotationResponse::from)
                    .toList();

            return new PageQuotationResponse(
                    content,
                    page.getNumber(),
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.isFirst(),
                    page.isLast(),
                    page.isEmpty()
            );
        }
    }

    public record QuotationResponse(
            Long id,
            String quotationNumber,
            QuotationType quotationType,
            String companyName,
            String representativeName,
            List<QuotationItemResponse> items,
            BigDecimal totalAmount,
            BigDecimal totalAfterTaxAmount,
            boolean isTax,
            Currency currency,
            String currencySymbol,
            String currencyName,
            String note,
            LocalDate orderedAt
    ) {
        public static QuotationResponse from(Quotation quotation) {
            return new QuotationResponse(
                    quotation.getId(),
                    quotation.getQuotationNumber(),
                    quotation.getQuotationType(),
                    quotation.getCompanyName(),
                    quotation.getRepresentativeName(),
                    quotation.getItems().stream()
                            .map(QuotationItemResponse::from)
                            .toList(),
                    quotation.getTotalAmount(),
                    quotation.getTotalAfterTaxAmount(),
                    quotation.isTax(),
                    quotation.getCurrency(),
                    quotation.getCurrency().getSymbol(),
                    quotation.getCurrency().getName(),
                    quotation.getNote(),
                    quotation.getOrderedAt()
            );
        }

    }

    public record QuotationItemResponse(
            Long id,
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal totalPrice
    ) {
        public static QuotationItemResponse from(QuotationItem item) {
            return new QuotationItemResponse(
                    item.getId(),
                    item.getProductName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getTotalPrice()
            );
        }
    }

}
