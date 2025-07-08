package com.yhs.inventroysystem.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/parts")
    public String partsPage() {
        return "parts.html";
    }

    @GetMapping("/products")
    public String productsPage() {
        return "products.html";
    }

    @GetMapping({"/dashboard"})
    public String dashboardPage() {
        return "inventory_dashboard.html";
    }

    @GetMapping("/")
    public String taskPage() {
        return "task.html";
    }
}
