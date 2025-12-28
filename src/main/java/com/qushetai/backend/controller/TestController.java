package com.qushetai.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/hello")
    public Map<String, Object> hello() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("success", true);
        response.put("message", "测试接口正常工作");
        response.put("timestamp", Instant.now().toString());

        Map<String, Object> data = new HashMap<>();
        data.put("service", "qushetai-backend");
        data.put("endpoint", "/test/hello");
        data.put("note", "这是一个测试接口，用于验证服务连通性");

        response.put("data", data);
        return response;
    }

    @GetMapping("/database")
    public Map<String, Object> testDatabase() {
        Map<String, Object> response = new HashMap<>();

        try {
            // 这里可以添加数据库连接测试
            // 暂时返回模拟数据
            Map<String, Object> data = new HashMap<>();
            data.put("databaseStatus", "UNKNOWN");
            data.put("message", "数据库连接测试待实现");
            data.put("suggestion", "请配置数据库连接并实现连接测试逻辑");

            response.put("code", 200);
            response.put("success", true);
            response.put("message", "数据库测试接口");
            response.put("timestamp", Instant.now().toString());
            response.put("data", data);

        } catch (Exception e) {
            response.put("code", 500);
            response.put("success", false);
            response.put("message", "数据库测试失败: " + e.getMessage());
            response.put("timestamp", Instant.now().toString());
            response.put("data", null);
        }

        return response;
    }

    @GetMapping("/endpoints")
    public Map<String, Object> listEndpoints() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("success", true);
        response.put("message", "可用测试端点列表");
        response.put("timestamp", Instant.now().toString());

        Map<String, Object> endpoints = new HashMap<>();
        endpoints.put("health", Map.of(
                "url", "/api/health",
                "method", "GET",
                "description", "基础健康检查"
        ));
        endpoints.put("detailedHealth", Map.of(
                "url", "/api/health/detailed",
                "method", "GET",
                "description", "详细健康检查"
        ));
        endpoints.put("hello", Map.of(
                "url", "/test/hello",
                "method", "GET",
                "description", "基础测试接口"
        ));
        endpoints.put("database", Map.of(
                "url", "/test/database",
                "method", "GET",
                "description", "数据库连接测试"
        ));

        response.put("data", endpoints);
        return response;
    }
}