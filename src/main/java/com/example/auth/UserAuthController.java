package com.example.auth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserAuthController {
    @GetMapping("/api/login")
    public String login() {
        return "User authentication successful!";
    }
}
