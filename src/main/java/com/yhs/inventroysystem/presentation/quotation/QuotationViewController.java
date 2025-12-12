package com.yhs.inventroysystem.presentation.quotation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/quotations")
public class QuotationViewController {

    @GetMapping
    public String quotationPage() {
        return "quotation/quotations";
    }
}
