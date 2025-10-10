package com.yhs.inventroysystem.presentation.sales;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sales")
public class SalesViewController {

    @GetMapping
    public String salesPage() {
        return "dashboard/sales";
    }
}
