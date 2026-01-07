package com.yhs.inventroysystem.application.shipment;

import com.yhs.inventroysystem.domain.shipment.entity.ShipmentType;
import com.yhs.inventroysystem.domain.shipment.entity.TradeTerms;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ShipmentBoxCommand {

    public record ShipmentBoxCreateCommand(
            String title,
            BigDecimal width,
            BigDecimal length,
            BigDecimal height
    ) {
    }

    public record ShipmentBoxUpdateCommand(
            String title,
            BigDecimal width,
            BigDecimal length,
            BigDecimal height
    ) {
    }

}
