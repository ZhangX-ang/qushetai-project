package com.qushetai.backend.service;

import com.qushetai.backend.entity.UserBehaviorLog;
import com.qushetai.backend.mapper.UserBehaviorLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class BehaviorLogService {

    @Autowired
    private UserBehaviorLogMapper behaviorLogMapper;

    // 记录行为日志
    public Map<String, Object> logBehavior(Map<String, Object> behaviorData) {
        Map<String, Object> result = new HashMap<>();

        try {
            UserBehaviorLog log = new UserBehaviorLog();

            // 设置基本字段 - 修复用户ID处理
            if (behaviorData.get("user_id") != null) {
                String userIdStr = behaviorData.get("user_id").toString();
                try {
                    // 处理 "anonymous" 和其他非数字用户ID
                    if ("anonymous".equals(userIdStr)) {
                        log.setUserId(0L); // 匿名用户设为0
                    } else {
                        log.setUserId(Long.parseLong(userIdStr));
                    }
                } catch (NumberFormatException e) {
                    // 如果解析失败，设为0（匿名用户）
                    log.setUserId(0L);
                    System.out.println("用户ID格式错误，使用匿名用户: " + userIdStr);
                }
            } else {
                log.setUserId(0L); // 没有用户ID也设为匿名
            }

            log.setSessionId((String) behaviorData.get("session_id"));
            log.setEvent((String) behaviorData.get("event"));
            log.setPage((String) behaviorData.get("page"));
            log.setReferrer((String) behaviorData.get("referrer"));

            // 设置业务字段
            if (behaviorData.get("item_id") != null) {
                try {
                    log.setItemId(Long.parseLong(behaviorData.get("item_id").toString()));
                } catch (NumberFormatException e) {
                    System.out.println("item_id格式错误: " + behaviorData.get("item_id"));
                    // 可以选择设为null或者默认值
                    log.setItemId(null);
                }
            }
            if (behaviorData.get("item_tags") != null) {
                log.setItemTags(behaviorData.get("item_tags").toString());
            }
            if (behaviorData.get("organizer_id") != null) {
                try {
                    log.setOrganizerId(Long.parseLong(behaviorData.get("organizer_id").toString()));
                } catch (NumberFormatException e) {
                    System.out.println("organizer_id格式错误: " + behaviorData.get("organizer_id"));
                    log.setOrganizerId(null);
                }
            }
            if (behaviorData.get("capacity") != null) {
                try {
                    log.setCapacity(Integer.parseInt(behaviorData.get("capacity").toString()));
                } catch (NumberFormatException e) {
                    System.out.println("capacity格式错误: " + behaviorData.get("capacity"));
                    log.setCapacity(0);
                }
            }
            if (behaviorData.get("dwell_ms") != null) {
                try {
                    log.setDwellMs(Integer.parseInt(behaviorData.get("dwell_ms").toString()));
                } catch (NumberFormatException e) {
                    System.out.println("dwell_ms格式错误: " + behaviorData.get("dwell_ms"));
                    log.setDwellMs(0);
                }
            }
            if (behaviorData.get("position") != null) {
                try {
                    log.setPosition(Integer.parseInt(behaviorData.get("position").toString()));
                } catch (NumberFormatException e) {
                    System.out.println("position格式错误: " + behaviorData.get("position"));
                    log.setPosition(0);
                }
            }
            log.setQuery((String) behaviorData.get("query"));
            if (behaviorData.get("filters") != null) {
                log.setFilters(behaviorData.get("filters").toString());
            }
            if (behaviorData.get("context") != null) {
                log.setContext(behaviorData.get("context").toString());
            }
            if (behaviorData.get("user_state") != null) {
                log.setUserState(behaviorData.get("user_state").toString());
            }

            // 时间戳
            if (behaviorData.get("timestamp") != null) {
                try {
                    log.setTimestamp(Long.parseLong(behaviorData.get("timestamp").toString()));
                } catch (NumberFormatException e) {
                    System.out.println("timestamp格式错误: " + behaviorData.get("timestamp"));
                    log.setTimestamp(System.currentTimeMillis());
                }
            } else {
                log.setTimestamp(System.currentTimeMillis());
            }

            int rows = behaviorLogMapper.insert(log);

            if (rows > 0) {
                result.put("success", true);
                result.put("message", "行为日志记录成功");
                result.put("logId", log.getId());
            } else {
                result.put("success", false);
                result.put("message", "行为日志记录失败");
            }

        } catch (Exception e) {
            System.out.println("行为日志记录异常: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "服务器错误: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取用户行为数据
     */
    public Map<String, Object> getUserBehaviorLogs(Long userId, int limit, Long startTime, Long endTime, String eventType) {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("【DEBUG】BehaviorLogService - 查询用户 " + userId + " 的行为数据");

            // 这里应该调用 UserBehaviorLogMapper 查询数据库
            // 由于 UserBehaviorLogMapper 目前没有相应方法，我们先返回模拟数据

            List<Map<String, Object>> logs = new ArrayList<>();

            // 模拟一些行为数据
            String[] events = {"view_item", "click_item", "interest", "search", "view_homepage"};
            String[] pages = {"home", "activity_detail", "search", "profile", "recommendation"};

            Random random = new Random();
            int logCount = Math.min(limit, 20); // 最多返回20条模拟数据

            for (int i = 0; i < logCount; i++) {
                Map<String, Object> log = new HashMap<>();
                log.put("id", 1000 + i);
                log.put("userId", userId);
                log.put("event", events[random.nextInt(events.length)]);
                log.put("page", pages[random.nextInt(pages.length)]);
                log.put("timestamp", System.currentTimeMillis() - (i * 3600000L)); // 按小时递减
                log.put("sessionId", "session_" + userId + "_" + i);

                // 如果是活动相关事件，添加活动ID
                if (log.get("event").equals("view_item") || log.get("event").equals("click_item") || log.get("event").equals("interest")) {
                    log.put("itemId", 100 + random.nextInt(50)); // 模拟活动ID 100-149
                }

                // 如果是搜索事件，添加搜索词
                if (log.get("event").equals("search")) {
                    String[] queries = {"篮球", "编程", "音乐", "运动", "摄影"};
                    log.put("query", queries[random.nextInt(queries.length)]);
                }

                logs.add(log);
            }

            // 如果指定了事件类型，进行过滤
            if (eventType != null && !eventType.trim().isEmpty()) {
                logs = logs.stream()
                        .filter(log -> eventType.equals(log.get("event")))
                        .collect(Collectors.toList());
            }

            // 如果指定了时间范围，进行过滤
            if (startTime != null) {
                logs = logs.stream()
                        .filter(log -> (Long) log.get("timestamp") >= startTime)
                        .collect(Collectors.toList());
            }
            if (endTime != null) {
                logs = logs.stream()
                        .filter(log -> (Long) log.get("timestamp") <= endTime)
                        .collect(Collectors.toList());
            }

            result.put("success", true);
            result.put("data", logs);
            result.put("count", logs.size());
            result.put("userId", userId);
            result.put("limit", limit);
            result.put("queryParams", Map.of(
                    "startTime", startTime,
                    "endTime", endTime,
                    "eventType", eventType
            ));

        } catch (Exception e) {
            System.err.println("【ERROR】查询用户行为数据失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "查询失败: " + e.getMessage());
        }

        return result;
    }
}