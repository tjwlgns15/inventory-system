package com.yhs.inventroysystem.application.delivery;

import java.util.List;

public class DeliveryCommands {

    public record DeliveryCreateCommand(
            Long clientId,
            List<DeliveryItemInfo> items
    ) {}

    public record DeliveryItemInfo(
            Long productId,
            Integer quantity
    ) {}
}
