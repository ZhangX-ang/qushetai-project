package com.qushetai.backend.controller;

import com.qushetai.backend.entity.Activity;
import com.qushetai.backend.service.ActivityService;
import com.qushetai.backend.service.ActivityInterestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

@RestController
@RequestMapping("/activities")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private ActivityInterestService interestService;

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
     * 创建活动
     */
    @PostMapping
    public Map<String, Object> createActivity(@RequestBody Activity activity, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            activity.setOrganizerId(userId);
            activity.setCreatedAt(LocalDateTime.now());
            activity.setUpdatedAt(LocalDateTime.now());
            return activityService.createActivity(activity);
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "创建活动失败: " + e.getMessage()
            );
        }
    }

    /**
     * 获取活动列表
     */
    @GetMapping
    public Map<String, Object> getActivities(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (page < 1) page = 1;
        if (size < 1 || size > 50) size = 10;
        return activityService.getActivityList(page, size);
    }

    /**
     * 获取活动详情
     */
    @GetMapping("/{id}")
    public Map<String, Object> getActivityDetail(@PathVariable Long id) {
        return activityService.getActivityDetail(id);
    }

    /**
     * 更新活动
     */
    @PutMapping("/{id}")
    public Map<String, Object> updateActivity(@PathVariable Long id, @RequestBody Activity activity, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);

            // 验证用户是否有权限修改这个活动
            Activity existingActivity = activityService.getActivityDetailInternal(id);
            if (existingActivity == null) {
                return Map.of("success", false, "message", "活动不存在");
            }
            if (!existingActivity.getOrganizerId().equals(userId)) {
                return Map.of("success", false, "message", "无权修改此活动");
            }

            activity.setId(id);
            activity.setUpdatedAt(LocalDateTime.now());
            return activityService.updateActivity(activity);
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "更新活动失败: " + e.getMessage()
            );
        }
    }

    /**
     * 删除活动
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteActivity(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);

            // 验证用户是否有权限删除这个活动
            Activity existingActivity = activityService.getActivityDetailInternal(id);
            if (existingActivity == null) {
                return Map.of("success", false, "message", "活动不存在");
            }
            if (!existingActivity.getOrganizerId().equals(userId)) {
                return Map.of("success", false, "message", "无权删除此活动");
            }

            return activityService.deleteActivity(id);
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "删除活动失败: " + e.getMessage()
            );
        }
    }

    /**
     * 获取我发起的活动
     */
    @GetMapping("/my/created")
    public Map<String, Object> getMyCreatedActivities(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            return activityService.getActivitiesByOrganizer(userId);
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "获取我的活动失败: " + e.getMessage()
            );
        }
    }

    // === 社交功能：感兴趣系统 ===

    /**
     * 添加感兴趣
     */
    @PostMapping("/{activityId}/interest")
    public Map<String, Object> addInterest(@PathVariable Long activityId, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            return interestService.addInterest(userId, activityId);
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "添加感兴趣失败: " + e.getMessage()
            );
        }
    }

    /**
     * 取消感兴趣
     */
    @DeleteMapping("/{activityId}/interest")
    public Map<String, Object> removeInterest(@PathVariable Long activityId, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            return interestService.removeInterest(userId, activityId);
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "取消感兴趣失败: " + e.getMessage()
            );
        }
    }

    /**
     * 获取活动的感兴趣用户列表
     */
    @GetMapping("/{activityId}/interests")
    public Map<String, Object> getActivityInterests(@PathVariable Long activityId) {
        return interestService.getActivityInterests(activityId);
    }

    /**
     * 获取用户感兴趣的活动列表
     */
    @GetMapping("/my/interests")
    public Map<String, Object> getMyInterests(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            return interestService.getUserInterests(userId);
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "获取感兴趣活动失败: " + e.getMessage()
            );
        }
    }

    /**
     * 检查用户是否对活动感兴趣
     */
    @GetMapping("/{activityId}/check-interest")
    public Map<String, Object> checkInterest(@PathVariable Long activityId, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            return interestService.checkInterest(userId, activityId);
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "检查感兴趣状态失败: " + e.getMessage()
            );
        }
    }

    /**
     * 测试接口
     */
    @GetMapping("/hello")
    public Map<String, Object> hello() {
        return Map.of(
                "success", true,
                "message", "Activity service with interest features is running!",
                "timestamp", LocalDateTime.now()
        );
    }

    // 在 ActivityController 类中添加这些方法

    /**
     * 公开测试接口
     */
    @GetMapping("/test")
    public Map<String, Object> test() {
        return Map.of(
                "success", true,
                "message", "活动接口测试成功",
                "timestamp", LocalDateTime.now(),
                "endpoints", Map.of(
                        "getActivities", "GET /activities (需要认证)",
                        "getActivityDetail", "GET /activities/{id} (需要认证)",
                        "createActivity", "POST /activities (需要认证)",
                        "test", "GET /activities/test (公开)",
                        "publicList", "GET /activities/public/list (公开)",
                        "health", "GET /activities/health (公开)"
                )
        );
    }

    /**
     * 公开获取活动列表（简化版）
     */
    @GetMapping("/public/list")
    public Map<String, Object> getPublicActivities(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {

        try {
            if (page < 1) page = 1;
            if (size < 1 || size > 10) size = 5;

            // 模拟数据，等数据库连接后再使用真实数据
            Map<String, Object> mockResponse = new HashMap<>();
            mockResponse.put("success", true);
            mockResponse.put("message", "这是模拟数据，数据库连接后显示真实数据");
            mockResponse.put("data", Arrays.asList(
                    Map.of("id", 1, "title", "测试活动1", "description", "这是一个测试活动"),
                    Map.of("id", 2, "title", "测试活动2", "description", "这是另一个测试活动")
            ));
            mockResponse.put("pagination", Map.of(
                    "currentPage", page,
                    "pageSize", size,
                    "total", 2,
                    "totalPages", 1
            ));

            return mockResponse;
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "获取活动列表失败: " + e.getMessage()
            );
        }
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "service", "ActivityService",
                "status", "UP",
                "timestamp", LocalDateTime.now(),
                "database", "未连接",
                "note", "这是一个健康检查接口，用于验证服务是否正常运行"
        );
    }

    /**
     * 检查活动权限（调试用）
     */
    @GetMapping("/{id}/check-permission")
    public Map<String, Object> checkPermission(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            Activity activity = activityService.getActivityDetailInternal(id);

            if (activity == null) {
                return Map.of(
                        "success", false,
                        "message", "活动不存在",
                        "hasPermission", false
                );
            }

            boolean hasPermission = activity.getOrganizerId().equals(userId);

            return Map.of(
                    "success", true,
                    "hasPermission", hasPermission,
                    "currentUserId", userId,
                    "activityOrganizerId", activity.getOrganizerId(),
                    "activityTitle", activity.getTitle()
            );
        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "检查权限失败: " + e.getMessage(),
                    "hasPermission", false
            );
        }
    }
}