package com.qushetai.backend.service;

import com.qushetai.backend.entity.Activity;
import com.qushetai.backend.mapper.ActivityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

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
            System.out.println("【DEBUG】getActivityDetailInternal - 开始内部查询活动详情，ID: " + id);
            Activity activity = activityMapper.selectActivityById(id);
            System.out.println("【DEBUG】getActivityDetailInternal - 查询结果: " + (activity != null ? "找到活动" : "活动为null"));
            return activity;
        } catch (Exception e) {
            System.err.println("【ERROR】getActivityDetailInternal - 获取活动详情失败: ");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 创建活动
     */
    public Map<String, Object> createActivity(Activity activity) {
        Map<String, Object> result = new HashMap<>();
        try {
            System.out.println("【DEBUG】createActivity - 开始创建活动: " + activity.getTitle());

            // 设置默认状态
            if (activity.getStatus() == null || activity.getStatus().trim().isEmpty()) {
                activity.setStatus("active");
                System.out.println("【DEBUG】createActivity - 设置默认状态: active");
            }

            int rows = activityMapper.insertActivity(activity);
            if (rows > 0) {
                System.out.println("【DEBUG】createActivity - 活动创建成功，ID: " + activity.getId());
                result.put("success", true);
                result.put("message", "活动创建成功");
                result.put("data", activity);
            } else {
                System.out.println("【DEBUG】createActivity - 活动创建失败，影响行数: " + rows);
                result.put("success", false);
                result.put("message", "活动创建失败");
            }
        } catch (Exception e) {
            System.err.println("【ERROR】createActivity - 创建活动异常: ");
            e.printStackTrace();
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

        System.out.println("\n【DEBUG】========== ActivityService.getActivityList ==========");
        System.out.println("【DEBUG】参数: page=" + page + ", size=" + size);

        try {
            System.out.println("【DEBUG】计算offset...");
            int offset = (page - 1) * size;
            System.out.println("【DEBUG】offset: " + offset);

            System.out.println("【DEBUG】调用activityMapper.selectActivitiesWithPagination...");
            List<Activity> activities = activityMapper.selectActivitiesWithPagination(offset, size);
            System.out.println("【DEBUG】查询到 " + activities.size() + " 条活动记录");

            System.out.println("【DEBUG】调用activityMapper.countActivities...");
            int total = activityMapper.countActivities();
            System.out.println("【DEBUG】总活动数: " + total);

            // 检查数据
            if (activities != null) {
                System.out.println("【DEBUG】活动列表不为null");
                for (int i = 0; i < Math.min(activities.size(), 3); i++) {
                    Activity activity = activities.get(i);
                    System.out.println("【DEBUG】活动 " + i + ": id=" + activity.getId() + ", title=" + activity.getTitle());
                }
            } else {
                System.out.println("【WARN】活动列表为null！");
            }

            result.put("success", true);
            result.put("data", activities != null ? activities : new ArrayList<>());
            result.put("pagination", Map.of(
                    "currentPage", page,
                    "pageSize", size,
                    "total", total,
                    "totalPages", (int) Math.ceil((double) total / size)
            ));

            System.out.println("【DEBUG】返回成功响应");

        } catch (Exception e) {
            System.err.println("【ERROR】ActivityService.getActivityList 异常详情:");
            e.printStackTrace(); // 🔥 这行最重要！

            // 打印异常的完整信息
            System.err.println("【ERROR】异常类: " + e.getClass().getName());
            System.err.println("【ERROR】异常消息: " + e.getMessage());

            if (e.getCause() != null) {
                System.err.println("【ERROR】异常原因: " + e.getCause().getMessage());
                e.getCause().printStackTrace();
            }

            result.put("success", false);
            result.put("message", "获取活动列表失败: " +
                    (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }

        System.out.println("【DEBUG】ActivityService.getActivityList 完成\n");
        return result;
    }

    /**
     * 获取活动详情
     */
    public Map<String, Object> getActivityDetail(Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            System.out.println("【DEBUG】getActivityDetail - 开始查询活动详情，ID: " + id);
            Activity activity = activityMapper.selectActivityById(id);
            System.out.println("【DEBUG】getActivityDetail - 查询结果: " + (activity != null ? "找到活动" : "活动为null"));

            if (activity != null) {
                System.out.println("【DEBUG】getActivityDetail - 活动数据: " + activity);
                System.out.println("【DEBUG】getActivityDetail - 活动标题: " + activity.getTitle());
                System.out.println("【DEBUG】getActivityDetail - 活动组织者ID: " + activity.getOrganizerId());
                System.out.println("【DEBUG】getActivityDetail - 活动状态: " + activity.getStatus());
                result.put("success", true);
                result.put("data", activity);
            } else {
                System.out.println("【DEBUG】getActivityDetail - 活动不存在，ID: " + id);
                result.put("success", false);
                result.put("message", "活动不存在或已被删除");
            }
        } catch (Exception e) {
            // 🔥 关键：打印完整堆栈信息
            System.err.println("【ERROR】getActivityDetail - 获取活动详情异常: ");
            e.printStackTrace(); // 这行最重要！

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
            System.out.println("【DEBUG】updateActivity - 开始更新活动，ID: " + activity.getId());
            int rows = activityMapper.updateActivity(activity);
            if (rows > 0) {
                System.out.println("【DEBUG】updateActivity - 活动更新成功，影响行数: " + rows);
                result.put("success", true);
                result.put("message", "活动更新成功");
            } else {
                System.out.println("【DEBUG】updateActivity - 活动更新失败，影响行数: " + rows);
                result.put("success", false);
                result.put("message", "活动更新失败，请检查活动ID");
            }
        } catch (Exception e) {
            System.err.println("【ERROR】updateActivity - 更新活动异常: ");
            e.printStackTrace();
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
            System.out.println("【DEBUG】deleteActivity - 开始删除活动，ID: " + id);
            int rows = activityMapper.deleteActivity(id);
            if (rows > 0) {
                System.out.println("【DEBUG】deleteActivity - 活动删除成功，影响行数: " + rows);
                result.put("success", true);
                result.put("message", "活动删除成功");
            } else {
                System.out.println("【DEBUG】deleteActivity - 活动删除失败，影响行数: " + rows);
                result.put("success", false);
                result.put("message", "活动删除失败，请检查活动ID");
            }
        } catch (Exception e) {
            System.err.println("【ERROR】deleteActivity - 删除活动异常: ");
            e.printStackTrace();
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
            System.out.println("【DEBUG】getActivitiesByOrganizer - 开始查询组织者活动，organizerId: " + organizerId);
            List<Activity> activities = activityMapper.selectActivitiesByOrganizer(organizerId);
            System.out.println("【DEBUG】getActivitiesByOrganizer - 查询结果: " + activities.size() + " 条记录");
            result.put("success", true);
            result.put("data", activities);
        } catch (Exception e) {
            System.err.println("【ERROR】getActivitiesByOrganizer - 获取我的活动异常: ");
            e.printStackTrace();
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
            System.out.println("【DEBUG】hasPermission - 检查权限，userId: " + userId + ", activityId: " + activityId);
            Activity activity = activityMapper.selectActivityById(activityId);
            boolean hasPermission = activity != null &&
                    activity.getOrganizerId().equals(userId) &&
                    "active".equals(activity.getStatus()); // 改为字符串比较
            System.out.println("【DEBUG】hasPermission - 权限检查结果: " + hasPermission);
            return hasPermission;
        } catch (Exception e) {
            System.err.println("【ERROR】hasPermission - 权限检查异常: ");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取所有活动（管理员用）
     */
    public Map<String, Object> getAllActivities(Long currentUserId, int page, int size) {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("【DEBUG】getAllActivities - 开始获取所有活动，currentUserId: " + currentUserId + ", page: " + page + ", size: " + size);

            // 检查权限
            if (!userService.isAdmin(currentUserId)) {
                System.out.println("【DEBUG】getAllActivities - 权限不足，currentUserId: " + currentUserId);
                result.put("success", false);
                result.put("message", "权限不足");
                return result;
            }

            if (page < 1) page = 1;
            if (size < 1 || size > 100) size = 20;

            int offset = (page - 1) * size;
            List<Activity> activities = activityMapper.selectAllActivitiesWithPagination(offset, size);
            int total = activityMapper.countAllActivities();

            System.out.println("【DEBUG】getAllActivities - 查询结果: " + activities.size() + " 条记录，总计: " + total);

            result.put("success", true);
            result.put("data", activities);
            result.put("pagination", Map.of(
                    "currentPage", page,
                    "pageSize", size,
                    "total", total,
                    "totalPages", (int) Math.ceil((double) total / size)
            ));
        } catch (Exception e) {
            System.err.println("【ERROR】getAllActivities - 获取活动列表异常: ");
            e.printStackTrace();
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
            System.out.println("【DEBUG】takeDownActivity - 开始下架活动，currentUserId: " + currentUserId + ", activityId: " + activityId);

            if (!userService.isAdmin(currentUserId)) {
                System.out.println("【DEBUG】takeDownActivity - 权限不足，currentUserId: " + currentUserId);
                result.put("success", false);
                result.put("message", "权限不足");
                return result;
            }

            int rows = activityMapper.takeDownActivity(activityId);
            if (rows > 0) {
                System.out.println("【DEBUG】takeDownActivity - 活动下架成功，影响行数: " + rows);
                result.put("success", true);
                result.put("message", "活动下架成功");
            } else {
                System.out.println("【DEBUG】takeDownActivity - 活动下架失败，影响行数: " + rows);
                result.put("success", false);
                result.put("message", "活动下架失败，活动可能不存在");
            }
        } catch (Exception e) {
            System.err.println("【ERROR】takeDownActivity - 下架活动异常: ");
            e.printStackTrace();
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
            System.out.println("【DEBUG】restoreActivity - 开始恢复活动，currentUserId: " + currentUserId + ", activityId: " + activityId);

            if (!userService.isAdmin(currentUserId)) {
                System.out.println("【DEBUG】restoreActivity - 权限不足，currentUserId: " + currentUserId);
                result.put("success", false);
                result.put("message", "权限不足");
                return result;
            }

            int rows = activityMapper.restoreActivity(activityId);
            if (rows > 0) {
                System.out.println("【DEBUG】restoreActivity - 活动恢复成功，影响行数: " + rows);
                result.put("success", true);
                result.put("message", "活动恢复成功");
            } else {
                System.out.println("【DEBUG】restoreActivity - 活动恢复失败，影响行数: " + rows);
                result.put("success", false);
                result.put("message", "活动恢复失败，活动可能不存在");
            }
        } catch (Exception e) {
            System.err.println("【ERROR】restoreActivity - 恢复活动异常: ");
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "恢复活动失败: " + e.getMessage());
        }
        return result;
    }
}