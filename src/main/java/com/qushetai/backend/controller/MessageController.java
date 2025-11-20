package com.qushetai.backend.controller;

import com.qushetai.backend.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

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
     * 获取用户消息列表（分页）
     */
    @GetMapping
    public ResponseEntity<?> getMessages(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            Long userId = getUserIdFromRequest(request);
            Map<String, Object> result = messageService.getUserMessages(userId, page, size);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                    "success", false,
                    "message", "获取消息失败: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 获取未读消息
     */
    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadMessages(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            Map<String, Object> result = messageService.getUnreadMessages(userId);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                    "success", false,
                    "message", "获取未读消息失败: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 获取消息统计
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getMessageStats(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            Map<String, Object> result = messageService.getMessageStats(userId);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                    "success", false,
                    "message", "获取消息统计失败: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 标记消息为已读
     */
    @PostMapping("/{messageId}/read")
    public ResponseEntity<?> markAsRead(
            @PathVariable Long messageId,
            HttpServletRequest request) {

        try {
            Long userId = getUserIdFromRequest(request);
            Map<String, Object> result = messageService.markMessageAsRead(messageId, userId);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                    "success", false,
                    "message", "标记消息失败: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 标记所有消息为已读
     */
    @PostMapping("/read-all")
    public ResponseEntity<?> markAllAsRead(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            Map<String, Object> result = messageService.markAllAsRead(userId);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                    "success", false,
                    "message", "标记所有消息失败: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 删除消息
     */
    @DeleteMapping("/{messageId}")
    public ResponseEntity<?> deleteMessage(
            @PathVariable Long messageId,
            HttpServletRequest request) {

        try {
            Long userId = getUserIdFromRequest(request);
            Map<String, Object> result = messageService.deleteMessage(messageId, userId);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                    "success", false,
                    "message", "删除消息失败: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 发送系统消息（管理员功能）
     */
    @PostMapping("/system")
    public ResponseEntity<?> sendSystemMessage(
            @RequestBody Map<String, Object> messageData,
            HttpServletRequest request) {

        try {
            Long currentUserId = getUserIdFromRequest(request);
            // 这里应该检查管理员权限，简化实现
            if (!currentUserId.equals(1L)) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "权限不足"
                ));
            }

            Long receiverId = Long.parseLong(messageData.get("receiverId").toString());
            String title = (String) messageData.get("title");
            String content = (String) messageData.get("content");

            Map<String, Object> result = messageService.sendSystemMessage(receiverId, title, content);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                    "success", false,
                    "message", "发送系统消息失败: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 测试接口
     */
    @GetMapping("/test")
    public ResponseEntity<?> test(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);

            Map<String, Object> response = Map.of(
                    "success", true,
                    "message", "消息服务运行正常",
                    "userId", userId,
                    "timestamp", System.currentTimeMillis(),
                    "endpoints", Map.of(
                            "getMessages", "GET /messages",
                            "getUnread", "GET /messages/unread",
                            "getStats", "GET /messages/stats",
                            "markAsRead", "POST /messages/{id}/read",
                            "markAllAsRead", "POST /messages/read-all",
                            "deleteMessage", "DELETE /messages/{id}"
                    )
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of(
                    "success", false,
                    "message", "测试失败: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}