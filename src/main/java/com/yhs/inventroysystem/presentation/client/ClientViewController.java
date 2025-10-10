package com.yhs.inventroysystem.presentation.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/clients")
public class ClientViewController {

    @GetMapping
    public String clientsPage() {
        return "client/clients";
    }
}
