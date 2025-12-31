package com.yhs.inventroysystem.presentation.shipment;

import com.yhs.inventroysystem.domain.shipment.entity.ShipmentType;
import com.yhs.inventroysystem.domain.shipment.entity.TradeTerms;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/shipments")
public class ShipmentViewController {

    @GetMapping
    public String shipmentsPage() {
        return "/shipment/shipments";
    }

    @GetMapping("/new")
    public String shipmentCreationForm(Model model) {
        model.addAttribute("shipmentTypes", ShipmentType.values());
        model.addAttribute("tradeTerms", TradeTerms.values());

        return "/shipment/new_shipment";
    }

    @GetMapping("/{shipmentId}")
    public String shipmentsDetailPage(@PathVariable Long shipmentId) {
        return "/shipment/detail_shipment";
    }

    @GetMapping("/{shipmentId}/edit")
    public String shipmentsEditPage(@PathVariable Long shipmentId) {
        return "/shipment/edit_shipment";
    }
}
