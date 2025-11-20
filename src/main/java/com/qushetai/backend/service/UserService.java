package com.qushetai.backend.service;

import com.qushetai.backend.entity.User;
import com.qushetai.backend.entity.VerificationCode;
import com.qushetai.backend.mapper.UserMapper;
import com.qushetai.backend.mapper.VerificationCodeMapper;
import com.qushetai.backend.mapper.ActivityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VerificationCodeMapper verificationCodeMapper;

    @Autowired
    private ActivityMapper activityMapper;

    // 密码编码器
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // 根据邮箱查找用户 - 新增方法
    public User findByEmail(String email) {
        return userMapper.findByEmail(email);
    }

    // 保存用户 - 新增方法
    public boolean saveUser(User user) {
        try {
            int result = userMapper.insert(user);
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据用户ID查找用户
     */
    public User findUserById(Long userId) {
        return userMapper.findById(userId);
    }

    /**
     * 检查用户是否是管理员
     */
    public boolean isAdmin(Long userId) {
        try {
            System.out.println("【DEBUG】UserService.isAdmin - 检查用户 " + userId + " 的管理员权限");
            Integer isAdmin = userMapper.isAdmin(userId);
            System.out.println("【DEBUG】数据库返回的is_admin值: " + isAdmin);
            boolean result = isAdmin != null && isAdmin == 1;
            System.out.println("【DEBUG】最终权限结果: " + result);
            return result;
        } catch (Exception e) {
            System.err.println("【ERROR】检查管理员权限异常: " + e.getMessage());
            return false;
        }
    }

    // 发送验证码
    public Map<String, Object> sendVerificationCode(String contact, Integer type) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 生成6位随机验证码
            String code = String.valueOf((int)((Math.random() * 9 + 1) * 100000));

            VerificationCode verificationCode = new VerificationCode();
            verificationCode.setContact(contact);
            verificationCode.setCode(code);
            verificationCode.setType(type);
            verificationCode.setExpiredAt(LocalDateTime.now().plusMinutes(5));

            System.out.println("发送验证码到 " + contact + ": " + code);

            boolean success = verificationCodeMapper.insert(verificationCode) > 0;

            response.put("success", success);
            response.put("message", success ? "验证码发送成功" : "验证码发送失败");

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "服务器错误: " + e.getMessage());
        }

        return response;
    }

    // 验证码注册
    public Map<String, Object> registerWithCode(String phone, String email, String code, String nickname, String password) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 验证验证码
            String contact = phone != null ? phone : email;
            VerificationCode validCode = verificationCodeMapper.findValidCode(contact, code, 1);
            if (validCode == null) {
                response.put("success", false);
                response.put("message", "验证码无效或已过期");
                return response;
            }

            // 检查手机号或邮箱是否已存在
            if (phone != null && userMapper.findByPhone(phone) != null) {
                response.put("success", false);
                response.put("message", "手机号已存在");
                return response;
            }
            if (email != null && userMapper.findByEmail(email) != null) {
                response.put("success", false);
                response.put("message", "邮箱已存在");
                return response;
            }

            // 创建用户
            User user = new User();
            user.setPhone(phone);
            user.setEmail(email);
            user.setNickname(nickname != null ? nickname : "用户" + contact);
            user.setIsActive(1);

            // 设置密码（如果有）
            if (password != null && !password.trim().isEmpty()) {
                String hashedPassword = passwordEncoder.encode(password);
                user.setPasswordHash(hashedPassword);
            }

            boolean success = userMapper.insert(user) > 0;
            if (success) {
                verificationCodeMapper.markAsUsed(validCode.getId());
                response.put("success", true);
                response.put("message", "注册成功");
                response.put("userId", user.getId());
            } else {
                response.put("success", false);
                response.put("message", "注册失败");
            }

        } catch (Exception e) {
            System.out.println("注册异常: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "服务器错误: " + e.getMessage());
        }

        return response;
    }

    // 验证码登录
    public Map<String, Object> loginWithCode(String phone, String email, String code) {
        Map<String, Object> response = new HashMap<>();

        try {
            String contact = phone != null ? phone : email;
            VerificationCode validCode = verificationCodeMapper.findValidCode(contact, code, 2);
            if (validCode == null) {
                response.put("success", false);
                response.put("message", "验证码无效或已过期");
                return response;
            }

            User user = phone != null ? userMapper.findByPhone(phone) : userMapper.findByEmail(email);
            if (user != null) {
                verificationCodeMapper.markAsUsed(validCode.getId());
                String token = "temp_token_" + user.getId() + "_" + System.currentTimeMillis();

                // 安全构建用户信息
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("email", user.getEmail());
                userInfo.put("nickname", user.getNickname() != null ? user.getNickname() : "");
                userInfo.put("isAdmin", user.getIsAdmin() != null ? user.getIsAdmin() : 0);

                Map<String, Object> data = new HashMap<>();
                data.put("token", token);
                data.put("user", userInfo);

                response.put("success", true);
                response.put("message", "登录成功");
                response.put("data", data);
            } else {
                response.put("success", false);
                response.put("message", "用户不存在");
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "服务器错误: " + e.getMessage());
        }

        return response;
    }

    // 密码登录
    public Map<String, Object> loginWithPassword(String phone, String email, String password) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("【DEBUG】UserService.loginWithPassword - 开始处理登录请求");
            System.out.println("【DEBUG】参数 - phone: " + phone + ", email: " + email);

            User user = phone != null ? userMapper.findByPhone(phone) : userMapper.findByEmail(email);
            System.out.println("【DEBUG】查询到的用户: " + (user != null ? "存在" : "null"));

            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }

            // 检查用户是否有设置密码
            if (user.getPasswordHash() == null || user.getPasswordHash().trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "该账号未设置密码，请使用验证码登录");
                return response;
            }

            // 验证密码
            boolean passwordMatch = passwordEncoder.matches(password, user.getPasswordHash());
            System.out.println("【DEBUG】密码验证结果: " + passwordMatch);

            if (passwordMatch) {
                String token = "temp_token_" + user.getId() + "_" + System.currentTimeMillis();
                System.out.println("【DEBUG】生成的token: " + token);

                // 安全构建用户信息
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("email", user.getEmail());
                userInfo.put("nickname", user.getNickname() != null ? user.getNickname() : "");
                userInfo.put("isAdmin", user.getIsAdmin() != null ? user.getIsAdmin() : 0);

                System.out.println("【DEBUG】构建的用户信息: " + userInfo);

                response.put("success", true);
                response.put("message", "登录成功");
                response.put("token", token);
                response.put("user", userInfo);
                response.put("userId", user.getId());

                System.out.println("【DEBUG】最终返回的response: " + response);
            } else {
                response.put("success", false);
                response.put("message", "密码错误");
            }

        } catch (Exception e) {
            System.out.println("【ERROR】UserService.loginWithPassword异常: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "服务器错误: " + e.getMessage());
        }

        return response;
    }

    // 设置/修改密码
    public Map<String, Object> setPassword(Long userId, String newPassword) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = userMapper.findById(userId);
            if (user == null) {
                response.put("success", false);
                response.put("message", "用户不存在");
                return response;
            }

            // 加密新密码
            String hashedPassword = passwordEncoder.encode(newPassword);
            int rows = userMapper.updatePassword(userId, hashedPassword);

            if (rows > 0) {
                response.put("success", true);
                response.put("message", "密码设置成功");
            } else {
                response.put("success", false);
                response.put("message", "密码设置失败");
            }

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "服务器错误: " + e.getMessage());
        }

        return response;
    }

    // 获取用户信息
    public Map<String, Object> getUserInfo(Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            User user = userMapper.findById(userId);
            if (user != null) {
                result.put("success", true);
                result.put("data", user);
            } else {
                result.put("success", false);
                result.put("message", "用户不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取用户信息失败: " + e.getMessage());
        }
        return result;
    }

    // 更新用户信息
    public Map<String, Object> updateUserInfo(User user) {
        Map<String, Object> result = new HashMap<>();
        try {
            int rows = userMapper.update(user);
            if (rows > 0) {
                result.put("success", true);
                result.put("message", "用户信息更新成功");
            } else {
                result.put("success", false);
                result.put("message", "用户信息更新失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新用户信息失败: " + e.getMessage());
        }
        return result;
    }

    // 获取用户兴趣标签
    public Map<String, Object> getUserInterests(Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            User user = userMapper.findById(userId);
            if (user != null) {
                result.put("success", true);
                result.put("data", Map.of("interestTags", user.getInterestTags()));
            } else {
                result.put("success", false);
                result.put("message", "用户不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取兴趣标签失败: " + e.getMessage());
        }
        return result;
    }

    // 更新用户兴趣标签
    public Map<String, Object> updateUserInterests(Long userId, String interestTags) {
        Map<String, Object> result = new HashMap<>();
        try {
            int rows = userMapper.updateInterests(userId, interestTags);
            if (rows > 0) {
                result.put("success", true);
                result.put("message", "兴趣标签更新成功");
            } else {
                result.put("success", false);
                result.put("message", "兴趣标签更新失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新兴趣标签失败: " + e.getMessage());
        }
        return result;
    }

    // 获取用户空闲时间
    public Map<String, Object> getUserFreeTime(Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            User user = userMapper.findById(userId);
            if (user != null) {
                result.put("success", true);
                result.put("data", Map.of("freeTimeSlots", user.getFreeTimeSlots()));
            } else {
                result.put("success", false);
                result.put("message", "用户不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取空闲时间失败: " + e.getMessage());
        }
        return result;
    }

    // 更新用户空闲时间
    public Map<String, Object> updateUserFreeTime(Long userId, String freeTimeSlots) {
        Map<String, Object> result = new HashMap<>();
        try {
            int rows = userMapper.updateFreeTime(userId, freeTimeSlots);
            if (rows > 0) {
                result.put("success", true);
                result.put("message", "空闲时间更新成功");
            } else {
                result.put("success", false);
                result.put("message", "空闲时间更新失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新空闲时间失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取所有用户（管理员用）
     */
    public Map<String, Object> getAllUsers(Long currentUserId, int page, int size) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 检查权限
            if (!isAdmin(currentUserId)) {
                result.put("success", false);
                result.put("message", "权限不足");
                return result;
            }

            if (page < 1) page = 1;
            if (size < 1 || size > 100) size = 20;

            int offset = (page - 1) * size;
            List<User> users = userMapper.selectAllUsersWithPagination(offset, size);
            int total = userMapper.countAllUsers();

            result.put("success", true);
            result.put("data", users);
            result.put("pagination", Map.of(
                    "currentPage", page,
                    "pageSize", size,
                    "total", total,
                    "totalPages", (int) Math.ceil((double) total / size)
            ));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取用户列表失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 封禁用户
     */
    public Map<String, Object> banUser(Long currentUserId, Long targetUserId) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 检查权限
            if (!isAdmin(currentUserId)) {
                result.put("success", false);
                result.put("message", "权限不足");
                return result;
            }

            // 不能封禁自己
            if (currentUserId.equals(targetUserId)) {
                result.put("success", false);
                result.put("message", "不能封禁自己");
                return result;
            }

            int rows = userMapper.updateUserStatus(targetUserId, 0); // 0表示封禁
            if (rows > 0) {
                result.put("success", true);
                result.put("message", "用户封禁成功");
            } else {
                result.put("success", false);
                result.put("message", "用户封禁失败，用户可能不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "封禁用户失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 解封用户
     */
    public Map<String, Object> unbanUser(Long currentUserId, Long targetUserId) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (!isAdmin(currentUserId)) {
                result.put("success", false);
                result.put("message", "权限不足");
                return result;
            }

            int rows = userMapper.updateUserStatus(targetUserId, 1); // 1表示解封
            if (rows > 0) {
                result.put("success", true);
                result.put("message", "用户解封成功");
            } else {
                result.put("success", false);
                result.put("message", "用户解封失败，用户可能不存在");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "解封用户失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 获取管理员统计信息
     */
    public Map<String, Object> getAdminStats(Long currentUserId) {
        Map<String, Object> result = new HashMap<>();

        try {
            if (!isAdmin(currentUserId)) {
                result.put("success", false);
                result.put("message", "权限不足");
                return result;
            }

            int totalUsers = userMapper.countAllUsers();
            int activeUsers = userMapper.countActiveUsers();
            int totalActivities = activityMapper.countAllActivities();
            int activeActivities = activityMapper.countActivities();

            result.put("success", true);
            result.put("data", Map.of(
                    "users", Map.of(
                            "total", totalUsers,
                            "active", activeUsers,
                            "banned", totalUsers - activeUsers
                    ),
                    "activities", Map.of(
                            "total", totalActivities,
                            "active", activeActivities,
                            "takenDown", totalActivities - activeActivities
                    )
            ));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取统计信息失败: " + e.getMessage());
        }
        return result;
    }
}