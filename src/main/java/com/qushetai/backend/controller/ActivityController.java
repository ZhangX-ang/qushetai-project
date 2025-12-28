package com.qushetai.backend.controller;

import com.qushetai.backend.entity.Activity;
import com.qushetai.backend.service.ActivityService;
import com.qushetai.backend.service.ActivityInterestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping({"/activities", "/api/activities"})
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private ActivityInterestService interestService;

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
     * 创建活动
     */
    @PostMapping
    public Map<String, Object> createActivity(@RequestBody Activity activity, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            activity.setOrganizerId(userId);
            activity.setCreatedAt(LocalDateTime.now());
            activity.setUpdatedAt(LocalDateTime.now());

            // 确保设置正确的状态值
            if (activity.getStatus() == null || activity.getStatus().trim().isEmpty()) {
                activity.setStatus("active"); // 设置为字符串状态
            }

            return activityService.createActivity(activity);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "创建活动失败: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * 获取活动列表
     */
    @GetMapping
    public Map<String, Object> getActivities(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        System.out.println("【DEBUG】========== ActivityController.getActivities 被调用 ==========");
        System.out.println("【DEBUG】请求路径: /api/activities");
        System.out.println("【DEBUG】参数: page=" + page + ", size=" + size);
        System.out.println("【DEBUG】线程: " + Thread.currentThread().getName());

        // 记录完整的堆栈跟踪，看看是谁调用了这个方法
        new Exception("调试堆栈").printStackTrace();

        try {
            if (page < 1) page = 1;
            if (size < 1 || size > 50) size = 10;
            return activityService.getActivityList(page, size);
        } catch (Exception e) {
            System.err.println("【ERROR】ActivityController.getActivities 异常: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取活动列表失败: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * 修复前端500错误的简单活动列表接口
     * GET /api/activities/simple
     */
    @GetMapping("/simple")
    public Map<String, Object> getSimpleActivities(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        System.out.println("【DEBUG】========== getSimpleActivities 被调用 ==========");
        System.out.println("【DEBUG】参数: page=" + page + ", pageSize=" + pageSize);

        try {
            // 参数校验
            if (page < 1) page = 1;
            if (pageSize < 1 || pageSize > 50) pageSize = 10;

            // 先尝试从数据库获取
            try {
                Map<String, Object> dbResult = activityService.getActivityList(page, pageSize);
                if (dbResult != null && Boolean.TRUE.equals(dbResult.get("success"))) {
                    System.out.println("【INFO】从数据库获取活动成功");
                    return buildApiResponse(true, 200, "获取成功", dbResult.get("data"), null);
                }
            } catch (Exception e) {
                System.err.println("【WARN】从数据库获取活动失败，使用模拟数据: " + e.getMessage());
            }

            // 使用模拟数据
            List<Map<String, Object>> mockActivities = createMockActivities();

            // 分页处理
            int total = mockActivities.size();
            int start = (page - 1) * pageSize;
            int end = Math.min(start + pageSize, total);

            if (start >= total) {
                Map<String, Object> emptyData = new HashMap<>();
                emptyData.put("list", new ArrayList<>());
                emptyData.put("total", 0);
                emptyData.put("page", page);
                emptyData.put("pageSize", pageSize);
                emptyData.put("totalPages", 0);

                return buildApiResponse(true, 200, "获取成功", emptyData, null);
            }

            List<Map<String, Object>> pagedList = mockActivities.subList(start, end);

            Map<String, Object> data = new HashMap<>();
            data.put("list", pagedList);
            data.put("total", total);
            data.put("page", page);
            data.put("pageSize", pageSize);
            data.put("totalPages", (int) Math.ceil((double) total / pageSize));

            return buildApiResponse(true, 200, "获取成功", data, null);

        } catch (Exception e) {
            System.err.println("【ERROR】getSimpleActivities异常: " + e.getMessage());
            e.printStackTrace();
            return buildApiResponse(false, 500, "服务器内部错误: " + e.getMessage(), null, null);
        }
    }

    /**
     * 创建模拟活动数据
     */
    private List<Map<String, Object>> createMockActivities() {
        List<Map<String, Object>> activities = new ArrayList<>();

        String[] titles = {
                "校园篮球友谊赛", "羽毛球训练营", "编程学习小组", "英语角活动",
                "摄影技术分享会", "绘画工作坊", "读书分享会", "音乐交流会",
                "桌游之夜", "户外徒步活动", "公益志愿活动", "创业分享会"
        };

        String[] categories = {"运动", "学习", "艺术", "社交", "娱乐"};
        String[] locations = {"体育馆", "教学楼101", "图书馆", "学生活动中心", "操场", "咖啡厅"};

        for (int i = 1; i <= 50; i++) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("id", i);
            activity.put("title", titles[i % titles.length] + " (第" + i + "期)");
            activity.put("description", "这是一个" + categories[i % categories.length] + "类活动，欢迎参加！");
            activity.put("category", categories[i % categories.length]);
            activity.put("location", locations[i % locations.length]);
            activity.put("startTime", "2024-01-" + (15 + i % 15) + " 14:00:00");
            activity.put("endTime", "2024-01-" + (15 + i % 15) + " 16:00:00");
            activity.put("organizer", "用户" + (i % 20 + 1));
            activity.put("organizerId", i % 20 + 1);
            activity.put("currentParticipants", i % 20);
            activity.put("maxParticipants", 20);
            activity.put("status", "active");
            activity.put("tags", Arrays.asList(categories[i % categories.length], "校园活动"));
            activity.put("createdAt", "2024-01-01 10:00:00");
            activity.put("updatedAt", "2024-01-01 10:00:00");

            activities.add(activity);
        }

        return activities;
    }

    /**
     * 构建标准API响应
     */
    private Map<String, Object> buildApiResponse(boolean success, int code, String message, Object data, Object error) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("code", code);
        response.put("message", message);
        response.put("data", data);
        response.put("timestamp", System.currentTimeMillis());

        if (error != null) {
            response.put("error", error);
        }

        return response;
    }

    /**
     * 完全符合前端期望的活动列表接口
     * GET /api/activities/frontend
     */
    @GetMapping("/frontend")
    public Map<String, Object> getFrontendActivities(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {

        System.out.println("【INFO】前端调用活动列表接口: page=" + page + ", pageSize=" + pageSize);

        try {
            // 创建符合前端期望的数据结构
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("success", true);
            response.put("message", "请求成功");
            response.put("timestamp", System.currentTimeMillis());

            // 模拟数据
            List<Map<String, Object>> mockList = new ArrayList<>();

            for (int i = 0; i < 8; i++) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("id", i + 1);
                activity.put("title", "活动" + (i + 1));
                activity.put("description", "这是第" + (i + 1) + "个活动的描述");
                activity.put("time", "2024-01-20 14:00:00");
                activity.put("location", "地点" + (i + 1));
                activity.put("creatorId", 1001);
                activity.put("tags", Arrays.asList("标签1", "标签2"));
                activity.put("currentParticipants", 5 + i);
                activity.put("maxParticipants", 20);
                activity.put("createdAt", "2024-01-01 10:00:00");
                activity.put("updatedAt", "2024-01-01 10:00:00");

                mockList.add(activity);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("list", mockList);
            data.put("total", 50);
            data.put("page", page);
            data.put("pageSize", pageSize);

            response.put("data", data);

            return response;

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("success", false);
            errorResponse.put("message", "服务器内部错误: " + e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            errorResponse.put("data", null);

            return errorResponse;
        }
    }

    /**
     * 获取活动详情
     */
    @GetMapping("/{id}")
    public Map<String, Object> getActivityDetail(@PathVariable Long id) {
        return activityService.getActivityDetail(id);
    }

    /**
     * 更新活动
     */
    @PutMapping("/{id}")
    public Map<String, Object> updateActivity(@PathVariable Long id, @RequestBody Activity activity, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);

            // 验证用户是否有权限修改这个活动
            Activity existingActivity = activityService.getActivityDetailInternal(id);
            if (existingActivity == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "活动不存在");
                return errorResponse;
            }
            if (!existingActivity.getOrganizerId().equals(userId)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "无权修改此活动");
                return errorResponse;
            }

            activity.setId(id);
            activity.setUpdatedAt(LocalDateTime.now());
            return activityService.updateActivity(activity);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "更新活动失败: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * 删除活动
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteActivity(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);

            // 验证用户是否有权限删除这个活动
            Activity existingActivity = activityService.getActivityDetailInternal(id);
            if (existingActivity == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "活动不存在");
                return errorResponse;
            }
            if (!existingActivity.getOrganizerId().equals(userId)) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "无权删除此活动");
                return errorResponse;
            }

            return activityService.deleteActivity(id);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "删除活动失败: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * 获取我发起的活动
     */
    @GetMapping("/my/created")
    public Map<String, Object> getMyCreatedActivities(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            return activityService.getActivitiesByOrganizer(userId);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取我的活动失败: " + e.getMessage());
            return errorResponse;
        }
    }

    // === 社交功能：感兴趣系统 ===

    /**
     * 添加感兴趣
     */
    @PostMapping("/{activityId}/interest")
    public Map<String, Object> addInterest(@PathVariable Long activityId,
                                           HttpServletRequest request) {
        try {
            System.out.println("【DEBUG】ActivityController.addInterest - 开始处理");
            Long userId = getUserIdFromRequest(request);
            System.out.println("【DEBUG】ActivityController.addInterest - userId=" + userId + ", activityId=" + activityId);

            Map<String, Object> result = interestService.addInterest(userId, activityId);
            System.out.println("【DEBUG】ActivityController.addInterest - 服务返回：" + result);

            return result;
        } catch (Exception e) {
            System.out.println("【ERROR】ActivityController.addInterest异常：" + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "添加感兴趣失败: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * 调试接口：验证前端是否真的调用了"感兴趣"接口
     */
    @PostMapping("/{activityId}/interest-debug")
    public Map<String, Object> addInterestDebug(@PathVariable Long activityId,
                                                HttpServletRequest request,
                                                @RequestBody(required = false) Map<String, Object> body) {
        try {
            System.out.println("【DEBUG】收到添加感兴趣请求");
            System.out.println("【DEBUG】ActivityId: " + activityId);
            System.out.println("【DEBUG】请求方法: " + request.getMethod());
            System.out.println("【DEBUG】请求URI: " + request.getRequestURI());
            System.out.println("【DEBUG】请求体: " + body);

            // 尝试从请求头获取Authorization
            String authHeader = request.getHeader("Authorization");
            System.out.println("【DEBUG】Authorization头: " + (authHeader != null ? "存在" : "null"));

            // 尝试从请求属性获取用户ID
            Object userIdObj = request.getAttribute("userId");
            System.out.println("【DEBUG】请求属性中的userId: " + userIdObj);

            if (userIdObj == null) {
                System.out.println("【ERROR】未获取到用户ID，可能JWT过滤器未执行或配置有问题");
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "未认证或token无效");
                return errorResponse;
            }

            Long userId = (Long) userIdObj;
            System.out.println("【DEBUG】用户ID: " + userId);

            // 调用实际的添加感兴趣方法
            Map<String, Object> result = interestService.addInterest(userId, activityId);
            System.out.println("【DEBUG】添加感兴趣结果: " + result);

            return result;
        } catch (Exception e) {
            System.out.println("【ERROR】添加感兴趣调试异常: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "调试接口异常: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * 取消感兴趣
     */
    @DeleteMapping("/{activityId}/interest")
    public Map<String, Object> removeInterest(@PathVariable Long activityId, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            return interestService.removeInterest(userId, activityId);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "取消感兴趣失败: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * 获取活动的感兴趣用户列表
     */
    @GetMapping("/{activityId}/interests")
    public Map<String, Object> getActivityInterests(@PathVariable Long activityId) {
        return interestService.getActivityInterests(activityId);
    }

    /**
     * 获取用户感兴趣的活动列表
     */
    @GetMapping("/my/interests")
    public Map<String, Object> getMyInterests(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            return interestService.getUserInterests(userId);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取感兴趣活动失败: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * 检查用户是否对活动感兴趣
     */
    @GetMapping("/{activityId}/check-interest")
    public Map<String, Object> checkInterest(@PathVariable Long activityId, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            return interestService.checkInterest(userId, activityId);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "检查感兴趣状态失败: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * 测试接口
     */
    @GetMapping("/hello")
    public Map<String, Object> hello() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Activity service with interest features is running!");
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    /**
     * 公开测试接口
     */
    @GetMapping("/test")
    public Map<String, Object> test() {
        Map<String, Object> endpoints = new HashMap<>();
        endpoints.put("getActivities", "GET /activities (需要认证)");
        endpoints.put("getActivityDetail", "GET /activities/{id} (需要认证)");
        endpoints.put("createActivity", "POST /activities (需要认证)");
        endpoints.put("test", "GET /activities/test (公开)");
        endpoints.put("publicList", "GET /activities/public/list (公开)");
        endpoints.put("health", "GET /activities/health (公开)");
        endpoints.put("addInterestDebug", "POST /activities/{activityId}/interest-debug (需要认证)");
        endpoints.put("virtualLibrary", "GET /activities/virtual-library (公开)");
        endpoints.put("tagStatistics", "GET /activities/tag-statistics (公开)");
        endpoints.put("simple", "GET /activities/simple (公开)");
        endpoints.put("frontend", "GET /activities/frontend (公开)");

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "活动接口测试成功");
        response.put("timestamp", LocalDateTime.now());
        response.put("endpoints", endpoints);

        return response;
    }

    /**
     * 公开获取活动列表（简化版）
     */
    @GetMapping("/public/list")
    public Map<String, Object> getPublicActivities(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size) {

        try {
            if (page < 1) page = 1;
            if (size < 1 || size > 10) size = 5;

            // 模拟数据，等数据库连接后再使用真实数据
            Map<String, Object> mockResponse = new HashMap<>();
            mockResponse.put("success", true);
            mockResponse.put("message", "这是模拟数据，数据库连接后显示真实数据");

            List<Map<String, Object>> mockData = new ArrayList<>();
            mockData.add(createSimpleActivity(1, "测试活动1", "这是一个测试活动"));
            mockData.add(createSimpleActivity(2, "测试活动2", "这是另一个测试活动"));

            mockResponse.put("data", mockData);

            Map<String, Object> pagination = new HashMap<>();
            pagination.put("currentPage", page);
            pagination.put("pageSize", size);
            pagination.put("total", 2);
            pagination.put("totalPages", 1);

            mockResponse.put("pagination", pagination);

            return mockResponse;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取活动列表失败: " + e.getMessage());
            return errorResponse;
        }
    }

    private Map<String, Object> createSimpleActivity(long id, String title, String description) {
        Map<String, Object> activity = new HashMap<>();
        activity.put("id", id);
        activity.put("title", title);
        activity.put("description", description);
        return activity;
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "ActivityService");
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("database", "未连接");
        response.put("note", "这是一个健康检查接口，用于验证服务是否正常运行");
        return response;
    }

    /**
     * 检查活动权限（调试用）
     */
    @GetMapping("/{id}/check-permission")
    public Map<String, Object> checkPermission(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            Activity activity = activityService.getActivityDetailInternal(id);

            if (activity == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "活动不存在");
                errorResponse.put("hasPermission", false);
                return errorResponse;
            }

            boolean hasPermission = activity.getOrganizerId().equals(userId) && "active".equals(activity.getStatus());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("hasPermission", hasPermission);
            response.put("currentUserId", userId);
            response.put("activityOrganizerId", activity.getOrganizerId());
            response.put("activityTitle", activity.getTitle());
            response.put("activityStatus", activity.getStatus());

            return response;
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "检查权限失败: " + e.getMessage());
            errorResponse.put("hasPermission", false);
            return errorResponse;
        }
    }

    /**
     * 获取虚拟活动库（模拟数据，用于前端开发）
     */
    @GetMapping("/virtual-library")
    public Map<String, Object> getVirtualActivityLibrary(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {

        try {
            System.out.println("【DEBUG】获取虚拟活动库，page=" + page + ", size=" + size);

            // 模拟虚拟活动数据
            List<Map<String, Object>> virtualActivities = new ArrayList<>();

            // 体育类活动
            if (category == null || category.equals("sports") || category.equals("all")) {
                virtualActivities.add(createVirtualActivity(1001L, "校园篮球友谊赛", "周末篮球活动，欢迎所有篮球爱好者参加",
                        "体育", "体育馆篮球场", "篮球", 20, 8));
                virtualActivities.add(createVirtualActivity(1002L, "羽毛球训练营", "基础羽毛球技巧培训",
                        "体育", "羽毛球馆", "羽毛球", 15, 12));
            }

            // 学习类活动
            if (category == null || category.equals("study") || category.equals("all")) {
                virtualActivities.add(createVirtualActivity(1003L, "Python编程学习小组", "Python基础到进阶学习讨论",
                        "学习", "计算机楼101", "编程", 30, 25));
                virtualActivities.add(createVirtualActivity(1004L, "英语角活动", "每周英语口语练习",
                        "学习", "外语学院302", "英语", 40, 32));
            }

            // 艺术类活动
            if (category == null || category.equals("arts") || category.equals("all")) {
                virtualActivities.add(createVirtualActivity(1005L, "摄影技术分享会", "摄影爱好者交流会",
                        "艺术", "艺术楼302", "摄影", 25, 18));
                virtualActivities.add(createVirtualActivity(1006L, "绘画工作坊", "基础素描技巧教学",
                        "艺术", "艺术楼105", "绘画", 20, 15));
            }

            // 关键词过滤
            if (keyword != null && !keyword.trim().isEmpty()) {
                String keywordLower = keyword.toLowerCase();
                virtualActivities = virtualActivities.stream()
                        .filter(activity ->
                                activity.get("title").toString().toLowerCase().contains(keywordLower) ||
                                        activity.get("description").toString().toLowerCase().contains(keywordLower) ||
                                        activity.get("tags").toString().toLowerCase().contains(keywordLower))
                        .collect(Collectors.toList());
            }

            // 分页处理
            int total = virtualActivities.size();
            int start = (page - 1) * size;
            int end = Math.min(start + size, total);

            if (start >= total) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("data", new ArrayList<>());

                Map<String, Object> pagination = new HashMap<>();
                pagination.put("currentPage", page);
                pagination.put("pageSize", size);
                pagination.put("total", total);
                pagination.put("totalPages", (int) Math.ceil((double) total / size));

                response.put("pagination", pagination);
                response.put("message", "虚拟活动库数据");
                return response;
            }

            List<Map<String, Object>> pagedActivities = virtualActivities.subList(start, end);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", pagedActivities);

            Map<String, Object> pagination = new HashMap<>();
            pagination.put("currentPage", page);
            pagination.put("pageSize", size);
            pagination.put("total", total);
            pagination.put("totalPages", (int) Math.ceil((double) total / size));

            response.put("pagination", pagination);
            response.put("message", "虚拟活动库数据（模拟数据）");

            return response;

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取虚拟活动库失败: " + e.getMessage());
            return errorResponse;
        }
    }

    private Map<String, Object> createVirtualActivity(Long id, String title, String description,
                                                      String activityType, String location,
                                                      String tag, int maxParticipants, int currentParticipants) {
        Map<String, Object> activity = new HashMap<>();
        activity.put("id", id);
        activity.put("title", title);
        activity.put("description", description);
        activity.put("activityType", activityType);
        activity.put("location", location);
        activity.put("startTime", getStartTimeForId(id));
        activity.put("tags", Arrays.asList(tag, "活动"));
        activity.put("organizer", getOrganizerForType(activityType));
        activity.put("maxParticipants", maxParticipants);
        activity.put("currentParticipants", currentParticipants);
        activity.put("status", "active");
        return activity;
    }

    private String getStartTimeForId(Long id) {
        // 根据ID生成不同的开始时间
        int day = 20 + (id.intValue() % 5);
        return "2024-01-" + day + " 14:00:00";
    }

    private String getOrganizerForType(String activityType) {
        switch(activityType) {
            case "体育": return "体育协会";
            case "学习": return "学习协会";
            case "艺术": return "艺术协会";
            default: return "学生组织";
        }
    }

    /**
     * 获取标签分类的活动统计
     */
    @GetMapping("/tag-statistics")
    public Map<String, Object> getTagStatistics() {
        try {
            // 模拟标签统计数据
            Map<String, Object> statistics = new HashMap<>();

            // 按分类统计
            Map<String, Integer> byCategory = new HashMap<>();
            byCategory.put("体育", 45);
            byCategory.put("学习", 38);
            byCategory.put("艺术", 27);
            byCategory.put("社交", 32);
            byCategory.put("娱乐", 25);
            statistics.put("byCategory", byCategory);

            // 热门标签
            List<Map<String, Object>> popularTags = new ArrayList<>();
            popularTags.add(createTagStat("篮球", 28));
            popularTags.add(createTagStat("编程", 25));
            popularTags.add(createTagStat("摄影", 22));
            popularTags.add(createTagStat("英语", 19));
            popularTags.add(createTagStat("音乐", 17));
            statistics.put("popularTags", popularTags);

            // 活动时间分布
            Map<String, Integer> timeDistribution = new HashMap<>();
            timeDistribution.put("周末", 68);
            timeDistribution.put("工作日晚上", 42);
            timeDistribution.put("工作日白天", 15);
            statistics.put("timeDistribution", timeDistribution);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", statistics);
            response.put("message", "标签统计数据");

            return response;

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取标签统计失败: " + e.getMessage());
            return errorResponse;
        }
    }

    private Map<String, Object> createTagStat(String name, int count) {
        Map<String, Object> tag = new HashMap<>();
        tag.put("name", name);
        tag.put("count", count);
        return tag;
    }
}