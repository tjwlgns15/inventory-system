package com.yhs.inventroysystem.application.price;

import java.math.BigDecimal;

public class PriceCommands {

    public record PriceRegisterCommand(
            Long clientId,
            Long productId,
            BigDecimal unitPrice
    ) {}

    public record PriceUpdateCommand(
            Long clientId,
            Long productId,
            BigDecimal newPrice
    ) {}
}
