package com.yhs.inventroysystem.presentation.contract;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/contracts")
public class ContractViewController {

    @GetMapping
    public String contractsPage() {
        return "contract/contracts";
    }
}
