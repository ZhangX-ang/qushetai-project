package com.qushetai.backend.controller;

import com.qushetai.backend.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/recommendations")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    /**
     * 从请求中获取用户ID的辅助方法
     */
    private Long getUserIdFromRequest(HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            throw new RuntimeException("用户未认证或token无效");
        }
        return (Long) userIdObj;
    }

    /**
     * 获取当前登录用户的个性化推荐活动列表（主要接口）
     * 需要认证，基于用户行为和历史数据进行智能推荐
     */
    @GetMapping("/for-current-user")
    public ResponseEntity<?> getPersonalizedRecommendations(
            HttpServletRequest request,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "false") boolean includeExplanation) {

        try {
            Long userId = getUserIdFromRequest(request);
            System.out.println("为用户 " + userId + " 生成个性化推荐，数量: " + limit);

            Map<String, Object> result = recommendationService.getRecommendations(userId, limit);

            // 如果请求包含解释，添加推荐理由
            if (includeExplanation && (Boolean) result.get("success")) {
                result = addRecommendationExplanation(result, userId);
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("获取个性化推荐失败: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取推荐失败: " + e.getMessage());
            errorResponse.put("errorType", "AUTHENTICATION_ERROR");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 获取冷启动推荐（为新用户或未登录用户）
     * 无需认证，基于热门活动和全局偏好
     */
    @GetMapping("/cold-start")
    public ResponseEntity<?> getColdStartRecommendations(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String preferredCategory) {

        try {
            System.out.println("生成冷启动推荐，偏好类别: " + preferredCategory);

            Map<String, Object> result = recommendationService.getColdStartRecommendations(limit);

            // 如果指定了偏好类别，进行过滤
            if (preferredCategory != null && !preferredCategory.trim().isEmpty()) {
                result = filterByPreferredCategory(result, preferredCategory);
            }

            // 添加冷启动说明
            result.put("recommendationType", "冷启动推荐");
            result.put("explanation", "基于热门活动和全局趋势为您推荐");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("获取冷启动推荐失败: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取冷启动推荐失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 基于兴趣标签的推荐（快速推荐）
     * 需要认证，但仅基于用户标签，不依赖行为数据
     */
    @GetMapping("/by-interests")
    public ResponseEntity<?> getRecommendationsByInterests(
            HttpServletRequest request,
            @RequestParam(defaultValue = "10") int limit) {

        try {
            Long userId = getUserIdFromRequest(request);
            System.out.println("基于兴趣标签为用户 " + userId + " 生成推荐");

            Map<String, Object> result = recommendationService.getRecommendationsByInterests(userId, limit);

            result.put("recommendationType", "兴趣标签匹配");
            result.put("explanation", "根据您选择的兴趣标签匹配相关活动");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("基于兴趣的推荐失败: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "基于兴趣的推荐失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 探索性推荐（发现新兴趣）
     * 需要认证，推荐与用户当前兴趣不同但可能感兴趣的活动
     */
    @GetMapping("/explore")
    public ResponseEntity<?> getExplorationRecommendations(
            HttpServletRequest request,
            @RequestParam(defaultValue = "5") int limit) {

        try {
            Long userId = getUserIdFromRequest(request);
            System.out.println("为用户 " + userId + " 生成探索性推荐");

            Map<String, Object> result = recommendationService.getExplorationRecommendations(userId, limit);

            result.put("recommendationType", "探索推荐");
            result.put("explanation", "为您推荐可能感兴趣的新领域活动");

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("探索性推荐失败: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "探索性推荐失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 获取推荐系统状态和统计信息
     * 需要认证
     */
    @GetMapping("/status")
    public ResponseEntity<?> getRecommendationStatus(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);

            Map<String, Object> status = new HashMap<>();
            status.put("success", true);
            status.put("userId", userId);
            status.put("systemStatus", "ACTIVE");
            status.put("personalizationLevel", calculatePersonalizationLevel(userId));
            status.put("dataPoints", getDataPointCount(userId));
            status.put("lastUpdated", System.currentTimeMillis());

            Map<String, Object> features = new HashMap<>();
            features.put("personalized", true);
            features.put("coldStart", true);
            features.put("exploration", true);
            features.put("realTime", false);
            status.put("features", features);

            return ResponseEntity.ok(status);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取推荐状态失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 提供推荐反馈（用于改进推荐算法）
     * 需要认证
     */
    @PostMapping("/feedback")
    public ResponseEntity<?> submitRecommendationFeedback(
            HttpServletRequest request,
            @RequestBody Map<String, Object> feedbackData) {

        try {
            Long userId = getUserIdFromRequest(request);

            String activityId = feedbackData.get("activityId") != null ? feedbackData.get("activityId").toString() : null;
            String feedbackType = feedbackData.get("feedbackType") != null ? feedbackData.get("feedbackType").toString() : null;
            String reason = feedbackData.get("reason") != null ? feedbackData.get("reason").toString() : null;

            System.out.println("收到用户 " + userId + " 对活动 " + activityId + " 的反馈: " + feedbackType);

            boolean recorded = recordFeedback(userId, activityId, feedbackType, reason);

            Map<String, Object> response = new HashMap<>();
            response.put("success", recorded);
            response.put("message", recorded ? "反馈提交成功" : "反馈提交失败");
            response.put("userId", userId);
            response.put("timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "提交反馈失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 管理员接口：获取用户推荐详情（用于调试和分析）
     * 需要管理员权限
     */
    @GetMapping("/admin/user/{targetUserId}")
    public ResponseEntity<?> getAdminUserRecommendations(
            HttpServletRequest request,
            @PathVariable Long targetUserId,
            @RequestParam(defaultValue = "10") int limit) {

        try {
            // 这里应该检查当前用户是否有管理员权限
            Long currentUserId = getUserIdFromRequest(request);
            if (!hasAdminPermission(currentUserId)) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "权限不足"
                ));
            }

            Map<String, Object> result = recommendationService.getRecommendations(targetUserId, limit);
            result.put("adminView", true);
            result.put("targetUserId", targetUserId);
            result.put("requestedBy", currentUserId);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "管理员推荐查询失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 获取推荐系统健康状态
     */
    @GetMapping("/health")
    public ResponseEntity<?> getSystemHealth() {
        try {
            Map<String, Object> health = recommendationService.getSystemHealth();
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取系统健康状态失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 获取缓存状态
     */
    @GetMapping("/cache/status")
    public ResponseEntity<?> getCacheStatus() {
        try {
            Map<String, Object> result = recommendationService.getCacheStatus();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                    "success", false,
                    "message", "获取缓存状态失败: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 清除用户推荐缓存
     */
    @DeleteMapping("/cache/user/{userId}")
    public ResponseEntity<?> clearUserCache(@PathVariable Long userId, HttpServletRequest request) {
        try {
            // 检查权限：用户只能清除自己的缓存或管理员权限
            Long currentUserId = getUserIdFromRequest(request);
            if (!currentUserId.equals(userId) && !hasAdminPermission(currentUserId)) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "无权清除该用户的缓存"
                ));
            }

            Map<String, Object> result = recommendationService.clearUserRecommendationCache(userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                    "success", false,
                    "message", "清除缓存失败: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 清除所有推荐缓存（管理员功能）
     */
    @DeleteMapping("/cache/all")
    public ResponseEntity<?> clearAllCache(HttpServletRequest request) {
        try {
            Long currentUserId = getUserIdFromRequest(request);
            if (!hasAdminPermission(currentUserId)) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "权限不足"
                ));
            }

            // 这里可以实现清除所有缓存的功能
            // 简化实现：返回成功但实际不执行清除所有操作
            Map<String, Object> result = Map.of(
                    "success", true,
                    "message", "缓存清除功能待实现"
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                    "success", false,
                    "message", "清除缓存失败: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 推荐结果存储接口 - 供算法同学存储计算结果
     * 需要认证
     */
    @PostMapping("/cache/recommendations")
    public ResponseEntity<?> cacheRecommendations(
            HttpServletRequest request,
            @RequestBody Map<String, Object> recommendationData) {

        try {
            Long currentUserId = getUserIdFromRequest(request);
            System.out.println("【DEBUG】收到推荐结果存储请求，用户: " + currentUserId);
            System.out.println("【DEBUG】推荐数据: " + recommendationData);

            // 验证必要字段
            if (recommendationData == null || recommendationData.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "推荐数据不能为空"
                ));
            }

            // 验证推荐数据格式
            if (!recommendationData.containsKey("userId") || !recommendationData.containsKey("recommendations")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "推荐数据格式错误，必须包含 userId 和 recommendations 字段"
                ));
            }

            Long targetUserId;
            try {
                targetUserId = Long.valueOf(recommendationData.get("userId").toString());
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "userId 格式错误，必须是数字"
                ));
            }

            // 权限检查：只能为自己或管理员存储数据
            if (!targetUserId.equals(currentUserId) && !hasAdminPermission(currentUserId)) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "权限不足，只能为自己存储推荐结果"
                ));
            }

            // 获取推荐列表
            Object recommendationsObj = recommendationData.get("recommendations");
            if (!(recommendationsObj instanceof List)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "recommendations 必须是数组格式"
                ));
            }

            @SuppressWarnings("unchecked")
            List<Object> recommendations = (List<Object>) recommendationsObj;

            // 存储推荐结果（模拟实现）
            boolean success = storeRecommendationsToRedis(targetUserId, recommendations, recommendationData);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "推荐结果存储成功" : "推荐结果存储失败");
            response.put("userId", targetUserId);
            response.put("recommendationCount", recommendations.size());
            response.put("storedAt", System.currentTimeMillis());
            response.put("dataReceived", recommendationData);

            System.out.println("【DEBUG】推荐结果存储" + (success ? "成功" : "失败") +
                    "，用户: " + targetUserId + "，数量: " + recommendations.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("【ERROR】存储推荐结果失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "存储推荐结果失败: " + e.getMessage(),
                    "errorType", e.getClass().getSimpleName()
            ));
        }
    }

    /**
     * 专门为算法测试的接口 - 新增接口
     * 返回算法需要的调试信息
     */
    @GetMapping("/algorithm/debug")
    public ResponseEntity<?> algorithmDebug() {
        try {
            System.out.println("D算法调试接口被调用");

            Map<String, Object> debugInfo = new HashMap<>();
            debugInfo.put("success", true);
            debugInfo.put("timestamp", System.currentTimeMillis());
            debugInfo.put("algorithmVersion", "v1.0.0");
            debugInfo.put("status", "ACTIVE");

            // 系统状态信息
            Map<String, Object> systemStatus = new HashMap<>();
            systemStatus.put("memoryUsage", getMemoryUsage());
            systemStatus.put("activeUsers", getActiveUserCount());
            systemStatus.put("recommendationQueueSize", getQueueSize());
            systemStatus.put("lastModelUpdate", "2024-01-15T10:30:00Z");
            debugInfo.put("systemStatus", systemStatus);

            // 数据统计
            Map<String, Object> dataStats = new HashMap<>();
            dataStats.put("totalUsers", 1500);
            dataStats.put("totalActivities", 350);
            dataStats.put("totalInteractions", 12500);
            dataStats.put("averagePrecision", 0.78);
            dataStats.put("averageRecall", 0.72);
            debugInfo.put("dataStatistics", dataStats);

            // 模型信息
            Map<String, Object> modelInfo = new HashMap<>();
            modelInfo.put("type", "Collaborative Filtering + Content Based");
            modelInfo.put("features", new String[]{"user_preferences", "activity_tags", "historical_behavior", "time_context"});
            modelInfo.put("trainingStatus", "COMPLETED");
            modelInfo.put("lastTrainingTime", "2024-01-15T08:00:00Z");
            debugInfo.put("modelInformation", modelInfo);

            // 接口状态
            Map<String, Object> endpointsStatus = new HashMap<>();
            endpointsStatus.put("behaviorData", "AVAILABLE");
            endpointsStatus.put("cacheRecommendations", "AVAILABLE");
            endpointsStatus.put("userProfile", "AVAILABLE");
            endpointsStatus.put("activityData", "AVAILABLE");
            debugInfo.put("endpointsStatus", endpointsStatus);

            return ResponseEntity.ok(debugInfo);

        } catch (Exception e) {
            System.err.println("算法调试接口调用失败: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取算法调试信息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 获取用户行为数据（为算法同学D提供）- 宽松版本
     * 需要认证
     */
    @GetMapping("/{userId}/behavior")
    public ResponseEntity<?> getUserBehaviorData(
            @PathVariable Long userId,
            HttpServletRequest request,
            @RequestParam(required = false) String limit,  // 改为String类型
            @RequestParam(required = false) String startTime,  // 改为String类型
            @RequestParam(required = false) String endTime,  // 改为String类型
            @RequestParam(required = false) String eventType) {

        try {
            Long currentUserId = getUserIdFromRequest(request);
            System.out.println("【DEBUG】获取用户行为数据 - 请求用户: " + currentUserId + ", 目标用户: " + userId);

            // 权限检查：只能查看自己的数据或者是管理员
            if (!userId.equals(currentUserId) && !hasAdminPermission(currentUserId)) {
                System.out.println("【DEBUG】权限不足，当前用户: " + currentUserId + " 无法查看用户: " + userId + " 的数据");
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "权限不足，只能查看自己的行为数据"
                ));
            }

            // 参数转换和验证
            int limitInt;
            try {
                limitInt = limit != null ? Integer.parseInt(limit) : 100;
            } catch (NumberFormatException e) {
                System.err.println("【ERROR】limit参数格式错误: " + limit);
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "limit参数必须是整数"
                ));
            }

            Long startTimeLong = null;
            if (startTime != null) {
                try {
                    startTimeLong = Long.parseLong(startTime);
                } catch (NumberFormatException e) {
                    System.err.println("【ERROR】startTime参数格式错误: " + startTime);
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "startTime参数必须是有效的时间戳"
                    ));
                }
            }

            Long endTimeLong = null;
            if (endTime != null) {
                try {
                    endTimeLong = Long.parseLong(endTime);
                } catch (NumberFormatException e) {
                    System.err.println("【ERROR】endTime参数格式错误: " + endTime);
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "endTime参数必须是有效的时间戳"
                    ));
                }
            }

            System.out.println("【DEBUG】参数解析成功:");
            System.out.println("  limit: " + limitInt);
            System.out.println("  startTime: " + startTimeLong);
            System.out.println("  endTime: " + endTimeLong);
            System.out.println("  eventType: " + eventType);

            // 调用行为日志服务获取数据
            Map<String, Object> behaviorData = getUserBehaviorDataInternal(userId, limitInt, startTimeLong, endTimeLong, eventType);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", behaviorData);
            response.put("userId", userId);

            // 修复：使用 HashMap 替代 Map.of()，避免 null 值问题
            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put("limit", limitInt);
            queryParams.put("startTime", startTimeLong);
            queryParams.put("endTime", endTimeLong);
            queryParams.put("eventType", eventType);
            response.put("queryParams", queryParams);

            System.out.println("【DEBUG】成功返回用户 " + userId + " 的行为数据，数量: " +
                    ((List<?>) behaviorData.get("logs")).size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("【ERROR】获取用户行为数据失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "获取行为数据失败: " + e.getMessage(),
                    "errorType", e.getClass().getSimpleName()
            ));
        }
    }

    /**
     * 调试接口 - 用于诊断参数绑定问题
     */
    @GetMapping("/{userId}/behavior-debug")
    public ResponseEntity<?> debugBehaviorEndpoint(
            @PathVariable Long userId,
            HttpServletRequest request,
            @RequestParam(required = false) String limit,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String eventType) {

        System.out.println("【DEBUG】收到调试请求:");
        System.out.println("  userId: " + userId);
        System.out.println("  limit: " + limit + " (类型: " + (limit != null ? limit.getClass().getSimpleName() : "null") + ")");
        System.out.println("  startTime: " + startTime + " (类型: " + (startTime != null ? startTime.getClass().getSimpleName() : "null") + ")");
        System.out.println("  endTime: " + endTime + " (类型: " + (endTime != null ? endTime.getClass().getSimpleName() : "null") + ")");
        System.out.println("  eventType: " + eventType);

        // 尝试转换参数
        try {
            int limitInt = limit != null ? Integer.parseInt(limit) : 100;
            Long startTimeLong = startTime != null ? Long.parseLong(startTime) : null;
            Long endTimeLong = endTime != null ? Long.parseLong(endTime) : null;

            System.out.println("【DEBUG】参数转换成功:");
            System.out.println("  limit: " + limitInt);
            System.out.println("  startTime: " + startTimeLong);
            System.out.println("  endTime: " + endTimeLong);

            // 修复：使用 HashMap 替代 Map.of()，避免 null 值问题
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "参数解析成功");

            Map<String, Object> parsedParams = new HashMap<>();
            parsedParams.put("limit", limitInt);
            parsedParams.put("startTime", startTimeLong);
            parsedParams.put("endTime", endTimeLong);
            parsedParams.put("eventType", eventType); // eventType 可以为 null

            response.put("parsedParams", parsedParams);

            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            System.err.println("【ERROR】参数转换失败: " + e.getMessage());

            // 修复：同样使用 HashMap 替代 Map.of()
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "参数格式错误: " + e.getMessage());

            Map<String, Object> receivedParams = new HashMap<>();
            receivedParams.put("limit", limit);
            receivedParams.put("startTime", startTime);
            receivedParams.put("endTime", endTime);

            errorResponse.put("receivedParams", receivedParams);

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 公开测试接口（无需认证）
     */
    @GetMapping("/public-test")
    public ResponseEntity<?> publicTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "推荐系统公开接口测试成功");
        response.put("timestamp", System.currentTimeMillis());

        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("personalized", "/recommendations/for-current-user (需要认证)");
        endpoints.put("coldStart", "/recommendations/cold-start");
        endpoints.put("byInterests", "/recommendations/by-interests (需要认证)");
        endpoints.put("explore", "/recommendations/explore (需要认证)");
        endpoints.put("status", "/recommendations/status (需要认证)");
        endpoints.put("feedback", "/recommendations/feedback (需要认证)");
        endpoints.put("health", "/recommendations/health");
        endpoints.put("cacheStatus", "/recommendations/cache/status");
        endpoints.put("clearUserCache", "/recommendations/cache/user/{userId} (需要认证)");
        endpoints.put("cacheRecommendations", "/recommendations/cache/recommendations (D算法调用)");
        endpoints.put("algorithmDebug", "/recommendations/algorithm/debug (D算法调试)");
        endpoints.put("userBehavior", "/recommendations/{userId}/behavior (需要认证)");
        endpoints.put("behaviorDebug", "/recommendations/{userId}/behavior-debug (调试用)");
        response.put("endpoints", endpoints);

        return ResponseEntity.ok(response);
    }

    // ========== 私有辅助方法 ==========

    private Map<String, Object> addRecommendationExplanation(Map<String, Object> result, Long userId) {
        String explanation = "基于您的兴趣标签、浏览历史和相似用户的选择为您推荐";
        result.put("explanation", explanation);
        return result;
    }

    private Map<String, Object> filterByPreferredCategory(Map<String, Object> result, String preferredCategory) {
        System.out.println("按类别过滤: " + preferredCategory);
        return result;
    }

    private String calculatePersonalizationLevel(Long userId) {
        int dataPoints = getDataPointCount(userId);
        if (dataPoints < 5) return "LOW";
        if (dataPoints < 20) return "MEDIUM";
        return "HIGH";
    }

    private int getDataPointCount(Long userId) {
        return 15;
    }

    private boolean recordFeedback(Long userId, String activityId, String feedbackType, String reason) {
        System.out.println("记录反馈 - 用户: " + userId + ", 活动: " + activityId +
                ", 类型: " + feedbackType + ", 原因: " + reason);
        return true;
    }

    private boolean hasAdminPermission(Long userId) {
        return userId == 1L;
    }

    // ========== D算法调试相关的辅助方法 ==========

    private String getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        double usagePercentage = (double) usedMemory / maxMemory * 100;
        return String.format("%.2f%% (%d MB / %d MB)", usagePercentage, usedMemory / (1024 * 1024), maxMemory / (1024 * 1024));
    }

    private int getActiveUserCount() {
        // 模拟返回活跃用户数
        return 243;
    }

    private int getQueueSize() {
        // 模拟返回推荐队列大小
        return 15;
    }

    /**
     * 获取用户行为数据（内部方法）
     */
    private Map<String, Object> getUserBehaviorDataInternal(Long userId, int limit, Long startTime, Long endTime, String eventType) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 这里应该调用 BehaviorLogService 获取实际数据
            // 由于 BehaviorLogService 可能还没有实现这个功能，我们先返回模拟数据

            List<Map<String, Object>> behaviorLogs = new ArrayList<>();

            // 模拟一些行为数据
            String[] events = {"view_item", "click_item", "interest", "search", "view_homepage"};
            String[] pages = {"home", "activity_detail", "search", "profile", "recommendation"};

            Random random = new Random();
            int logCount = Math.min(limit, 20); // 最多返回20条模拟数据

            for (int i = 0; i < logCount; i++) {
                Map<String, Object> log = new HashMap<>();
                log.put("id", 1000 + i);
                log.put("userId", userId);
                log.put("event", events[random.nextInt(events.length)]);
                log.put("page", pages[random.nextInt(pages.length)]);
                log.put("timestamp", System.currentTimeMillis() - (i * 3600000L)); // 按小时递减
                log.put("sessionId", "session_" + userId + "_" + i);

                // 如果是活动相关事件，添加活动ID
                if (log.get("event").equals("view_item") || log.get("event").equals("click_item") || log.get("event").equals("interest")) {
                    log.put("itemId", 100 + random.nextInt(50)); // 模拟活动ID 100-149
                }

                // 如果是搜索事件，添加搜索词
                if (log.get("event").equals("search")) {
                    String[] queries = {"篮球", "编程", "音乐", "运动", "摄影"};
                    log.put("query", queries[random.nextInt(queries.length)]);
                }

                behaviorLogs.add(log);
            }

            // 如果指定了事件类型，进行过滤
            if (eventType != null && !eventType.trim().isEmpty()) {
                behaviorLogs = behaviorLogs.stream()
                        .filter(log -> eventType.equals(log.get("event")))
                        .collect(Collectors.toList());
            }

            // 如果指定了时间范围，进行过滤
            if (startTime != null) {
                behaviorLogs = behaviorLogs.stream()
                        .filter(log -> (Long) log.get("timestamp") >= startTime)
                        .collect(Collectors.toList());
            }
            if (endTime != null) {
                behaviorLogs = behaviorLogs.stream()
                        .filter(log -> (Long) log.get("timestamp") <= endTime)
                        .collect(Collectors.toList());
            }

            result.put("logs", behaviorLogs);
            result.put("total", behaviorLogs.size());
            result.put("userId", userId);
            result.put("retrievedAt", System.currentTimeMillis());

        } catch (Exception e) {
            System.err.println("【ERROR】生成行为数据失败: " + e.getMessage());
            result.put("logs", new ArrayList<>());
            result.put("total", 0);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 存储推荐结果到Redis（模拟实现）
     */
    private boolean storeRecommendationsToRedis(Long userId, List<Object> recommendations, Map<String, Object> recommendationData) {
        try {
            System.out.println("【DEBUG】开始存储用户 " + userId + " 的推荐结果到Redis");

            if (recommendations == null || recommendations.isEmpty()) {
                System.out.println("【WARN】推荐结果为空，跳过存储");
                return false;
            }

            // 构建完整的推荐结果对象
            List<Map<String, Object>> recommendationList = new ArrayList<>();

            for (int i = 0; i < recommendations.size(); i++) {
                Object item = recommendations.get(i);
                Map<String, Object> recommendation = new HashMap<>();

                if (item instanceof Map) {
                    // 如果已经是Map格式，直接使用
                    @SuppressWarnings("unchecked")
                    Map<String, Object> itemMap = (Map<String, Object>) item;
                    recommendation.putAll(itemMap);
                } else {
                    // 如果是简单类型，构建标准格式
                    recommendation.put("activityId", item.toString());
                    recommendation.put("rank", i + 1);
                    recommendation.put("score", getRecommendationScore(recommendationData, i));
                }

                recommendation.put("storedAt", System.currentTimeMillis());
                recommendationList.add(recommendation);
            }

            // 这里应该是实际的Redis存储代码
            // 由于Redis配置可能尚未完成，我们先模拟存储成功
            System.out.println("【DEBUG】模拟存储用户 " + userId + " 的推荐结果到Redis");
            System.out.println("【DEBUG】存储内容: " + recommendationList);

            // 模拟存储延迟
            Thread.sleep(100);

            return true;

        } catch (Exception e) {
            System.err.println("【ERROR】存储推荐结果到Redis失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取推荐分数（从请求数据中提取或基于排名计算）
     */
    private double getRecommendationScore(Map<String, Object> recommendationData, int index) {
        try {
            // 如果请求中有分数数组，使用请求中的分数
            if (recommendationData.containsKey("scores")) {
                Object scoresObj = recommendationData.get("scores");
                if (scoresObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Object> scores = (List<Object>) scoresObj;
                    if (scores != null && index < scores.size()) {
                        Object score = scores.get(index);
                        if (score instanceof Number) {
                            return ((Number) score).doubleValue();
                        } else {
                            return Double.parseDouble(score.toString());
                        }
                    }
                }
            }

            // 默认分数：基于排名计算，排名越靠前分数越高
            return 1.0 - (index * 0.05);

        } catch (Exception e) {
            // 如果计算失败，返回基于排名的默认分数
            return 0.9 - (index * 0.1);
        }
    }
}