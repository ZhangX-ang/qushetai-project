package com.qushetai.backend.service;

import com.qushetai.backend.entity.Message;
import com.qushetai.backend.mapper.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageService {

    @Autowired
    private MessageMapper messageMapper;

    /**
     * 发送系统消息
     */
    public Map<String, Object> sendSystemMessage(Long receiverId, String title, String content) {
        Map<String, Object> result = new HashMap<>();

        try {
            Message message = new Message(0L, receiverId, title, content, 1, 0, 0L);
            int rows = messageMapper.insert(message);

            if (rows > 0) {
                result.put("success", true);
                result.put("message", "消息发送成功");
                result.put("messageId", message.getId());
            } else {
                result.put("success", false);
                result.put("message", "消息发送失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "发送消息失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 发送活动通知（当有人对活动感兴趣时）
     */
    public Map<String, Object> sendActivityNotification(Long receiverId, String title, String content, Long activityId) {
        Map<String, Object> result = new HashMap<>();

        try {
            int rows = messageMapper.sendActivityNotification(receiverId, title, content, activityId);

            if (rows > 0) {
                result.put("success", true);
                result.put("message", "活动通知发送成功");
            } else {
                result.put("success", false);
                result.put("message", "活动通知发送失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "发送活动通知失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取用户消息列表（分页）
     */
    public Map<String, Object> getUserMessages(Long userId, int page, int size) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (page < 1) page = 1;
            if (size < 1 || size > 50) size = 20;

            int offset = (page - 1) * size;
            List<Message> messages = messageMapper.findByReceiverWithPagination(userId, offset, size);
            int total = messageMapper.countByReceiver(userId);
            int unreadCount = messageMapper.countUnreadByReceiver(userId);

            // 获取消息类型统计
            Map<Integer, Map<String, Object>> typeStats = messageMapper.getMessageTypeStats(userId);

            result.put("success", true);
            result.put("data", messages);
            result.put("pagination", Map.of(
                    "currentPage", page,
                    "pageSize", size,
                    "total", total,
                    "totalPages", (int) Math.ceil((double) total / size),
                    "unreadCount", unreadCount
            ));
            result.put("typeStats", typeStats);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取消息列表失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取用户未读消息
     */
    public Map<String, Object> getUnreadMessages(Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Message> messages = messageMapper.findUnreadByReceiver(userId);
            int unreadCount = messages.size();

            result.put("success", true);
            result.put("data", messages);
            result.put("unreadCount", unreadCount);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取未读消息失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 标记消息为已读
     */
    public Map<String, Object> markMessageAsRead(Long messageId, Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            int rows = messageMapper.markAsRead(messageId, userId);

            if (rows > 0) {
                result.put("success", true);
                result.put("message", "消息标记为已读");
            } else {
                result.put("success", false);
                result.put("message", "消息标记失败，可能消息不存在或无权操作");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "标记消息失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 标记所有消息为已读
     */
    public Map<String, Object> markAllAsRead(Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            int rows = messageMapper.markAllAsRead(userId);

            result.put("success", true);
            result.put("message", "已标记 " + rows + " 条消息为已读");
            result.put("markedCount", rows);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "标记所有消息失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 删除消息
     */
    public Map<String, Object> deleteMessage(Long messageId, Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            int rows = messageMapper.deleteMessage(messageId, userId);

            if (rows > 0) {
                result.put("success", true);
                result.put("message", "消息删除成功");
            } else {
                result.put("success", false);
                result.put("message", "消息删除失败，可能消息不存在或无权操作");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除消息失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取消息统计
     */
    public Map<String, Object> getMessageStats(Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            int totalCount = messageMapper.countByReceiver(userId);
            int unreadCount = messageMapper.countUnreadByReceiver(userId);
            Map<Integer, Map<String, Object>> typeStats = messageMapper.getMessageTypeStats(userId);

            result.put("success", true);
            result.put("data", Map.of(
                    "totalCount", totalCount,
                    "unreadCount", unreadCount,
                    "typeStats", typeStats
            ));

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取消息统计失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 自动发送"感兴趣"通知
     * 当用户对活动感兴趣时，自动通知活动发起者
     */
    public void sendInterestNotification(Long activityId, Long interestedUserId, String interestedUserName, Long activityOwnerId) {
        try {
            String title = "有人对您的活动感兴趣";
            String content = "用户 " + interestedUserName + " 对您的活动表示了兴趣，快去看看吧！";

            messageMapper.sendActivityNotification(activityOwnerId, title, content, activityId);
            System.out.println("已发送感兴趣通知给活动发起者: " + activityOwnerId);

        } catch (Exception e) {
            System.err.println("发送感兴趣通知失败: " + e.getMessage());
        }
    }
}