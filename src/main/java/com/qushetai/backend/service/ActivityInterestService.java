package com.qushetai.backend.service;

import com.qushetai.backend.entity.UserActivityInterest;
import com.qushetai.backend.entity.Activity;
import com.qushetai.backend.entity.User;
import com.qushetai.backend.mapper.UserActivityInterestMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ActivityInterestService {

    @Autowired
    private UserActivityInterestMapper interestMapper;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private ActivityService activityService;

    // 添加感兴趣
    public Map<String, Object> addInterest(Long userId, Long activityId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 检查是否已存在
            UserActivityInterest existing = interestMapper.findByUserAndActivity(userId, activityId);
            if (existing != null) {
                result.put("success", false);
                result.put("message", "已经对该活动感兴趣了");
                return result;
            }

            // 创建新的感兴趣记录
            UserActivityInterest interest = new UserActivityInterest(userId, activityId);
            int rows = interestMapper.insert(interest);

            if (rows > 0) {
                // 发送通知给活动发起者
                sendInterestNotification(activityId, userId);

                result.put("success", true);
                result.put("message", "添加感兴趣成功");
                result.put("interestId", interest.getId());
            } else {
                result.put("success", false);
                result.put("message", "添加感兴趣失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "服务器错误: " + e.getMessage());
        }

        return result;
    }

    // 取消感兴趣
    public Map<String, Object> removeInterest(Long userId, Long activityId) {
        Map<String, Object> result = new HashMap<>();

        try {
            int rows = interestMapper.delete(userId, activityId);

            if (rows > 0) {
                result.put("success", true);
                result.put("message", "取消感兴趣成功");
            } else {
                result.put("success", false);
                result.put("message", "取消感兴趣失败，可能还未感兴趣");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "服务器错误: " + e.getMessage());
        }

        return result;
    }

    // 获取活动的感兴趣用户列表
    public Map<String, Object> getActivityInterests(Long activityId) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<UserActivityInterest> interests = interestMapper.findByActivityId(activityId);
            result.put("success", true);
            result.put("data", interests);
            result.put("count", interests.size());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取感兴趣列表失败: " + e.getMessage());
        }

        return result;
    }

    // 获取用户感兴趣的活动列表
    public Map<String, Object> getUserInterests(Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<UserActivityInterest> interests = interestMapper.findByUserId(userId);
            result.put("success", true);
            result.put("data", interests);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取用户感兴趣活动失败: " + e.getMessage());
        }

        return result;
    }

    // 检查用户是否对活动感兴趣
    public Map<String, Object> checkInterest(Long userId, Long activityId) {
        Map<String, Object> result = new HashMap<>();

        try {
            UserActivityInterest interest = interestMapper.findByUserAndActivity(userId, activityId);
            result.put("success", true);
            result.put("isInterested", interest != null);
            if (interest != null) {
                result.put("interestInfo", interest);
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "检查感兴趣状态失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 发送感兴趣通知给活动发起者
     */
    private void sendInterestNotification(Long activityId, Long interestedUserId) {
        try {
            // 获取活动信息
            Activity activity = activityService.getActivityDetailInternal(activityId);
            if (activity == null) {
                System.err.println("活动不存在，无法发送通知: " + activityId);
                return;
            }

            Long activityOwnerId = activity.getOrganizerId();

            // 如果用户对自己发起的活动感兴趣，不需要发送通知
            if (activityOwnerId.equals(interestedUserId)) {
                System.out.println("用户对自己发起的活动感兴趣，不发送通知");
                return;
            }

            // 获取感兴趣用户信息
            Map<String, Object> userInfoResult = userService.getUserInfo(interestedUserId);
            if (!(Boolean) userInfoResult.get("success")) {
                System.err.println("获取用户信息失败: " + interestedUserId);
                return;
            }

            Map<String, Object> userData = (Map<String, Object>) userInfoResult.get("data");
            String interestedUserName = (String) userData.get("nickname");
            if (interestedUserName == null || interestedUserName.trim().isEmpty()) {
                interestedUserName = "某用户";
            }

            // 构建通知内容
            String title = "有人对您的活动感兴趣";
            String content = "用户 " + interestedUserName + " 对您的活动《" + activity.getTitle() + "》表示了兴趣！";

            // 发送通知
            Map<String, Object> notificationResult = messageService.sendActivityNotification(
                    activityOwnerId, title, content, activityId);

            if ((Boolean) notificationResult.get("success")) {
                System.out.println("成功发送感兴趣通知给活动发起者: " + activityOwnerId);
            } else {
                System.err.println("发送感兴趣通知失败: " + notificationResult.get("message"));
            }

        } catch (Exception e) {
            System.err.println("发送感兴趣通知异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
}