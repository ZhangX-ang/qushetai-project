package com.qushetai.backend.controller;

import com.qushetai.backend.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    @Autowired
    private TagService tagService;

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        boolean isHealthy = tagService.healthCheck();
        return Map.of(
                "success", true,
                "message", isHealthy ? "标签服务连接正常" : "标签服务连接失败",
                "tagServiceStatus", isHealthy ? "CONNECTED" : "DISCONNECTED",
                "tagServiceUrl", tagService.getTagServiceBaseUrl()
        );
    }

    /**
     * 获取标签服务状态
     */
    @GetMapping("/status")
    public Map<String, Object> getServiceStatus() {
        Map<String, Object> status = tagService.getServiceStatus();
        // 创建新的可变Map，而不是在不可变Map上添加
        Map<String, Object> response = new HashMap<>(status);
        response.put("success", true);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 测试C同学的新接口
     */
    @GetMapping("/test-new-apis")
    public Map<String, Object> testNewApis() {
        String testResult = tagService.testNewApis();
        return Map.of(
                "success", true,
                "testResult", testResult,
                "timestamp", System.currentTimeMillis()
        );
    }

    /**
     * 根据分类获取标签
     */
    @GetMapping("/by-category")
    public Map<String, Object> getTagsByCategory(@RequestParam String category) {
        List<Map<String, Object>> tags = tagService.getTagsByCategory(category);
        return Map.of(
                "success", true,
                "data", tags,
                "count", tags.size(),
                "category", category
        );
    }

    /**
     * 测试接口
     */
    @GetMapping("/test")
    public Map<String, Object> test() {
        return Map.of(
                "success", true,
                "message", "标签服务接口运行正常",
                "tagServiceUrl", tagService.getTagServiceBaseUrl(),
                "endpoints", Map.of(
                        "health", "GET /api/tags/health",
                        "status", "GET /api/tags/status",
                        "testNewApis", "GET /api/tags/test-new-apis",
                        "byCategory", "GET /api/tags/by-category?category={category}",
                        "getAllTags", "GET /api/tags",
                        "getTagById", "GET /api/tags/{tagId}",
                        "searchTags", "GET /api/tags/search?keyword={keyword}",
                        "getActivityTags", "GET /api/tags/activities/{activityId}"
                )
        );
    }

    /**
     * 获取所有标签
     */
    @GetMapping
    public Map<String, Object> getAllTags() {
        List<Map<String, Object>> tags = tagService.getAllTags();
        return Map.of(
                "success", true,
                "data", tags,
                "count", tags.size()
        );
    }

    /**
     * 搜索标签
     */
    @GetMapping("/search")
    public Map<String, Object> searchTags(@RequestParam String keyword) {
        List<Map<String, Object>> tags = tagService.searchTags(keyword);
        return Map.of(
                "success", true,
                "data", tags,
                "count", tags.size(),
                "keyword", keyword
        );
    }

    /**
     * 获取活动标签
     */
    @GetMapping("/activities/{activityId}")
    public Map<String, Object> getActivityTags(@PathVariable Long activityId) {
        List<String> tags = tagService.getTagsByActivityId(activityId);
        return Map.of(
                "success", true,
                "data", tags,
                "count", tags.size(),
                "activityId", activityId
        );
    }

    /**
     * 获取标签详情
     */
    @GetMapping("/{tagId}")
    public Map<String, Object> getTagById(@PathVariable Long tagId) {
        Map<String, Object> tag = tagService.getTagById(tagId);
        if (tag.isEmpty()) {
            return Map.of(
                    "success", false,
                    "message", "标签不存在"
            );
        }
        return Map.of(
                "success", true,
                "data", tag
        );
    }
}