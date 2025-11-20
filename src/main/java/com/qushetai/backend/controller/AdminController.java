package com.qushetai.backend.controller;

import com.qushetai.backend.entity.User;
import com.qushetai.backend.service.UserService;
import com.qushetai.backend.service.ActivityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private ActivityService activityService;

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
     * 完全公开的管理员测试接口（临时用于调试）
     */
    @GetMapping("/public-test")
    public Map<String, Object> publicTest() {
        try {
            // 模拟管理员数据
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "管理员公开测试接口正常工作");
            result.put("timestamp", System.currentTimeMillis());
            result.put("serviceStatus", "运行正常");
            result.put("port", 8082);
            result.put("endpoints", Map.of(
                    "hello", "/hello (公开)",
                    "users", "/admin/users (需要认证)",
                    "activities", "/admin/activities (需要认证)",
                    "stats", "/admin/stats (需要认证)",
                    "testPermission", "/admin/test-permission (需要认证)"
            ));

            System.out.println("【DEBUG】公开测试接口被访问");
            return result;

        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "公开测试接口异常: " + e.getMessage()
            );
        }
    }

    /**
     * 获取所有用户列表（分页）
     */
    @GetMapping("/users")
    public Map<String, Object> getAllUsers(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            Long currentUserId = getUserIdFromRequest(request);
            System.out.println("【DEBUG】AdminController.getAllUsers - 当前用户ID: " + currentUserId);

            // 检查管理员权限
            boolean isAdmin = userService.isAdmin(currentUserId);
            System.out.println("【DEBUG】用户 " + currentUserId + " 是管理员: " + isAdmin);

            if (!isAdmin) {
                System.out.println("【DEBUG】权限拒绝: 用户 " + currentUserId + " 不是管理员");
                return Map.of("success", false, "message", "权限不足");
            }

            System.out.println("【DEBUG】权限通过，继续处理管理员请求");
            return userService.getAllUsers(currentUserId, page, size);

        } catch (Exception e) {
            System.err.println("【ERROR】获取用户列表失败: " + e.getMessage());
            return Map.of(
                    "success", false,
                    "message", "获取用户列表失败: " + e.getMessage()
            );
        }
    }

    /**
     * 获取所有活动列表（分页）
     */
    @GetMapping("/activities")
    public Map<String, Object> getAllActivities(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            Long currentUserId = getUserIdFromRequest(request);
            System.out.println("【DEBUG】AdminController.getAllActivities - 当前用户ID: " + currentUserId);

            boolean isAdmin = userService.isAdmin(currentUserId);
            System.out.println("【DEBUG】用户 " + currentUserId + " 是管理员: " + isAdmin);

            if (!isAdmin) {
                return Map.of("success", false, "message", "权限不足");
            }

            return activityService.getAllActivities(currentUserId, page, size);

        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "获取活动列表失败: " + e.getMessage()
            );
        }
    }

    /**
     * 封禁用户
     */
    @PostMapping("/users/{userId}/ban")
    public Map<String, Object> banUser(
            @PathVariable Long userId,
            HttpServletRequest request) {

        try {
            Long currentUserId = getUserIdFromRequest(request);
            System.out.println("【DEBUG】AdminController.banUser - 当前用户ID: " + currentUserId + ", 目标用户ID: " + userId);

            boolean isAdmin = userService.isAdmin(currentUserId);
            System.out.println("【DEBUG】用户 " + currentUserId + " 是管理员: " + isAdmin);

            if (!isAdmin) {
                return Map.of("success", false, "message", "权限不足");
            }

            return userService.banUser(currentUserId, userId);

        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "封禁用户失败: " + e.getMessage()
            );
        }
    }

    /**
     * 解封用户
     */
    @PostMapping("/users/{userId}/unban")
    public Map<String, Object> unbanUser(
            @PathVariable Long userId,
            HttpServletRequest request) {

        try {
            Long currentUserId = getUserIdFromRequest(request);
            System.out.println("【DEBUG】AdminController.unbanUser - 当前用户ID: " + currentUserId + ", 目标用户ID: " + userId);

            boolean isAdmin = userService.isAdmin(currentUserId);
            System.out.println("【DEBUG】用户 " + currentUserId + " 是管理员: " + isAdmin);

            if (!isAdmin) {
                return Map.of("success", false, "message", "权限不足");
            }

            return userService.unbanUser(currentUserId, userId);

        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "解封用户失败: " + e.getMessage()
            );
        }
    }

    /**
     * 下架活动
     */
    @PostMapping("/activities/{activityId}/take-down")
    public Map<String, Object> takeDownActivity(
            @PathVariable Long activityId,
            HttpServletRequest request) {

        try {
            Long currentUserId = getUserIdFromRequest(request);
            System.out.println("【DEBUG】AdminController.takeDownActivity - 当前用户ID: " + currentUserId + ", 目标活动ID: " + activityId);

            boolean isAdmin = userService.isAdmin(currentUserId);
            System.out.println("【DEBUG】用户 " + currentUserId + " 是管理员: " + isAdmin);

            if (!isAdmin) {
                return Map.of("success", false, "message", "权限不足");
            }

            return activityService.takeDownActivity(currentUserId, activityId);

        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "下架活动失败: " + e.getMessage()
            );
        }
    }

    /**
     * 恢复活动
     */
    @PostMapping("/activities/{activityId}/restore")
    public Map<String, Object> restoreActivity(
            @PathVariable Long activityId,
            HttpServletRequest request) {

        try {
            Long currentUserId = getUserIdFromRequest(request);
            System.out.println("【DEBUG】AdminController.restoreActivity - 当前用户ID: " + currentUserId + ", 目标活动ID: " + activityId);

            boolean isAdmin = userService.isAdmin(currentUserId);
            System.out.println("【DEBUG】用户 " + currentUserId + " 是管理员: " + isAdmin);

            if (!isAdmin) {
                return Map.of("success", false, "message", "权限不足");
            }

            return activityService.restoreActivity(currentUserId, activityId);

        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "恢复活动失败: " + e.getMessage()
            );
        }
    }

    /**
     * 获取管理员统计信息
     */
    @GetMapping("/stats")
    public Map<String, Object> getAdminStats(HttpServletRequest request) {
        try {
            Long currentUserId = getUserIdFromRequest(request);
            System.out.println("【DEBUG】AdminController.getAdminStats - 当前用户ID: " + currentUserId);

            boolean isAdmin = userService.isAdmin(currentUserId);
            System.out.println("【DEBUG】用户 " + currentUserId + " 是管理员: " + isAdmin);

            if (!isAdmin) {
                return Map.of("success", false, "message", "权限不足");
            }

            return userService.getAdminStats(currentUserId);

        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "获取统计信息失败: " + e.getMessage()
            );
        }
    }

    /**
     * 权限测试接口（需要认证）
     */
    @GetMapping("/test-permission")
    public Map<String, Object> testPermission(HttpServletRequest request) {
        try {
            Long currentUserId = getUserIdFromRequest(request);
            System.out.println("【DEBUG】AdminController.testPermission - 当前用户ID: " + currentUserId);

            // 获取用户信息
            Map<String, Object> userInfo = userService.getUserInfo(currentUserId);
            boolean isAdmin = userService.isAdmin(currentUserId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("currentUserId", currentUserId);
            result.put("isAdmin", isAdmin);
            result.put("userInfo", userInfo);
            result.put("timestamp", System.currentTimeMillis());

            System.out.println("【DEBUG】权限测试结果: " + result);
            return result;

        } catch (Exception e) {
            return Map.of(
                    "success", false,
                    "message", "测试失败: " + e.getMessage(),
                    "error", e.toString()
            );
        }
    }
}