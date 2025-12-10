package com.qushetai.backend.controller;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/interests")
public class InterestsController {

    /**
     * 获取可用兴趣标签列表
     * GET /api/interests/available
     */
    @GetMapping("/available")
    public Map<String, Object> getAvailableTags() {
        // 硬编码的标签列表（可以后续从数据库或配置文件读取）
        List<String> tags = Arrays.asList(
                "美食", "运动", "艺术", "学习",
                "社交", "旅游", "游戏", "音乐",
                "电影", "读书", "摄影", "编程",
                "健身", "烹饪", "手工", "宠物"
        );

        return Map.of(
                "code", 200,
                "success", true,
                "message", "成功",
                "data", tags
        );
    }

    /**
     * 测试接口
     */
    @GetMapping("/test")
    public Map<String, Object> test() {
        return Map.of(
                "success", true,
                "message", "兴趣标签服务运行正常",
                "timestamp", System.currentTimeMillis(),
                "endpoints", Map.of(
                        "getAvailableTags", "GET /api/interests/available",
                        "test", "GET /api/interests/test"
                )
        );
    }
}