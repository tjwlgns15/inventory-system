package com.yhs.inventroysystem.presentation.delivery;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/deliveries")
public class DeliveryViewController {

    @GetMapping
    public String deliveryPage() {
        return "delivery/deliveries";
    }
}
