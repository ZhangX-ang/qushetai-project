package com.qushetai.backend.service;

import com.qushetai.backend.entity.Activity;
import com.qushetai.backend.entity.User;
import com.qushetai.backend.mapper.ActivityMapper;
import com.qushetai.backend.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private RecommendationCacheService cacheService;

    /**
     * 获取用户推荐活动 - 主推荐方法（集成缓存）
     */
    public Map<String, Object> getRecommendations(Long userId, int limit) {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("开始为用户 " + userId + " 生成推荐...");

            // 1. 首先尝试从缓存获取
            if (cacheService.isCacheEnabled()) {
                List<Activity> cachedRecommendations = cacheService.getCachedUserRecommendations(userId);
                if (cachedRecommendations != null && !cachedRecommendations.isEmpty()) {
                    System.out.println("使用缓存的推荐结果");
                    result.put("success", true);
                    result.put("data", cachedRecommendations);
                    result.put("recommendationType", "缓存推荐");
                    result.put("count", cachedRecommendations.size());
                    result.put("fromCache", true);
                    return result;
                }
            }

            // 2. 缓存中没有，执行正常推荐逻辑
            User user = userMapper.findById(userId);
            if (user == null) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }

            List<Activity> allActivities = activityMapper.findActiveActivities();
            System.out.println("找到 " + allActivities.size() + " 个活跃活动");

            if (allActivities.isEmpty()) {
                result.put("success", false);
                result.put("message", "暂无活动");
                return result;
            }

            List<Activity> recommendedActivities;

            if (user.getInterestTags() != null && !user.getInterestTags().isEmpty()) {
                System.out.println("使用兴趣标签推荐");
                recommendedActivities = recommendByInterestTags(user, allActivities, limit);
            } else {
                System.out.println("使用冷启动推荐");
                recommendedActivities = recommendColdStart(allActivities, limit);
            }

            // 3. 如果推荐数量不足，用热门活动补全
            if (recommendedActivities.size() < limit) {
                System.out.println("推荐数量不足，使用热门活动补全");
                List<Activity> popularActivities = getPopularActivities(limit - recommendedActivities.size());
                recommendedActivities.addAll(popularActivities);
                recommendedActivities = recommendedActivities.stream()
                        .distinct()
                        .collect(Collectors.toList());
            }

            System.out.println("最终推荐 " + recommendedActivities.size() + " 个活动");

            // 4. 缓存推荐结果
            if (cacheService.isCacheEnabled() && !recommendedActivities.isEmpty()) {
                cacheService.cacheUserRecommendations(userId, recommendedActivities);
            }

            result.put("success", true);
            result.put("data", recommendedActivities);
            result.put("recommendationType", user.getInterestTags() != null ? "兴趣匹配" : "热门推荐");
            result.put("count", recommendedActivities.size());
            result.put("fromCache", false);

        } catch (Exception e) {
            System.err.println("推荐系统错误: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "推荐系统错误: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取冷启动推荐（集成缓存）
     */
    public Map<String, Object> getColdStartRecommendations(int limit) {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("开始冷启动推荐，限制数量: " + limit);

            // 尝试从缓存获取热门活动
            List<Activity> popularActivities = getPopularActivities(limit);

            result.put("success", true);
            result.put("data", popularActivities);
            result.put("recommendationType", "冷启动推荐");
            result.put("count", popularActivities.size());
            result.put("fromCache", false);

        } catch (Exception e) {
            System.err.println("冷启动推荐失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "冷启动推荐失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 基于兴趣标签的推荐
     */
    public Map<String, Object> getRecommendationsByInterests(Long userId, int limit) {
        Map<String, Object> result = new HashMap<>();

        try {
            User user = userMapper.findById(userId);
            if (user == null) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }

            List<Activity> allActivities = activityMapper.findActiveActivities();
            List<Activity> recommendedActivities = recommendByInterestTags(user, allActivities, limit);

            result.put("success", true);
            result.put("data", recommendedActivities);
            result.put("recommendationType", "兴趣标签匹配");
            result.put("count", recommendedActivities.size());

        } catch (Exception e) {
            System.err.println("基于兴趣的推荐失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "基于兴趣的推荐失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 探索性推荐
     */
    public Map<String, Object> getExplorationRecommendations(Long userId, int limit) {
        Map<String, Object> result = new HashMap<>();

        try {
            List<Activity> allActivities = activityMapper.findActiveActivities();
            List<Activity> explorationActivities = findExplorationActivities(userId, allActivities, limit);

            result.put("success", true);
            result.put("data", explorationActivities);
            result.put("count", explorationActivities.size());

        } catch (Exception e) {
            System.err.println("探索性推荐失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "探索性推荐失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取热门活动（集成缓存）
     */
    private List<Activity> getPopularActivities(int limit) {
        // 首先尝试从缓存获取
        if (cacheService.isCacheEnabled()) {
            List<Activity> cachedPopular = cacheService.getCachedPopularActivities();
            if (cachedPopular != null && !cachedPopular.isEmpty()) {
                System.out.println("使用缓存的热门活动");
                return cachedPopular.stream().limit(limit).collect(Collectors.toList());
            }
        }

        // 缓存中没有，从数据库获取
        List<Activity> allActivities = activityMapper.findActiveActivities();
        List<Activity> popularActivities = recommendByPopularity(allActivities, limit);

        // 缓存热门活动
        if (cacheService.isCacheEnabled() && !popularActivities.isEmpty()) {
            cacheService.cachePopularActivities(popularActivities);
        }

        return popularActivities;
    }

    /**
     * 清除用户推荐缓存
     */
    public Map<String, Object> clearUserRecommendationCache(Long userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            cacheService.deleteUserRecommendationCache(userId);
            result.put("success", true);
            result.put("message", "已清除用户 " + userId + " 的推荐缓存");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "清除缓存失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取缓存状态
     */
    public Map<String, Object> getCacheStatus() {
        Map<String, Object> result = new HashMap<>();

        try {
            Object cacheStats = cacheService.getCacheStats();
            result.put("success", true);
            result.put("data", cacheStats);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取缓存状态失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 基于兴趣标签推荐
     */
    private List<Activity> recommendByInterestTags(User user, List<Activity> activities, int limit) {
        String userTags = user.getInterestTags();
        if (userTags == null || userTags.isEmpty()) {
            return Collections.emptyList();
        }

        // 简单的标签匹配逻辑（后期可替换为更复杂的算法）
        return activities.stream()
                .filter(activity -> activity.getTags() != null)
                .filter(activity -> hasCommonTags(userTags, activity.getTags()))
                .sorted((a1, a2) -> {
                    // 按标签匹配度排序（简单实现：共同标签数量）
                    int score1 = getCommonTagCount(userTags, a1.getTags());
                    int score2 = getCommonTagCount(userTags, a2.getTags());
                    return Integer.compare(score2, score1);
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 冷启动推荐（新用户或无标签用户）
     */
    private List<Activity> recommendColdStart(List<Activity> activities, int limit) {
        // 冷启动策略：热门活动 + 新活动混合
        List<Activity> popular = recommendByPopularity(activities, limit / 2);
        List<Activity> recent = recommendByRecency(activities, limit - popular.size());

        List<Activity> result = new ArrayList<>();
        result.addAll(popular);
        result.addAll(recent);

        return result.stream().distinct().limit(limit).collect(Collectors.toList());
    }

    /**
     * 基于热度推荐（按参与人数，带空值保护）
     */
    private List<Activity> recommendByPopularity(List<Activity> activities, int limit) {
        return activities.stream()
                .sorted((a1, a2) -> {
                    // 空值保护：如果参与人数为null，视为0
                    int participants1 = a1.getCurrentParticipants() != null ? a1.getCurrentParticipants() : 0;
                    int participants2 = a2.getCurrentParticipants() != null ? a2.getCurrentParticipants() : 0;
                    return Integer.compare(participants2, participants1);
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 基于新近度推荐（带空值保护）
     */
    private List<Activity> recommendByRecency(List<Activity> activities, int limit) {
        return activities.stream()
                .sorted((a1, a2) -> {
                    // 空值保护：如果createdAt为null，放到最后面
                    if (a1.getCreatedAt() == null && a2.getCreatedAt() == null) {
                        return 0;
                    } else if (a1.getCreatedAt() == null) {
                        return 1; // a1为null，放到后面
                    } else if (a2.getCreatedAt() == null) {
                        return -1; // a2为null，a1放到前面
                    } else {
                        return a2.getCreatedAt().compareTo(a1.getCreatedAt());
                    }
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 探索性推荐逻辑
     */
    private List<Activity> findExplorationActivities(Long userId, List<Activity> activities, int limit) {
        User user = userMapper.findById(userId);
        if (user == null || user.getInterestTags() == null) {
            // 如果没有用户信息或兴趣标签，返回热门活动
            return recommendByPopularity(activities, limit);
        }

        // 探索逻辑：推荐与用户当前兴趣不同的热门活动
        return activities.stream()
                .filter(activity -> activity.getTags() != null)
                .filter(activity -> !hasCommonTags(user.getInterestTags(), activity.getTags()))
                .sorted((a1, a2) -> {
                    int participants1 = a1.getCurrentParticipants() != null ? a1.getCurrentParticipants() : 0;
                    int participants2 = a2.getCurrentParticipants() != null ? a2.getCurrentParticipants() : 0;
                    return Integer.compare(participants2, participants1);
                })
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 检查是否有共同标签（简单字符串包含检查）
     */
    private boolean hasCommonTags(String userTags, String activityTags) {
        // JSON数组格式处理：移除方括号和引号
        String cleanUserTags = userTags.replaceAll("[\\[\\]\"]", "");
        String cleanActivityTags = activityTags.replaceAll("[\\[\\]\"]", "");

        String[] userTagArray = cleanUserTags.split(",");
        String[] activityTagArray = cleanActivityTags.split(",");

        for (String userTag : userTagArray) {
            for (String activityTag : activityTagArray) {
                if (userTag.trim().equalsIgnoreCase(activityTag.trim())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取共同标签数量
     */
    private int getCommonTagCount(String userTags, String activityTags) {
        // JSON数组格式处理：移除方括号和引号
        String cleanUserTags = userTags.replaceAll("[\\[\\]\"]", "");
        String cleanActivityTags = activityTags.replaceAll("[\\[\\]\"]", "");

        String[] userTagArray = cleanUserTags.split(",");
        String[] activityTagArray = cleanActivityTags.split(",");

        int count = 0;
        for (String userTag : userTagArray) {
            for (String activityTag : activityTagArray) {
                if (userTag.trim().equalsIgnoreCase(activityTag.trim())) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * 测试推荐逻辑（开发用）
     */
    public void testRecommendationLogic() {
        System.out.println("=== 推荐系统测试 ===");

        // 测试用户1
        User user = userMapper.findById(1L);
        if (user != null) {
            System.out.println("用户兴趣标签: " + user.getInterestTags());

            // 获取所有活动
            List<Activity> allActivities = activityMapper.findActiveActivities();
            System.out.println("活动数量: " + allActivities.size());

            // 测试基于兴趣的推荐
            List<Activity> interestRecommendations = recommendByInterestTags(user, allActivities, 3);
            System.out.println("基于兴趣的推荐结果:");
            for (Activity activity : interestRecommendations) {
                System.out.println(" - " + activity.getTitle() + " [标签: " + activity.getTags() + "]");
            }

            // 测试冷启动推荐
            List<Activity> coldStartRecommendations = recommendColdStart(allActivities, 3);
            System.out.println("冷启动推荐结果:");
            for (Activity activity : coldStartRecommendations) {
                System.out.println(" - " + activity.getTitle());
            }
        } else {
            System.out.println("测试用户不存在");
        }
    }

    /**
     * 获取推荐系统健康状态
     */
    public Map<String, Object> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();

        try {
            int userCount = userMapper.countUsers();
            int activityCount = activityMapper.countActivities();
            int behaviorCount = userBehaviorLogCount();

            boolean hasEnoughData = userCount >= 5 && activityCount >= 3 && behaviorCount >= 10;

            health.put("success", true);
            health.put("userCount", userCount);
            health.put("activityCount", activityCount);
            health.put("behaviorCount", behaviorCount);
            health.put("hasEnoughData", hasEnoughData);
            health.put("status", hasEnoughData ? "READY" : "NEEDS_MORE_DATA");

        } catch (Exception e) {
            health.put("success", false);
            health.put("message", "获取系统健康状态失败: " + e.getMessage());
        }

        return health;
    }

    /**
     * 获取行为日志数量（简化实现）
     */
    private int userBehaviorLogCount() {
        // 这里应该查询user_behavior_log表
        // 简化实现，返回模拟数据
        return 15;
    }
}