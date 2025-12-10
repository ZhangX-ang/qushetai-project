package com.qushetai.backend.controller;

import com.qushetai.backend.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    @Autowired
    private TagService tagService;

    // 预定义的兴趣标签列表
    private static final List<String> DEFAULT_INTEREST_TAGS = Arrays.asList(
            "美食", "运动", "艺术", "学习",
            "社交", "旅游", "游戏", "音乐",
            "电影", "读书", "摄影", "编程",
            "健身", "烹饪", "手工", "宠物"
    );

    /**
     * 获取可用兴趣标签列表（兼容前端需求）
     * GET /api/tags/available
     */
    @GetMapping("/available")
    public Map<String, Object> getAvailableTags() {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("success", true);
        response.put("message", "成功");
        response.put("data", DEFAULT_INTEREST_TAGS);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 从标签服务获取兴趣标签
     */
    @GetMapping("/available-from-db")
    public Map<String, Object> getAvailableTagsFromDb() {
        try {
            // 从标签服务获取标签
            List<Map<String, Object>> tagsFromDb = tagService.getAllTags();

            if (tagsFromDb != null && !tagsFromDb.isEmpty()) {
                // 从结果中提取标签名称
                List<String> tagNames = new ArrayList<>();
                for (Map<String, Object> tag : tagsFromDb) {
                    Object tagName = tag.get("name");
                    if (tagName != null) {
                        tagNames.add(tagName.toString());
                    }
                }

                // 如果获取到标签，使用这些标签
                if (!tagNames.isEmpty()) {
                    return buildSuccessResponse(tagNames);
                }
            }

            // 如果标签服务没有返回数据，返回默认标签
            return buildSuccessResponse(DEFAULT_INTEREST_TAGS);

        } catch (Exception e) {
            System.out.println("【WARN】从标签服务获取标签失败，返回默认标签: " + e.getMessage());
            // 发生异常时返回默认标签
            return buildSuccessResponse(DEFAULT_INTEREST_TAGS);
        }
    }

    private Map<String, Object> buildSuccessResponse(Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("success", true);
        response.put("message", "成功");
        response.put("data", data);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 健康检查 - 检查C++标签服务和数据库连接状态
     */
    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        boolean isHealthy = tagService.healthCheck();
        return Map.of(
                "success", true,
                "message", isHealthy ? "标签服务连接正常" : "标签服务连接失败",
                "serviceStatus", isHealthy ? "CONNECTED" : "DISCONNECTED",
                "tagServiceUrl", tagService.getTagServiceBaseUrl()
        );
    }

    /**
     * 获取服务状态信息
     */
    @GetMapping("/status")
    public Map<String, Object> getServiceStatus() {
        Map<String, Object> status = tagService.getServiceStatus();
        // 创建新的可变Map
        Map<String, Object> response = new HashMap<>(status);
        response.put("success", true);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    /**
     * 测试C++标签API（调试用）
     */
    @GetMapping("/test-api")
    public Map<String, Object> testTagApi() {
        String testResult = tagService.testTagApi();
        return Map.of(
                "success", true,
                "testResult", testResult,
                "timestamp", System.currentTimeMillis()
        );
    }

    /**
     * 测试数据库连接（调试用）
     */
    @GetMapping("/test-db")
    public Map<String, Object> testDatabase() {
        String testResult = tagService.testDatabaseConnectionDirectly();
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
        // 使用HashMap替代Map.of，因为Map.of最多只能有10个参数
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("health", "GET /api/tags/health");
        endpoints.put("status", "GET /api/tags/status");
        endpoints.put("testTagApi", "GET /api/tags/test-api");
        endpoints.put("testDatabase", "GET /api/tags/test-db");
        endpoints.put("byCategory", "GET /api/tags/by-category?category={category}");
        endpoints.put("getAllTags", "GET /api/tags");
        endpoints.put("getTagById", "GET /api/tags/{tagId}");
        endpoints.put("searchTags", "GET /api/tags/search?keyword={keyword}");
        endpoints.put("getActivityTags", "GET /api/tags/activities/{activityId}");
        endpoints.put("getAvailableTags", "GET /api/tags/available");
        endpoints.put("getAvailableTagsFromDb", "GET /api/tags/available-from-db");
        endpoints.put("getPopularTags", "GET /api/tags/popular?limit=10");

        return Map.of(
                "success", true,
                "message", "标签服务接口运行正常",
                "tagServiceUrl", tagService.getTagServiceBaseUrl(),
                "endpoints", endpoints
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

    /**
     * 获取热门标签
     */
    @GetMapping("/popular")
    public Map<String, Object> getPopularTags(@RequestParam(defaultValue = "10") int limit) {
        try {
            // 注意：这里需要TagMapper支持findPopularTags方法
            // 如果TagMapper没有这个方法，可以从所有标签中模拟
            List<Map<String, Object>> allTags = tagService.getAllTags();

            // 随机选择一些作为热门标签（模拟）
            Collections.shuffle(allTags);
            List<Map<String, Object>> popularTags = allTags.subList(0, Math.min(limit, allTags.size()));

            return Map.of(
                    "success", true,
                    "data", popularTags,
                    "count", popularTags.size()
            );
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "获取热门标签失败: " + e.getMessage()
            );
        }
    }
}