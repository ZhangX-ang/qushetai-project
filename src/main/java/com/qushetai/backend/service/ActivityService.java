package com.qushetai.backend.service;

import com.qushetai.backend.entity.Activity;
import com.qushetai.backend.mapper.ActivityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ActivityService {

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private UserService userService;  // 添加这行注入UserService

    /**
     * 内部方法：获取活动详情（不包装响应格式，用于权限验证）
     * 这个方法专门用于权限检查，不返回包装的响应
     */
    public Activity getActivityDetailInternal(Long id) {
        try {
            return activityMapper.selectActivityById(id);
        } catch (Exception e) {
            System.err.println("获取活动详情失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 创建活动
     */
    public Map<String, Object> createActivity(Activity activity) {
        Map<String, Object> result = new HashMap<>();
        try {
            int rows = activityMapper.insertActivity(activity);
            if (rows > 0) {
                result.put("success", true);
                result.put("message", "活动创建成功");
                result.put("data", activity);
            } else {
                result.put("success", false);
                result.put("message", "活动创建失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "服务器错误: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取活动列表（分页）
     */
    public Map<String, Object> getActivityList(int page, int size) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * size;
            List<Activity> activities = activityMapper.selectActivitiesWithPagination(offset, size);
            int total = activityMapper.countActivities();

            result.put("success", true);
            result.put("data", activities);
            result.put("pagination", Map.of(
                    "currentPage", page,
                    "pageSize", size,
                    "total", total,
                    "totalPages", (int) Math.ceil((double) total / size)
            ));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取活动列表失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取活动详情
     */
    public Map<String, Object> getActivityDetail(Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            Activity activity = activityMapper.selectActivityById(id);
            if (activity != null) {
                result.put("success", true);
                result.put("data", activity);
            } else {
                result.put("success", false);
                result.put("message", "活动不存在或已被删除");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取活动详情失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 更新活动
     */
    public Map<String, Object> updateActivity(Activity activity) {
        Map<String, Object> result = new HashMap<>();
        try {
            int rows = activityMapper.updateActivity(activity);
            if (rows > 0) {
                result.put("success", true);
                result.put("message", "活动更新成功");
            } else {
                result.put("success", false);
                result.put("message", "活动更新失败，请检查活动ID");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "服务器错误: " + e.getMessage());
        }
        return result;
    }

    /**
     * 删除活动
     */
    public Map<String, Object> deleteActivity(Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            int rows = activityMapper.deleteActivity(id);
            if (rows > 0) {
                result.put("success", true);
                result.put("message", "活动删除成功");
            } else {
                result.put("success", false);
                result.put("message", "活动删除失败，请检查活动ID");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "服务器错误: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取用户发起的活动
     */
    public Map<String, Object> getActivitiesByOrganizer(Long organizerId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Activity> activities = activityMapper.selectActivitiesByOrganizer(organizerId);
            result.put("success", true);
            result.put("data", activities);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取我的活动失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 检查用户是否有权限操作活动
     */
    public boolean hasPermission(Long userId, Long activityId) {
        try {
            Activity activity = activityMapper.selectActivityById(activityId);
            return activity != null && activity.getOrganizerId().equals(userId);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取所有活动（管理员用）
     */
    public Map<String, Object> getAllActivities(Long currentUserId, int page, int size) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 检查权限
            if (!userService.isAdmin(currentUserId)) {
                result.put("success", false);
                result.put("message", "权限不足");
                return result;
            }

            if (page < 1) page = 1;
            if (size < 1 || size > 100) size = 20;

            int offset = (page - 1) * size;
            List<Activity> activities = activityMapper.selectAllActivitiesWithPagination(offset, size);
            int total = activityMapper.countAllActivities();

            result.put("success", true);
            result.put("data", activities);
            result.put("pagination", Map.of(
                    "currentPage", page,
                    "pageSize", size,
                    "total", total,
                    "totalPages", (int) Math.ceil((double) total / size)
            ));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取活动列表失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 下架活动
     */
    public Map<String, Object> takeDownActivity(Long currentUserId, Long activityId) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (!userService.isAdmin(currentUserId)) {
                result.put("success", false);
                result.put("message", "权限不足");
                return result;
            }

            int rows = activityMapper.takeDownActivity(activityId);
            if (rows > 0) {
                result.put("success", true);
                result.put("message", "活动下架成功");
            } else {
                result.put("success", false);
                result.put("message", "活动下架失败，活动可能不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "下架活动失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 恢复活动
     */
    public Map<String, Object> restoreActivity(Long currentUserId, Long activityId) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (!userService.isAdmin(currentUserId)) {
                result.put("success", false);
                result.put("message", "权限不足");
                return result;
            }

            int rows = activityMapper.restoreActivity(activityId);
            if (rows > 0) {
                result.put("success", true);
                result.put("message", "活动恢复成功");
            } else {
                result.put("success", false);
                result.put("message", "活动恢复失败，活动可能不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "恢复活动失败: " + e.getMessage());
        }
        return result;
    }
}