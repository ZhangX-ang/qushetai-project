package com.qushetai.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthCheckController {

    @GetMapping("/api/health")
    public Map<String, Object> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("success", true);
        response.put("message", "服务运行正常");
        response.put("timestamp", Instant.now().toString());

        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("service", "qushetai-backend");
        data.put("version", "1.0.0");
        data.put("environment", "development");

        response.put("data", data);
        return response;
    }

    // 兼容旧版 /health 端点
    @GetMapping("/health")
    public Map<String, Object> legacyHealthCheck() {
        return healthCheck();
    }

    // 添加更详细的健康检查
    @GetMapping("/api/health/detailed")
    public Map<String, Object> detailedHealthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("success", true);
        response.put("message", "详细健康检查完成");
        response.put("timestamp", Instant.now().toString());

        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("service", "qushetai-backend");
        data.put("version", "1.0.0");
        data.put("environment", "development");

        // 系统信息
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("javaVersion", System.getProperty("java.version"));
        systemInfo.put("osName", System.getProperty("os.name"));
        systemInfo.put("memoryUsage", getMemoryUsage());
        data.put("systemInfo", systemInfo);

        // 服务状态
        Map<String, Object> serviceStatus = new HashMap<>();
        serviceStatus.put("database", "UNKNOWN"); // 需要数据库连接检查
        serviceStatus.put("redis", "UNKNOWN");    // 需要Redis连接检查
        serviceStatus.put("externalApis", "UNKNOWN");
        data.put("serviceStatus", serviceStatus);

        response.put("data", data);
        return response;
    }

    private String getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        double usagePercentage = (double) usedMemory / maxMemory * 100;
        return String.format("%.2f%% (%d MB / %d MB)",
                usagePercentage,
                usedMemory / (1024 * 1024),
                maxMemory / (1024 * 1024));
    }
}