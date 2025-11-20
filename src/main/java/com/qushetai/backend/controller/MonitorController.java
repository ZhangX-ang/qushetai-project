package com.qushetai.backend.controller;

import com.qushetai.backend.service.DataMonitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/monitor")
public class MonitorController {

    @Autowired
    private DataMonitorService dataMonitorService;

    /**
     * 获取数据概览
     */
    @GetMapping("/overview")
    public ResponseEntity<?> getDataOverview() {
        Map<String, Object> result = dataMonitorService.getDataOverview();
        return ResponseEntity.ok(result);
    }

    /**
     * 获取推荐系统健康状态
     */
    @GetMapping("/recommendation-health")
    public ResponseEntity<?> getRecommendationHealth() {
        Map<String, Object> result = dataMonitorService.getRecommendationHealth();
        return ResponseEntity.ok(result);
    }

    /**
     * 生成测试数据（开发用）- POST版本
     */
    @PostMapping("/generate-test-data")
    public ResponseEntity<?> generateTestData() {
        Map<String, Object> result = dataMonitorService.generateTestData();
        return ResponseEntity.ok(result);
    }

    /**
     * 生成测试数据（开发用）- GET版本（用于浏览器测试）
     */
    @GetMapping("/generate-test-data-get")
    public ResponseEntity<?> generateTestDataGet() {
        Map<String, Object> result = dataMonitorService.generateTestData();
        return ResponseEntity.ok(result);
    }

    /**
     * 清除测试数据（开发用）- DELETE版本
     */
    @DeleteMapping("/clear-test-data")
    public ResponseEntity<?> clearTestData() {
        Map<String, Object> result = dataMonitorService.clearTestData();
        return ResponseEntity.ok(result);
    }

    /**
     * 清除测试数据（开发用）- GET版本（用于浏览器测试）
     */
    @GetMapping("/clear-test-data-get")
    public ResponseEntity<?> clearTestDataGet() {
        Map<String, Object> result = dataMonitorService.clearTestData();
        return ResponseEntity.ok(result);
    }

    /**
     * 测试接口（GET方法，用于浏览器测试）
     */
    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "监控接口测试成功",
                "timestamp", System.currentTimeMillis()
        ));
    }
}