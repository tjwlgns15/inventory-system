package com.yhs.inventroysystem.presentation.price;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/prices")
public class PriceViewController {

    @GetMapping
    public String pricesPage() {
        return "price/prices";
    }
}
