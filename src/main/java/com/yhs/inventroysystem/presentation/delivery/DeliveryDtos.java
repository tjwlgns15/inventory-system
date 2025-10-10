package com.yhs.inventroysystem.presentation.delivery;

import com.yhs.inventroysystem.domain.delivery.Delivery;
import com.yhs.inventroysystem.domain.delivery.DeliveryItem;
import com.yhs.inventroysystem.domain.delivery.DeliveryStatus;
import com.yhs.inventroysystem.domain.exchange.Currency;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class DeliveryDtos {

    public record DeliveryCreateRequest(
            @NotNull(message = "거래처 ID는 필수입니다")
            Long clientId,

            @NotEmpty(message = "납품 항목은 필수입니다")
            @Valid
            List<DeliveryItemRequest> items
    ) {}


    public record DeliveryItemRequest(
            @NotNull(message = "제품 ID는 필수입니다")
            Long productId,

            @NotNull(message = "수량은 필수입니다")
            @Positive(message = "수량은 0보다 커야 합니다")
            Integer quantity
    ) {}


    public record DeliveryResponse(
            Long id,
            String deliveryNumber,
            Long clientId,
            String clientName,
            List<DeliveryItemResponse> items,
            DeliveryStatus status,
            BigDecimal totalAmount,
            Currency currency,
            String currencySymbol,
            String currencyName,
            BigDecimal exchangeRate,
            BigDecimal totalAmountKRW,
            LocalDateTime deliveredAt
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
                    delivery.getTotalAmount(),
                    currency,
                    currency.getSymbol(),
                    currency.getName(),
                    delivery.getExchangeRate(),
                    delivery.getTotalAmountKRW(),
                    delivery.getDeliveredAt()
            );
        }
    }


    public record DeliveryItemResponse(
            Long productId,
            String productName,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal totalPrice
    ) {
        public static DeliveryItemResponse from(DeliveryItem item) {
            return new DeliveryItemResponse(
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getTotalPrice()
            );
        }
    }
}
