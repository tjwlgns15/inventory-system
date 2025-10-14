package com.yhs.inventroysystem.presentation.part;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/parts")
public class PartViewController {

    @GetMapping
    public String partsPage() {
        return "part/parts";
    }

    @GetMapping("/transactions")
    public String partTransactionPage() {
        return "part/part-transactions";
    }
}
