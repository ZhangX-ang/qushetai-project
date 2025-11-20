package com.qushetai.backend.service;

import com.qushetai.backend.entity.UserBehaviorLog;
import com.qushetai.backend.entity.User;
import com.qushetai.backend.entity.Activity;
import com.qushetai.backend.mapper.UserBehaviorLogMapper;
import com.qushetai.backend.mapper.ActivityMapper;
import com.qushetai.backend.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class DataMonitorService {

    @Autowired
    private UserBehaviorLogMapper userBehaviorLogMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Random random = new Random();

    /**
     * 获取数据监控概览
     */
    public Map<String, Object> getDataOverview() {
        Map<String, Object> overview = new HashMap<>();

        try {
            // 用户数据
            int totalUsers = userMapper.countUsers();
            int activeUsers = userMapper.countActiveUsers();

            // 活动数据 - 修复：使用正确的方法名
            int totalActivities = activityMapper.countActivities();
            int activeActivities = activityMapper.countActivities();

            // 行为数据
            int totalBehaviorLogs = userBehaviorLogMapper.countTotalLogs();
            int todayBehaviorLogs = userBehaviorLogMapper.countTodayLogs();

            // 事件类型分布 - 修复类型转换问题
            Map<String, Map<String, Object>> eventDistributionRaw = userBehaviorLogMapper.getEventDistribution();
            Map<String, Integer> eventDistribution = new HashMap<>();

            if (eventDistributionRaw != null) {
                for (Map.Entry<String, Map<String, Object>> entry : eventDistributionRaw.entrySet()) {
                    String event = entry.getKey();
                    Map<String, Object> value = entry.getValue();
                    if (value != null && value.containsKey("count")) {
                        Object countObj = value.get("count");
                        int count = (countObj instanceof Long) ? ((Long) countObj).intValue() : (Integer) countObj;
                        eventDistribution.put(event, count);
                    }
                }
            }

            overview.put("success", true);
            overview.put("data", Map.of(
                    "users", Map.of(
                            "total", totalUsers,
                            "active", activeUsers
                    ),
                    "activities", Map.of(
                            "total", totalActivities,
                            "active", activeActivities
                    ),
                    "behaviorLogs", Map.of(
                            "total", totalBehaviorLogs,
                            "today", todayBehaviorLogs
                    ),
                    "eventDistribution", eventDistribution
            ));

        } catch (Exception e) {
            overview.put("success", false);
            overview.put("message", "获取数据概览失败: " + e.getMessage());
        }

        return overview;
    }

    /**
     * 获取推荐系统健康状态
     */
    public Map<String, Object> getRecommendationHealth() {
        Map<String, Object> health = new HashMap<>();

        try {
            // 检查是否有足够的数据进行推荐
            int userCount = userMapper.countUsers();
            int activityCount = activityMapper.countActivities();
            int behaviorCount = userBehaviorLogMapper.countTotalLogs();

            boolean hasEnoughData = userCount >= 5 && activityCount >= 3 && behaviorCount >= 10;

            health.put("success", true);
            health.put("data", Map.of(
                    "userCount", userCount,
                    "activityCount", activityCount,
                    "behaviorCount", behaviorCount,
                    "hasEnoughData", hasEnoughData,
                    "recommendationStatus", hasEnoughData ? "READY" : "NEEDS_MORE_DATA",
                    "message", hasEnoughData ?
                            "推荐系统数据充足" :
                            String.format("需要更多数据: 用户(%d/5), 活动(%d/3), 行为(%d/10)",
                                    userCount, activityCount, behaviorCount)
            ));

        } catch (Exception e) {
            health.put("success", false);
            health.put("message", "检查推荐系统健康状态失败: " + e.getMessage());
        }

        return health;
    }

    /**
     * 生成测试数据（开发用）
     */
    public Map<String, Object> generateTestData() {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("开始生成测试数据...");

            int generatedLogs = generateTestBehaviorLogs();

            result.put("success", true);
            result.put("message", "测试数据生成成功");
            result.put("generatedLogs", generatedLogs);
            result.put("totalLogs", userBehaviorLogMapper.countTotalLogs());

            System.out.println("测试数据生成完成，共生成 " + generatedLogs + " 条行为日志");

        } catch (Exception e) {
            System.err.println("生成测试数据失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "生成测试数据失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 生成完整的测试数据（用户+活动+行为）
     */
    public Map<String, Object> generateCompleteTestData() {
        Map<String, Object> result = new HashMap<>();

        try {
            System.out.println("开始生成完整测试数据...");

            // 1. 生成测试用户
            int generatedUsers = generateTestUsers();

            // 2. 生成测试活动
            int generatedActivities = generateTestActivities();

            // 3. 生成行为数据
            int generatedLogs = generateTestBehaviorLogs();

            result.put("success", true);
            result.put("message", "完整测试数据生成成功");
            result.put("generatedUsers", generatedUsers);
            result.put("generatedActivities", generatedActivities);
            result.put("generatedLogs", generatedLogs);
            result.put("totalUsers", userMapper.countUsers());
            result.put("totalActivities", activityMapper.countActivities());
            result.put("totalLogs", userBehaviorLogMapper.countTotalLogs());

            System.out.println("完整测试数据生成完成");

        } catch (Exception e) {
            System.err.println("生成完整测试数据失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "生成完整测试数据失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 生成测试用户
     */
    private int generateTestUsers() {
        int count = 0;
        try {
            String[] interests = {
                    "[\"篮球\", \"运动\"]",
                    "[\"摄影\", \"艺术\"]",
                    "[\"编程\", \"技术\"]",
                    "[\"音乐\", \"表演\"]",
                    "[\"读书\", \"学习\"]"
            };

            String[] nicknames = {"篮球爱好者", "摄影达人", "编程高手", "音乐家", "书虫"};

            for (int i = 0; i < 5; i++) {
                // 检查用户是否已存在
                String email = "test" + (i+1) + "@qushetai.com";
                User existingUser = userMapper.findByEmail(email);
                if (existingUser != null) {
                    System.out.println("测试用户已存在: " + email);
                    continue;
                }

                User user = new User();
                user.setEmail(email);
                user.setPasswordHash(passwordEncoder.encode("123456"));
                user.setNickname(nicknames[i]);
                user.setInterestTags(interests[i]);
                user.setIsAdmin(0);

                if (userMapper.insert(user) > 0) {
                    count++;
                    System.out.println("创建测试用户成功: " + email);
                }
            }
        } catch (Exception e) {
            System.err.println("生成测试用户失败: " + e.getMessage());
        }
        return count;
    }

    /**
     * 生成测试活动
     */
    private int generateTestActivities() {
        int count = 0;
        try {
            String[] titles = {
                    "周末篮球比赛",
                    "摄影外拍活动",
                    "编程学习小组",
                    "音乐分享会",
                    "读书交流会"
            };

            String[] descriptions = {
                    "组织周末篮球友谊赛，欢迎所有篮球爱好者参加",
                    "户外摄影活动，拍摄校园美景",
                    "编程学习小组，共同进步",
                    "音乐分享交流，展示才艺",
                    "读书心得分享，交流阅读体验"
            };

            String[] tags = {
                    "[\"篮球\", \"运动\", \"比赛\"]",
                    "[\"摄影\", \"艺术\", \"户外\"]",
                    "[\"编程\", \"学习\", \"技术\"]",
                    "[\"音乐\", \"表演\", \"艺术\"]",
                    "[\"读书\", \"学习\", \"交流\"]"
            };

            for (int i = 0; i < 5; i++) {
                Activity activity = new Activity();
                activity.setTitle(titles[i]);
                activity.setDescription(descriptions[i]);
                activity.setActivityType("兴趣活动");
                activity.setStartTime(LocalDateTime.now().plusDays(i));
                activity.setLocation("校园活动中心");
                activity.setOrganizerId(1L); // 使用第一个用户作为组织者
                activity.setMaxParticipants(20);
                activity.setCurrentParticipants(random.nextInt(10) + 1);
                activity.setTags(tags[i]);

                if (activityMapper.insertActivity(activity) > 0) {
                    count++;
                    System.out.println("创建测试活动成功: " + titles[i]);
                }
            }
        } catch (Exception e) {
            System.err.println("生成测试活动失败: " + e.getMessage());
        }
        return count;
    }

    /**
     * 生成测试行为日志
     */
    private int generateTestBehaviorLogs() {
        int generatedCount = 0;

        try {
            // 获取现有用户和活动
            int userCount = userMapper.countUsers();
            int activityCount = activityMapper.countActivities();

            if (userCount == 0 || activityCount == 0) {
                System.out.println("没有用户或活动数据，无法生成行为日志");
                return 0;
            }

            // 事件类型列表
            String[] events = {"view_item", "click_item", "interest_item", "search"};
            String[] pages = {"home", "activity_detail", "search", "profile"};

            // 为每个用户生成一些行为数据
            for (long userId = 1; userId <= userCount; userId++) {
                // 每个用户生成5-15条行为记录
                int logsPerUser = 5 + random.nextInt(10);

                for (int i = 0; i < logsPerUser; i++) {
                    UserBehaviorLog log = new UserBehaviorLog();
                    log.setUserId(userId);
                    log.setSessionId("test_session_" + userId + "_" + i);
                    log.setEvent(events[random.nextInt(events.length)]);
                    log.setPage(pages[random.nextInt(pages.length)]);
                    log.setTimestamp(System.currentTimeMillis() - random.nextInt(7 * 24 * 60 * 60 * 1000)); // 7天内随机时间

                    // 如果是活动相关事件，设置活动ID
                    if (log.getEvent().equals("view_item") ||
                            log.getEvent().equals("click_item") ||
                            log.getEvent().equals("interest_item")) {
                        log.setItemId((long) (1 + random.nextInt(activityCount)));
                    }

                    // 如果是搜索事件，设置搜索关键词
                    if (log.getEvent().equals("search")) {
                        String[] queries = {"篮球", "摄影", "编程", "音乐", "运动"};
                        log.setQuery(queries[random.nextInt(queries.length)]);
                    }

                    // 设置停留时间（如果是浏览事件）
                    if (log.getEvent().equals("view_item")) {
                        log.setDwellMs(1000 + random.nextInt(30000)); // 1-30秒
                    }

                    // 插入行为日志
                    int inserted = userBehaviorLogMapper.insert(log);
                    if (inserted > 0) {
                        generatedCount++;
                    }
                }
            }

            System.out.println("生成测试行为日志完成，共生成 " + generatedCount + " 条记录");

        } catch (Exception e) {
            System.err.println("生成测试行为日志失败: " + e.getMessage());
            throw e;
        }

        return generatedCount;
    }

    /**
     * 清除测试数据（开发用）
     */
    public Map<String, Object> clearTestData() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 注意：这里我们只是清除行为日志，不会清除用户和活动数据
            int deletedCount = userBehaviorLogMapper.deleteAllTestData();

            result.put("success", true);
            result.put("message", "测试数据清除成功");
            result.put("deletedCount", deletedCount);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "清除测试数据失败: " + e.getMessage());
        }

        return result;
    }
}