package com.yhs.inventroysystem.application.delivery;

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
            Integer quantity
    ) {}
}
