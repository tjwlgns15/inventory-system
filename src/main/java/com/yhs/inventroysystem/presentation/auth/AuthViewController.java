package com.yhs.inventroysystem.presentation.auth;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class AuthViewController {


    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

}
