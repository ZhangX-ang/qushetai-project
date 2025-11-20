package com.qushetai.backend.controller;

import com.qushetai.backend.entity.User;
import com.qushetai.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // 发送验证码
    @PostMapping("/send-code")
    public Map<String, Object> sendCode(@RequestBody Map<String, String> request) {
        String contact = request.get("contact");
        Integer type = Integer.parseInt(request.get("type"));
        return userService.sendVerificationCode(contact, type);
    }

    // 验证码注册
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        String email = request.get("email");
        String code = request.get("code");
        String nickname = request.get("nickname");
        String password = request.get("password");

        return userService.registerWithCode(phone, email, code, nickname, password);
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

    // 密码登录
    @PostMapping("/login-pwd")
    public Map<String, Object> loginWithPassword(@RequestBody Map<String, String> request) {
        String phone = request.get("phone");
        String email = request.get("email");
        String password = request.get("password");

        Map<String, Object> result = userService.loginWithPassword(phone, email, password);

        // 统一返回格式
        return formatLoginResponse(result);
    }

    // 统一格式化登录响应
    private Map<String, Object> formatLoginResponse(Map<String, Object> serviceResult) {
        if ((Boolean) serviceResult.get("success")) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", serviceResult.get("message"));

            Map<String, Object> data = new HashMap<>();
            data.put("token", serviceResult.get("token"));
            data.put("user", serviceResult.get("user"));

            response.put("data", data);
            return response;
        } else {
            return serviceResult;
        }
    }

    // 设置/修改密码
    @PostMapping("/{userId}/password")
    public Map<String, Object> setPassword(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        String newPassword = request.get("newPassword");
        return userService.setPassword(userId, newPassword);
    }

    // 获取用户信息
    @GetMapping("/{userId}")
    public Map<String, Object> getUserInfo(@PathVariable Long userId) {
        return userService.getUserInfo(userId);
    }

    // 更新用户信息
    @PutMapping("/{userId}")
    public Map<String, Object> updateUserInfo(@PathVariable Long userId, @RequestBody User user) {
        user.setId(userId);
        return userService.updateUserInfo(user);
    }

    // 获取用户兴趣标签
    @GetMapping("/{userId}/interests")
    public Map<String, Object> getUserInterests(@PathVariable Long userId) {
        return userService.getUserInterests(userId);
    }

    // 更新用户兴趣标签
    @PutMapping("/{userId}/interests")
    public Map<String, Object> updateUserInterests(@PathVariable Long userId, @RequestBody Map<String, Object> request) {
        String interestTags = (String) request.get("interestTags");
        return userService.updateUserInterests(userId, interestTags);
    }

    // 获取用户空闲时间
    @GetMapping("/{userId}/free-time")
    public Map<String, Object> getUserFreeTime(@PathVariable Long userId) {
        return userService.getUserFreeTime(userId);
    }

    // 更新用户空闲时间
    @PutMapping("/{userId}/free-time")
    public Map<String, Object> updateUserFreeTime(@PathVariable Long userId, @RequestBody Map<String, Object> request) {
        String freeTimeSlots = (String) request.get("freeTimeSlots");
        return userService.updateUserFreeTime(userId, freeTimeSlots);
    }

    // 获取用户行为数据 - 新增接口，供D的算法调用
    @GetMapping("/behavior-data")
    public Map<String, Object> getUserBehaviorData(
            @RequestParam Long userId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "100") int limit) {

        // 这里调用服务层获取用户行为数据
        // 由于您说不要变动其他内容，这里先返回一个基本结构
        // 实际实现需要在UserService中添加相应方法

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "用户行为数据获取成功");

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("startTime", startTime);
        data.put("endTime", endTime);
        data.put("limit", limit);
        data.put("behaviorList", new java.util.ArrayList<>()); // 空列表，需要实际实现

        result.put("data", data);
        return result;
    }
}