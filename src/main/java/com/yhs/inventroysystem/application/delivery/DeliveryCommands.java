package com.yhs.inventroysystem.application.delivery;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class DeliveryCommands {

    public record DeliveryCreateCommand(
            Long clientId,
            List<DeliveryItemInfo> items,
            LocalDate orderedAt,
            LocalDate requestedAt
    ) {}

    public record DeliveryItemInfo(
            Long productId,
            Integer quantity,
            BigDecimal actualUnitPrice, // null: 기준 거래 가격 사용
            String priceNote
    ) {
        // 기준가 사용
        public DeliveryItemInfo(Long productId, Integer quantity) {
            this(productId, quantity, null, null);
        }
    }

    public record DeliveryDiscountCommand(
            BigDecimal discountAmount,  // 할인액
            String note                 // 할인 사유
    ) {}

    public record DeliveryDiscountRateCommand(
            BigDecimal discountRate,    // 할인율 (%)
            String note                 // 할인 사유
    ) {}

    public record DeliveryItemPriceAdjustCommand(
            Long deliveryId,
            Long itemId,
            BigDecimal newActualPrice,
            String note
    ) {}
}
