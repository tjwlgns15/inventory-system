package com.yhs.inventroysystem.presentation.delivery;

import com.yhs.inventroysystem.domain.delivery.Delivery;
import com.yhs.inventroysystem.domain.delivery.DeliveryItem;
import com.yhs.inventroysystem.domain.delivery.DeliveryStatus;
import com.yhs.inventroysystem.domain.exchange.Currency;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class DeliveryDtos {

    public record DeliveryCreateRequest(
            @NotNull(message = "거래처 ID는 필수입니다")
            Long clientId,

            @NotEmpty(message = "납품 항목은 필수입니다")
            @Valid
            List<DeliveryItemRequest> items,

            LocalDate orderedAt,
            LocalDate requestedAt
    ) {
    }

    public record DeliveryItemRequest(
            @NotNull(message = "제품 ID는 필수입니다")
            Long productId,

            @NotNull(message = "수량은 필수입니다")
            @Positive(message = "수량은 0보다 커야 합니다")
            Integer quantity,

            @PositiveOrZero(message = "실제 단가는 0 이상이어야 합니다")
            BigDecimal actualUnitPrice,  // null이면 기준가 사용

            @Size(max = 200, message = "가격 메모는 200자 이내여야 합니다")
            String priceNote             // 가격 조정 사유
    ) {
    }

    public record DeliveryMemoUpdateRequest(
            @NotNull(message = "메모는 필수입니다")
            String memo
    ) {
    }

    public record DeliveryDiscountRequest(
            @NotNull(message = "할인액은 필수입니다")
            @PositiveOrZero(message = "할인액은 0 이상이어야 합니다")
            BigDecimal discountAmount,

            @Size(max = 200, message = "할인 사유는 200자 이내여야 합니다")
            String note
    ) {}

    public record DeliveryDiscountRateRequest(
            @NotNull(message = "할인율은 필수입니다")
            @DecimalMin(value = "0.0", message = "할인율은 0 이상이어야 합니다")
            @DecimalMax(value = "100.0", message = "할인율은 100 이하여야 합니다")
            BigDecimal discountRate,

            @Size(max = 200, message = "할인 사유는 200자 이내여야 합니다")
            String note
    ) {}

    public record DeliveryResponse(
            Long id,
            String deliveryNumber,
            Long clientId,
            String clientName,
            List<DeliveryItemResponse> items,
            DeliveryStatus status,
            BigDecimal subtotalAmount,
            BigDecimal totalDiscountAmount,
            String discountNote,
            BigDecimal totalAmount,
            Currency currency,
            String currencySymbol,
            String currencyName,
            BigDecimal exchangeRate,
            BigDecimal totalAmountKRW,
            LocalDate orderedAt,
            LocalDate requestedAt,
            LocalDateTime deliveredAt,
            String memo
    ) {
        public static DeliveryResponse from(Delivery delivery) {
            Currency currency = delivery.getClient().getCurrency();

            return new DeliveryResponse(
                    delivery.getId(),
                    delivery.getDeliveryNumber(),
                    delivery.getClient().getId(),
                    delivery.getClient().getName(),
                    delivery.getItems().stream()
                            .map(DeliveryItemResponse::from)
                            .collect(Collectors.toList()),
                    delivery.getStatus(),
                    delivery.getSubtotalAmount(),
                    delivery.getTotalDiscountAmount(),
                    delivery.getDiscountNote(),
                    delivery.getTotalAmount(),
                    currency,
                    currency.getSymbol(),
                    currency.getName(),
                    delivery.getExchangeRate(),
                    delivery.getTotalAmountKRW(),
                    delivery.getOrderedAt(),
                    delivery.getRequestedAt(),
                    delivery.getDeliveredAt(),
                    delivery.getMemo()
            );
        }
    }

    public record DeliveryItemResponse(
            Long itemId,
            Long productId,
            String productName,
            Integer quantity,
            BigDecimal baseUnitPrice,
            BigDecimal actualUnitPrice,
            BigDecimal discountAmount,
            String priceNote,
            BigDecimal totalPrice,
            Boolean isFreeItem,
            Boolean isDiscounted
    ) {
        public static DeliveryItemResponse from(DeliveryItem item) {
            return new DeliveryItemResponse(
                    item.getId(),
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getBaseUnitPrice(),
                    item.getActualUnitPrice(),
                    item.getDiscountAmount(),
                    item.getPriceNote(),
                    item.getTotalPrice(),
                    item.getIsFreeItem(),
                    item.isDiscounted()
            );
        }
    }
}
