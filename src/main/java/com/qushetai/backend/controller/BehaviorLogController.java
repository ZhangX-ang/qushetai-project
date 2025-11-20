package com.qushetai.backend.controller;

import com.qushetai.backend.service.BehaviorLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/behavior")
public class BehaviorLogController {

    @Autowired
    private BehaviorLogService behaviorLogService;

    // 记录行为日志
    @PostMapping("/log")
    public Map<String, Object> logBehavior(@RequestBody Map<String, Object> behaviorData) {
        System.out.println("收到行为日志: " + behaviorData);
        return behaviorLogService.logBehavior(behaviorData);
    }

    // 测试接口
    @GetMapping("/test")
    public Map<String, Object> test() {
        return Map.of(
                "success", true,
                "message", "Behavior log service is running!"
        );
    }

    /**
     * 获取用户行为数据（实际实现）
     * 需要认证
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserBehaviorLogs(
            @PathVariable Long userId,
            HttpServletRequest request,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(required = false) Long startTime,
            @RequestParam(required = false) Long endTime,
            @RequestParam(required = false) String eventType) {

        try {
            // 从请求中获取当前用户ID
            Object userIdObj = request.getAttribute("userId");
            if (userIdObj == null) {
                return ResponseEntity.status(401).body(Map.of(
                        "success", false,
                        "message", "用户未认证"
                ));
            }

            Long currentUserId = (Long) userIdObj;

            // 权限检查
            if (!userId.equals(currentUserId)) {
                // 检查是否是管理员
                // 这里需要调用 UserService 检查管理员权限
                // 简化实现：只允许用户查看自己的数据
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "只能查看自己的行为数据"
                ));
            }

            System.out.println("【DEBUG】BehaviorLogController - 获取用户 " + userId + " 的行为数据");

            // 调用 BehaviorLogService 获取数据
            Map<String, Object> result = behaviorLogService.getUserBehaviorLogs(userId, limit, startTime, endTime, eventType);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            System.err.println("【ERROR】获取用户行为日志失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "获取行为日志失败: " + e.getMessage()
            ));
        }
    }
}