package com.qushetai.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qushetai.backend.entity.User;
import com.qushetai.backend.service.UserService;
import com.qushetai.backend.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.RequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

@RestController
@RequestMapping({"/api/users", "/api/user"})
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TagService tagService;

    // 统一响应格式辅助方法
    private Map<String, Object> buildResponse(int code, boolean success, String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", code);
        response.put("success", success);
        response.put("message", message);
        response.put("data", data);
        response.put("timestamp", Instant.now().toString());
        return response;
    }

    // 发送验证码
    @PostMapping("/send-code")
    public Map<String, Object> sendCode(@RequestBody Map<String, String> request) {
        String contact = request.get("contact");
        Integer type = Integer.parseInt(request.get("type"));
        Map<String, Object> result = userService.sendVerificationCode(contact, type);

        // 统一响应格式
        if ((Boolean) result.get("success")) {
            return buildResponse(200, true, (String) result.get("message"), null);
        } else {
            return buildResponse(400, false, (String) result.get("message"), null);
        }
    }

    // 验证码注册 - 🔥 修复版本：处理空邮箱问题
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        String email = request.get("email");
        String code = request.get("code");
        String nickname = request.get("nickname");
        String password = request.get("password");

        System.out.println("【DEBUG】UserController.register - 接收参数:");
        System.out.println("【DEBUG】手机号: " + phone);
        System.out.println("【DEBUG】邮箱: " + email);
        System.out.println("【DEBUG】验证码: " + code);
        System.out.println("【DEBUG】昵称: " + nickname);

        // 🔥 关键修复：如果前端传的是空字符串，转换为null，让UserService处理
        if (email != null && email.trim().isEmpty()) {
            email = null;
            System.out.println("【DEBUG】邮箱为空字符串，转换为null");
        }

        Map<String, Object> result = userService.registerWithCode(phone, email, code, nickname, password);

        // 统一响应格式
        if ((Boolean) result.get("success")) {
            return buildResponse(200, true, (String) result.get("message"), null);
        } else {
            return buildResponse(400, false, (String) result.get("message"), null);
        }
    }

    // 验证码登录
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        String email = request.get("email");
        String code = request.get("code");

        Map<String, Object> result = userService.loginWithCode(phone, email, code);

        // 统一返回格式
        return formatLoginResponse(result);
    }

    // 密码登录 - 修复响应格式
    @PostMapping("/login-pwd")
    public Map<String, Object> loginWithPassword(@RequestBody Map<String, String> request) {
        try {
            String phone = request.get("phone");
            String email = request.get("email");
            String password = request.get("password");

            System.out.println("【DEBUG】UserController.loginWithPassword - 开始处理");
            System.out.println("【DEBUG】请求参数 - phone: " + phone + ", email: " + email);

            Map<String, Object> result = userService.loginWithPassword(phone, email, password);
            System.out.println("【DEBUG】UserService返回结果: " + result);

            // 统一返回格式
            if ((Boolean) result.get("success")) {
                // 🔥 关键修复：直接返回 UserService 的完整结果，不要重新构建
                Object data = result.get("data");
                System.out.println("【DEBUG】提取的data: " + data);

                if (data == null) {
                    System.err.println("【ERROR】UserService返回的data为null！");
                    return buildResponse(500, false, "服务器数据异常", null);
                }

                Map<String, Object> response = buildResponse(200, true, (String) result.get("message"), data);
                System.out.println("【DEBUG】UserController最终返回: " + response);
                return response;
            } else {
                return buildResponse(400, false, (String) result.get("message"), null);
            }

        } catch (Exception e) {
            System.err.println("【ERROR】UserController.loginWithPassword异常: " + e.getMessage());
            e.printStackTrace();
            String errorMessage = e.getMessage() != null ? e.getMessage() : "密码登录失败";
            return buildResponse(500, false, errorMessage, null);
        }
    }

    // 统一格式化登录响应
    private Map<String, Object> formatLoginResponse(Map<String, Object> serviceResult) {
        if ((Boolean) serviceResult.get("success")) {
            Map<String, Object> data = new HashMap<>();
            data.put("token", serviceResult.get("token"));
            data.put("userInfo", serviceResult.get("userInfo")); // 修复：改为userInfo保持统一

            return buildResponse(200, true, (String) serviceResult.get("message"), data);
        } else {
            return buildResponse(400, false, (String) serviceResult.get("message"), null);
        }
    }

    // 设置/修改密码
    @PostMapping("/{userId}/password")
    public Map<String, Object> setPassword(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        String newPassword = request.get("newPassword");
        Map<String, Object> result = userService.setPassword(userId, newPassword);

        if ((Boolean) result.get("success")) {
            return buildResponse(200, true, (String) result.get("message"), null);
        } else {
            return buildResponse(400, false, (String) result.get("message"), null);
        }
    }

    // 获取用户信息
    @GetMapping("/{userId}")
    public Map<String, Object> getUserInfo(@PathVariable Long userId) {
        Map<String, Object> result = userService.getUserInfo(userId);

        if ((Boolean) result.get("success")) {
            return buildResponse(200, true, "获取用户信息成功", result.get("data"));
        } else {
            return buildResponse(404, false, (String) result.get("message"), null);
        }
    }

    // 更新用户信息
    @PutMapping("/{userId}")
    public Map<String, Object> updateUserInfo(@PathVariable Long userId, @RequestBody User user) {
        user.setId(userId);
        Map<String, Object> result = userService.updateUserInfo(user);

        if ((Boolean) result.get("success")) {
            return buildResponse(200, true, (String) result.get("message"), null);
        } else {
            return buildResponse(400, false, (String) result.get("message"), null);
        }
    }

    // 获取用户兴趣标签
    @GetMapping("/{userId}/interests")
    public Map<String, Object> getUserInterests(@PathVariable Long userId) {
        Map<String, Object> result = userService.getUserInterests(userId);

        if ((Boolean) result.get("success")) {
            return buildResponse(200, true, "获取兴趣标签成功", result.get("data"));
        } else {
            return buildResponse(404, false, (String) result.get("message"), null);
        }
    }

    // 更新用户兴趣标签（PUT方法 - 保持原样）
    @PutMapping("/{userId}/interests")
    public Map<String, Object> updateUserInterests(@PathVariable Long userId, @RequestBody Map<String, Object> request) {
        String interestTags = (String) request.get("interestTags");
        Map<String, Object> result = userService.updateUserInterests(userId, interestTags);

        if ((Boolean) result.get("success")) {
            return buildResponse(200, true, (String) result.get("message"), null);
        } else {
            return buildResponse(400, false, (String) result.get("message"), null);
        }
    }

    /**
     * 更新用户兴趣标签（前端需要的 POST /api/user/interests 接口）
     * 兼容前端格式：POST /api/user/interests
     * 实际实现为：POST /api/users/{userId}/interests
     */
    @PostMapping("/{userId}/interests")
    public Map<String, Object> updateUserInterestsByPost(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> request) {

        String interestTags = (String) request.get("interestTags");
        System.out.println("【DEBUG】UserController.updateUserInterestsByPost - userId=" + userId + ", tags=" + interestTags);

        // 调用现有的 updateUserInterests 方法
        Map<String, Object> result = userService.updateUserInterests(userId, interestTags);

        if ((Boolean) result.get("success")) {
            return buildResponse(200, true, (String) result.get("message"), null);
        } else {
            return buildResponse(400, false, (String) result.get("message"), null);
        }
    }

    /**
     * 新增：处理 POST /api/user/interests（使用当前登录用户ID）
     * 前端会调用这个接口，我们重定向到正确的接口
     */
    @PostMapping("/me/interests")
    public Map<String, Object> updateMyInterests(
            @RequestBody Map<String, Object> request) {

        try {
            // 使用RequestContextHolder获取RequestAttributes
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes == null) {
                return buildResponse(401, false, "无法获取请求上下文", null);
            }

            // 从请求属性中获取userId
            Long userId = (Long) requestAttributes.getAttribute("userId", RequestAttributes.SCOPE_REQUEST);

            if (userId == null) {
                return buildResponse(401, false, "用户未认证", null);
            }

            String interestTags = (String) request.get("interestTags");
            System.out.println("【DEBUG】UserController.updateMyInterests - userId=" + userId + ", tags=" + interestTags);

            // 调用 service 更新兴趣标签
            Map<String, Object> result = userService.updateUserInterests(userId, interestTags);

            if ((Boolean) result.get("success")) {
                return buildResponse(200, true, (String) result.get("message"), null);
            } else {
                return buildResponse(400, false, (String) result.get("message"), null);
            }

        } catch (Exception e) {
            System.out.println("【ERROR】UserController.updateMyInterests异常: " + e.getMessage());
            return buildResponse(500, false, "更新兴趣标签失败: " + e.getMessage(), null);
        }
    }

    // 获取用户空闲时间
    @GetMapping("/{userId}/free-time")
    public Map<String, Object> getUserFreeTime(@PathVariable Long userId) {
        Map<String, Object> result = userService.getUserFreeTime(userId);

        if ((Boolean) result.get("success")) {
            return buildResponse(200, true, "获取空闲时间成功", result.get("data"));
        } else {
            return buildResponse(404, false, (String) result.get("message"), null);
        }
    }

    // 更新用户空闲时间
    @PutMapping("/{userId}/free-time")
    public Map<String, Object> updateUserFreeTime(@PathVariable Long userId, @RequestBody Map<String, Object> request) {
        String freeTimeSlots = (String) request.get("freeTimeSlots");
        Map<String, Object> result = userService.updateUserFreeTime(userId, freeTimeSlots);

        if ((Boolean) result.get("success")) {
            return buildResponse(200, true, (String) result.get("message"), null);
        } else {
            return buildResponse(400, false, (String) result.get("message"), null);
        }
    }

    // 获取当前登录用户的空闲时间（不需要userId参数，从token中获取）
    @GetMapping("/free-time")
    public Map<String, Object> getCurrentUserFreeTime(HttpServletRequest request) {
        try {
            Long currentUserId = getUserIdFromRequest(request);
            return getUserFreeTime(currentUserId);
        } catch (Exception e) {
            return buildResponse(500, false, "获取空闲时间失败: " + e.getMessage(), null);
        }
    }

    // 修改：更新当前登录用户的空闲时间，确保正确处理空数组
    @PutMapping("/free-time")
    public Map<String, Object> updateCurrentUserFreeTime(@RequestBody Map<String, Object> requestData,
                                                         HttpServletRequest httpRequest) {
        try {
            Long currentUserId = getUserIdFromRequest(httpRequest);

            // 处理前端可能的 "slots" 字段
            Object slots = requestData.get("slots");
            String freeTimeSlots = null;

            // 如果有 slots 数组，转换为 JSON 字符串
            if (slots != null) {
                // 将 slots 数组转换为 JSON 字符串
                ObjectMapper objectMapper = new ObjectMapper();
                String slotsJson = objectMapper.writeValueAsString(slots);

                // 处理空数组情况
                if ("[]".equals(slotsJson)) {
                    freeTimeSlots = ""; // 或者 "[]" 根据你的需求
                } else {
                    freeTimeSlots = slotsJson;
                }
            }
            // 如果直接有 freeTimeSlots 字段，直接使用
            else if (requestData.get("freeTimeSlots") != null) {
                freeTimeSlots = (String) requestData.get("freeTimeSlots");
            }

            // 创建新的请求参数
            Map<String, Object> request = new HashMap<>();
            request.put("freeTimeSlots", freeTimeSlots);

            return updateUserFreeTime(currentUserId, request);
        } catch (Exception e) {
            System.err.println("【ERROR】更新空闲时间失败: " + e.getMessage());
            // 不打印详细堆栈给前端看
            return buildResponse(500, false, "更新空闲时间失败：参数格式错误", null);
        }
    }

    // 添加获取当前用户ID的方法
    private Long getUserIdFromRequest(HttpServletRequest request) {
        Object userIdObj = request.getAttribute("userId");
        if (userIdObj == null) {
            throw new RuntimeException("用户未认证或token无效");
        }
        return (Long) userIdObj;
    }

    // 获取用户行为数据 - 新增接口，供D的算法调用
    @GetMapping("/behavior-data")
    public Map<String, Object> getUserBehaviorData(
            @RequestParam Long userId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "100") int limit) {

        // 这里调用服务层获取用户行为数据
        // 实际实现需要在UserService中添加相应方法

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("startTime", startTime);
        data.put("endTime", endTime);
        data.put("limit", limit);
        data.put("behaviorList", new java.util.ArrayList<>()); // 空列表，需要实际实现

        return buildResponse(200, true, "用户行为数据获取成功", data);
    }

    // ========== 新增：标签管理接口（处理JSON格式标签数组） ==========

    /**
     * 保存用户选择的标签（前端要求的 POST /api/user/tags）
     */
    @PostMapping("/tags")
    public Map<String, Object> saveUserTags(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            Long currentUserId = getUserIdFromRequest(httpRequest);
            System.out.println("【DEBUG】UserController.saveUserTags - 用户ID: " + currentUserId);

            // 获取标签数组
            List<String> tags = (List<String>) request.get("tags");
            System.out.println("【DEBUG】接收到的标签: " + tags);

            if (tags == null) {
                return buildResponse(400, false, "标签不能为空", null);
            }

            // 调用服务层保存标签
            Map<String, Object> result = userService.saveUserTags(currentUserId, tags);

            if ((Boolean) result.get("success")) {
                return buildResponse(200, true, (String) result.get("message"), result.get("tags"));
            } else {
                return buildResponse(400, false, (String) result.get("message"), null);
            }
        } catch (Exception e) {
            System.err.println("【ERROR】保存用户标签异常: " + e.getMessage());
            e.printStackTrace();
            return buildResponse(500, false, "保存标签失败: " + e.getMessage(), null);
        }
    }

    /**
     * 获取用户已选的标签（前端要求的 GET /api/user/tags）
     */
    @GetMapping("/tags")
    public Map<String, Object> getUserTags(HttpServletRequest httpRequest) {
        try {
            Long currentUserId = getUserIdFromRequest(httpRequest);
            System.out.println("【DEBUG】UserController.getUserTags - 用户ID: " + currentUserId);

            // 调用服务层获取标签
            List<String> tags = userService.getUserTags(currentUserId);
            System.out.println("【DEBUG】获取到的标签: " + tags);

            return buildResponse(200, true, "获取用户标签成功", tags);
        } catch (Exception e) {
            System.err.println("【ERROR】获取用户标签异常: " + e.getMessage());
            return buildResponse(500, false, "获取标签失败: " + e.getMessage(), null);
        }
    }

    /**
     * 删除用户标签（前端要求的 DELETE /api/user/tags）
     */
    @DeleteMapping("/tags")
    public Map<String, Object> deleteUserTags(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            Long currentUserId = getUserIdFromRequest(httpRequest);
            System.out.println("【DEBUG】UserController.deleteUserTags - 用户ID: " + currentUserId);

            // 获取要删除的标签数组
            List<String> tagsToDelete = (List<String>) request.get("tags");
            System.out.println("【DEBUG】要删除的标签: " + tagsToDelete);

            if (tagsToDelete == null || tagsToDelete.isEmpty()) {
                return buildResponse(400, false, "要删除的标签不能为空", null);
            }

            // 调用服务层删除标签
            Map<String, Object> result = userService.deleteUserTags(currentUserId, tagsToDelete);

            if ((Boolean) result.get("success")) {
                return buildResponse(200, true, (String) result.get("message"), result.get("tags"));
            } else {
                return buildResponse(400, false, (String) result.get("message"), null);
            }
        } catch (Exception e) {
            System.err.println("【ERROR】删除用户标签异常: " + e.getMessage());
            return buildResponse(500, false, "删除标签失败: " + e.getMessage(), null);
        }
    }

    // ========== 新增：调试接口 - 用于前端调试标签问题 ==========

    /**
     * 调试接口：获取可用的标签列表（从TagService获取）
     */
    @GetMapping("/tags/available")
    public Map<String, Object> getAvailableTags() {
        try {
            System.out.println("【DEBUG】UserController.getAvailableTags - 被调用");

            // 调用TagService获取所有标签
            List<Map<String, Object>> categories = tagService.getAllTags();
            List<Map<String, Object>> allTags = new ArrayList<>();

            // 获取每个分类下的标签
            for (Map<String, Object> category : categories) {
                String categoryName = (String) category.get("name");
                if (categoryName != null) {
                    List<Map<String, Object>> tags = tagService.getTagsByCategory(categoryName);
                    allTags.addAll(tags);
                }
            }

            System.out.println("【DEBUG】获取到的标签数量: " + allTags.size());

            return buildResponse(200, true, "获取可用标签成功", allTags);
        } catch (Exception e) {
            System.err.println("【ERROR】获取可用标签失败: " + e.getMessage());
            return buildResponse(500, false, "获取标签失败: " + e.getMessage(), null);
        }
    }

    /**
     * 调试接口：获取可用的标签列表（简化版，直接返回预定义标签）
     */
    @GetMapping("/tags/available-from-db")
    public Map<String, Object> getAvailableTagsFromDb() {
        try {
            System.out.println("【DEBUG】UserController.getAvailableTagsFromDb - 被调用");

            // 预定义一些常用标签
            List<Map<String, Object>> predefinedTags = new ArrayList<>();
            String[] tags = {
                    "篮球", "足球", "羽毛球", "乒乓球", "游泳", "跑步", "健身",
                    "编程", "学习", "英语", "阅读", "写作", "数学",
                    "摄影", "绘画", "音乐", "舞蹈", "电影", "游戏",
                    "美食", "旅游", "社交", "聚会", "志愿者", "讲座"
            };

            for (int i = 0; i < tags.length; i++) {
                Map<String, Object> tag = new HashMap<>();
                tag.put("id", i + 1);
                tag.put("name", tags[i]);
                tag.put("type", "tag");
                predefinedTags.add(tag);
            }

            System.out.println("【DEBUG】返回预定义标签数量: " + predefinedTags.size());

            return buildResponse(200, true, "获取标签成功", predefinedTags);
        } catch (Exception e) {
            System.err.println("【ERROR】获取预定义标签失败: " + e.getMessage());
            return buildResponse(500, false, "获取标签失败", null);
        }
    }

    /**
     * 获取标签分类（硬编码，用于前端开发）
     */
    @GetMapping("/tags/categories")
    public Map<String, Object> getTagCategories() {
        try {
            List<Map<String, Object>> categories = new ArrayList<>();

            String[] categoryNames = {"体育", "学习", "艺术", "社交", "娱乐"};
            String[][] categoryTags = {
                    {"篮球", "足球", "羽毛球", "乒乓球", "游泳", "跑步", "健身"},
                    {"编程", "学习", "英语", "阅读", "写作", "数学", "讲座"},
                    {"摄影", "绘画", "音乐", "舞蹈", "电影", "手工", "设计"},
                    {"美食", "旅游", "聚会", "志愿者", "聊天", "交友", "团队活动"},
                    {"游戏", "电影", "唱歌", "桌游", "户外", "探险", "休闲"}
            };

            for (int i = 0; i < categoryNames.length; i++) {
                Map<String, Object> category = new HashMap<>();
                category.put("id", i + 1);
                category.put("name", categoryNames[i]);
                category.put("description", categoryNames[i] + "相关活动");
                category.put("tags", Arrays.asList(categoryTags[i]));
                categories.add(category);
            }

            return buildResponse(200, true, "获取标签分类成功", Map.of(
                    "categories", categories,
                    "total", categories.size()
            ));
        } catch (Exception e) {
            return buildResponse(500, false, "获取标签分类失败", null);
        }
    }

    /**
     * 获取热门标签
     */
    @GetMapping("/tags/popular")
    public Map<String, Object> getPopularTags() {
        try {
            List<Map<String, Object>> popularTags = new ArrayList<>();

            String[] tags = {"篮球", "编程", "摄影", "美食", "音乐", "旅游", "游戏", "学习"};
            int[] counts = {120, 98, 85, 76, 65, 54, 43, 32};

            for (int i = 0; i < tags.length; i++) {
                Map<String, Object> tag = new HashMap<>();
                tag.put("id", i + 1);
                tag.put("name", tags[i]);
                tag.put("count", counts[i]);
                popularTags.add(tag);
            }

            return buildResponse(200, true, "获取热门标签成功", Map.of(
                    "tags", popularTags,
                    "total", popularTags.size()
            ));
        } catch (Exception e) {
            return buildResponse(500, false, "获取热门标签失败", null);
        }
    }
}