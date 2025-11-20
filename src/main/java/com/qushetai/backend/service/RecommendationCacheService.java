package com.qushetai.backend.service;

import com.qushetai.backend.entity.Activity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RecommendationCacheService {

    @Autowired
    private RedisService redisService;

    @Value("${recommendation.cache.ttl:3600}")
    private long cacheTtl;

    @Value("${recommendation.cache.enabled:true}")
    private boolean cacheEnabled;

    /**
     * 缓存用户推荐结果
     */
    public void cacheUserRecommendations(Long userId, List<Activity> recommendations) {
        if (!cacheEnabled) return;

        try {
            String key = redisService.generateRecommendationKey(userId);
            redisService.set(key, recommendations, cacheTtl, TimeUnit.SECONDS);
            System.out.println("已缓存用户 " + userId + " 的推荐结果，数量: " + recommendations.size());
        } catch (Exception e) {
            System.err.println("缓存推荐结果失败: " + e.getMessage());
        }
    }

    /**
     * 获取缓存的用户推荐结果
     */
    @SuppressWarnings("unchecked")
    public List<Activity> getCachedUserRecommendations(Long userId) {
        if (!cacheEnabled) return null;

        try {
            String key = redisService.generateRecommendationKey(userId);
            Object cached = redisService.get(key);
            if (cached instanceof List) {
                System.out.println("从缓存获取用户 " + userId + " 的推荐结果");
                return (List<Activity>) cached;
            }
        } catch (Exception e) {
            System.err.println("获取缓存推荐结果失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 删除用户的推荐缓存
     */
    public void deleteUserRecommendationCache(Long userId) {
        if (!cacheEnabled) return;

        try {
            String key = redisService.generateRecommendationKey(userId);
            redisService.delete(key);
            System.out.println("已删除用户 " + userId + " 的推荐缓存");
        } catch (Exception e) {
            System.err.println("删除推荐缓存失败: " + e.getMessage());
        }
    }

    /**
     * 缓存热门活动
     */
    public void cachePopularActivities(List<Activity> popularActivities) {
        if (!cacheEnabled) return;

        try {
            String key = redisService.generatePopularActivitiesKey();
            redisService.set(key, popularActivities, cacheTtl, TimeUnit.SECONDS);
            System.out.println("已缓存热门活动，数量: " + popularActivities.size());
        } catch (Exception e) {
            System.err.println("缓存热门活动失败: " + e.getMessage());
        }
    }

    /**
     * 获取缓存的热门活动
     */
    @SuppressWarnings("unchecked")
    public List<Activity> getCachedPopularActivities() {
        if (!cacheEnabled) return null;

        try {
            String key = redisService.generatePopularActivitiesKey();
            Object cached = redisService.get(key);
            if (cached instanceof List) {
                System.out.println("从缓存获取热门活动");
                return (List<Activity>) cached;
            }
        } catch (Exception e) {
            System.err.println("获取缓存热门活动失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 缓存用户会话信息
     */
    public void cacheUserSession(Long userId, Object sessionData) {
        try {
            String key = redisService.generateUserSessionKey(userId);
            redisService.set(key, sessionData, 24, TimeUnit.HOURS); // 会话缓存24小时
        } catch (Exception e) {
            System.err.println("缓存用户会话失败: " + e.getMessage());
        }
    }

    /**
     * 获取缓存的用户会话信息
     */
    public Object getCachedUserSession(Long userId) {
        try {
            String key = redisService.generateUserSessionKey(userId);
            return redisService.get(key);
        } catch (Exception e) {
            System.err.println("获取缓存用户会话失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 检查缓存是否启用
     */
    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    /**
     * 获取缓存统计信息
     */
    public Object getCacheStats() {
        try {
            // 这里可以添加更复杂的缓存统计逻辑
            return java.util.Map.of(
                    "cacheEnabled", cacheEnabled,
                    "cacheTtl", cacheTtl,
                    "redisStatus", "CONNECTED"
            );
        } catch (Exception e) {
            return java.util.Map.of(
                    "cacheEnabled", cacheEnabled,
                    "cacheTtl", cacheTtl,
                    "redisStatus", "ERROR: " + e.getMessage()
            );
        }
    }
}