package com.yhs.inventroysystem.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexViewController {

    private static final String LOGIN_REDIRECT_URL = "redirect:/auth/login";

    @GetMapping("/")
    public String index() {
        return LOGIN_REDIRECT_URL;
    }
}
