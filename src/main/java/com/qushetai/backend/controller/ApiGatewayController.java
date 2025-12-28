package com.qushetai.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiGatewayController {

    // 提供 API 网关的根端点
    @GetMapping
    public Map<String, Object> apiRoot() {
        return Map.of(
                "success", true,
                "message", "Qushetai API Gateway",
                "endpoints", Map.of(
                        "activities", "/api/activities (转发到 /activities)",
                        "users", "/api/users/**",
                        "auth", "/api/auth/**",
                        "recommendations", "/api/recommendations/**"
                )
        );
    }

    // 或者更好的方式：创建一个代理服务来转发请求
}