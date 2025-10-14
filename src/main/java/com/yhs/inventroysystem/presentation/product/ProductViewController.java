package com.yhs.inventroysystem.presentation.product;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/products")
public class ProductViewController {

    @GetMapping
    public String productsPage() {
        return "product/products";
    }

    @GetMapping("/transactions")
    public String transactionsPage() {
        return "product/product-transactions";
    }

    @GetMapping("/visualization")
    public String summaryPage() {
        return "product/product-visualization";
    }
}
