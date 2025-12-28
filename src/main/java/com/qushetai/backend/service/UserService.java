package com.qushetai.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.qushetai.backend.entity.User;
import com.qushetai.backend.entity.VerificationCode;
import com.qushetai.backend.mapper.UserMapper;
import com.qushetai.backend.mapper.VerificationCodeMapper;
import com.qushetai.backend.mapper.ActivityMapper;
import com.qushetai.backend.config.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private VerificationCodeMapper verificationCodeMapper;

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;  // 添加Jackson ObjectMapper

    // 密码编码器
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // === 修改方法：根据手机号查找用户，添加详细调试信息 ===
    public User findByPhone(String phone) {
        try {
            System.out.println("【DEBUG】UserService.findByPhone - 开始查询手机号: " + phone);
            System.out.println("【DEBUG】手机号长度: " + phone.length());
            System.out.println("【DEBUG】手机号格式验证: " + phone.matches("\\d{11}"));

            // 使用列表查询而不是单个查询
            List<User> users = userMapper.findListByPhone(phone);
            System.out.println("【DEBUG】UserService.findByPhone - 查询结果数量: " + (users != null ? users.size() : "null"));

            if (users == null || users.isEmpty()) {
                System.out.println("【DEBUG】UserService.findByPhone - 未找到用户");
                return null;
            }
            if (users.size() > 1) {
                System.err.println("【WARN】手机号 " + phone + " 存在 " + users.size() + " 条重复记录，使用第一条");
                // 记录所有重复用户的ID
                for (int i = 0; i < users.size(); i++) {
                    User duplicateUser = users.get(i);
                    System.err.println("【WARN】重复用户 " + (i + 1) + ": ID=" + duplicateUser.getId() +
                            ", 手机号=" + duplicateUser.getPhone() +
                            ", 邮箱=" + duplicateUser.getEmail());
                }
            }

            User user = users.get(0);
            System.out.println("【DEBUG】UserService.findByPhone - 返回用户: ID=" + user.getId() +
                    ", 手机号=" + user.getPhone() +
                    ", 邮箱=" + user.getEmail() +
                    ", 昵称=" + user.getNickname());
            return user;

        } catch (Exception e) {
            System.err.println("【ERROR】查询手机号用户异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // 根据邮箱查找用户 - 原有方法
    public User findByEmail(String email) {
        return userMapper.findByEmail(email);
    }

    // 保存用户 - 原有方法
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

    // 发送验证码 - 添加详细日志
    public Map<String, Object> sendVerificationCode(String contact, Integer type) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("【DEBUG】UserService.sendVerificationCode - 开始: contact=" + contact + ", type=" + type);

            // 生成6位随机验证码
            String code = generateVerificationCode();
            System.out.println("【DEBUG】生成的验证码: " + code);

            VerificationCode verificationCode = new VerificationCode();
            verificationCode.setContact(contact);
            verificationCode.setCode(code);
            verificationCode.setType(type);
            verificationCode.setExpiredAt(LocalDateTime.now().plusMinutes(5));

            System.out.println("【DEBUG】准备保存验证码到数据库");
            int insertResult = verificationCodeMapper.insert(verificationCode);
            System.out.println("【DEBUG】验证码保存结果: " + insertResult);  // 🔥 添加这行关键日志

            boolean success = insertResult > 0;

            if (success) {
                // 模拟发送验证码（开发环境）
                System.out.println("【INFO】验证码发送到 " + contact + ": " + code);

                // 如果是生产环境，这里应该调用真实的短信/邮件服务
                if (type == 1) {
                    System.out.println("【DEBUG】模拟发送短信验证码");
                } else if (type == 2) {
                    System.out.println("【DEBUG】模拟发送邮件验证码");
                }
            }

            response.put("success", success);
            response.put("message", success ? "验证码发送成功" : "验证码发送失败");
            response.put("code", code); // 返回验证码用于测试

            // 🔥 添加返回前的日志
            System.out.println("【DEBUG】准备返回响应: " + response);

        } catch (Exception e) {
            System.err.println("【ERROR】UserService.sendVerificationCode - 异常: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "服务器错误: " + e.getMessage());
        }

        return response;
    }

    // 优化的验证码生成方法
    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(999999));
    }

    // 验证码注册 - 🔥 修复版本：处理空邮箱问题
    public Map<String, Object> registerWithCode(String phone, String email, String code, String nickname, String password) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("【DEBUG】UserService.registerWithCode - 开始注册流程");
            System.out.println("【DEBUG】参数 - phone: " + phone + ", email: " + email + ", code: " + code + ", nickname: " + nickname);

            // 🔥 第一步：处理邮箱参数
            // 如果email为空或空字符串，生成一个基于手机号的临时邮箱
            String actualEmail = email;
            if (actualEmail == null || actualEmail.trim().isEmpty()) {
                if (phone != null && !phone.trim().isEmpty()) {
                    actualEmail = phone + "@qushetai.com";
                    System.out.println("【DEBUG】邮箱为空，生成临时邮箱: " + actualEmail);
                } else {
                    // 如果没有手机号，生成随机邮箱
                    actualEmail = "user_" + System.currentTimeMillis() + "@qushetai.com";
                    System.out.println("【DEBUG】邮箱和手机号都为空，生成随机邮箱: " + actualEmail);
                }
            }

            // 验证验证码
            String contact = phone != null ? phone : actualEmail;
            System.out.println("【DEBUG】使用联系方式进行验证码验证: " + contact);

            VerificationCode validCode = verificationCodeMapper.findValidCode(contact, code, 1);
            if (validCode == null) {
                System.out.println("【DEBUG】验证码验证失败");
                response.put("success", false);
                response.put("message", "验证码无效或已过期");
                return response;
            }

            System.out.println("【DEBUG】验证码验证成功");

            // 🔥 第二步：检查手机号是否已存在
            if (phone != null) {
                List<User> users = userMapper.findListByPhone(phone);
                if (users != null && !users.isEmpty()) {
                    System.out.println("【DEBUG】手机号已存在: " + phone);
                    response.put("success", false);
                    response.put("message", "手机号已存在");
                    return response;
                }
            }

            // 🔥 第三步：检查邮箱是否已存在（使用处理后的actualEmail）
            if (userMapper.findByEmail(actualEmail) != null) {
                System.out.println("【DEBUG】邮箱已存在: " + actualEmail);
                response.put("success", false);
                response.put("message", "邮箱已存在");
                return response;
            }

            System.out.println("【DEBUG】手机号和邮箱检查通过");

            // 创建用户
            User user = new User();
            user.setPhone(phone);
            user.setEmail(actualEmail);  // 🔥 使用处理后的actualEmail
            user.setNickname(nickname != null ? nickname : "用户" + (phone != null ? phone : actualEmail));
            user.setIsActive(1);

            // === 自动生成用户名 ===
            String generatedUsername;
            if (actualEmail != null && !actualEmail.trim().isEmpty()) {
                // 使用邮箱前缀作为用户名
                generatedUsername = actualEmail.split("@")[0];
            } else if (phone != null && !phone.trim().isEmpty()) {
                // 使用手机号后4位作为用户名
                generatedUsername = "user_" + phone.substring(phone.length() - 4);
            } else {
                // 备用方案：使用时间戳
                generatedUsername = "user_" + System.currentTimeMillis();
            }
            user.setUsername(generatedUsername);
            System.out.println("【DEBUG】自动生成的用户名: " + generatedUsername);

            // 设置密码（如果有）
            if (password != null && !password.trim().isEmpty()) {
                String hashedPassword = passwordEncoder.encode(password);
                user.setPasswordHash(hashedPassword);
                System.out.println("【DEBUG】设置密码，已加密");
            }

            System.out.println("【DEBUG】准备保存用户到数据库");
            boolean success = userMapper.insert(user) > 0;
            if (success) {
                verificationCodeMapper.markAsUsed(validCode.getId());
                System.out.println("【DEBUG】注册成功，用户ID: " + user.getId());
                response.put("success", true);
                response.put("message", "注册成功");
                response.put("userId", user.getId());
            } else {
                System.out.println("【DEBUG】注册失败，数据库插入失败");
                response.put("success", false);
                response.put("message", "注册失败");
            }

        } catch (Exception e) {
            System.out.println("【ERROR】注册异常: " + e.getMessage());
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

            User user = null;
            if (phone != null) {
                List<User> users = userMapper.findListByPhone(phone);
                if (users != null && !users.isEmpty()) {
                    if (users.size() > 1) {
                        System.err.println("【ERROR】手机号 " + phone + " 存在多个用户，无法登录");
                        response.put("success", false);
                        response.put("message", "账号存在异常，请联系管理员");
                        return response;
                    }
                    user = users.get(0);
                }
            } else {
                user = userMapper.findByEmail(email);
            }

            if (user != null) {
                verificationCodeMapper.markAsUsed(validCode.getId());

                // 检查邮箱是否为空，如果为空，使用手机号生成临时邮箱
                String userEmail = user.getEmail();
                if (userEmail == null || userEmail.trim().isEmpty()) {
                    System.out.println("【WARN】用户 " + user.getId() + " 邮箱为空，使用手机号生成临时邮箱");
                    if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
                        userEmail = user.getPhone() + "@qushetai.com";
                    } else {
                        userEmail = "user_" + user.getId() + "@qushetai.com";
                    }
                }

                // 使用JWT token而不是临时token
                String token = jwtUtil.generateToken(userEmail, user.getId());

                // 安全构建用户信息
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("email", userEmail);
                userInfo.put("nickname", user.getNickname() != null ? user.getNickname() : "");
                userInfo.put("isAdmin", user.getIsAdmin() != null ? user.getIsAdmin() : 0);

                // 统一响应格式 - 修复字段名问题
                Map<String, Object> data = new HashMap<>();
                data.put("token", token);
                data.put("userInfo", userInfo);  // 🔥 修复：改为 "userInfo"

                response.put("success", true);
                response.put("message", "登录成功");
                response.put("data", data);

                // 添加数据验证日志
                System.out.println("【DEBUG】=== 验证码登录响应数据验证 ===");
                System.out.println("【DEBUG】token: " + (token != null ? token : "null"));
                System.out.println("【DEBUG】userInfo: " + userInfo);
                System.out.println("【DEBUG】data: " + data);
                System.out.println("【DEBUG】完整response: " + response);

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

    // 密码登录 - 修复版本，添加重复记录保护
    public Map<String, Object> loginWithPassword(String phone, String email, String password) {
        Map<String, Object> response = new HashMap<>();

        try {
            System.out.println("【DEBUG】UserService.loginWithPassword - 开始处理登录请求");
            System.out.println("【DEBUG】参数 - phone: " + phone + ", email: " + email);

            User user = null;
            try {
                if (phone != null) {
                    List<User> users = userMapper.findListByPhone(phone);
                    if (users != null && !users.isEmpty()) {
                        if (users.size() > 1) {
                            System.err.println("【ERROR】手机号 " + phone + " 存在多个用户，无法登录");
                            response.put("success", false);
                            response.put("message", "账号存在异常，请联系管理员");
                            return response;
                        }
                        user = users.get(0);
                    }
                } else {
                    user = userMapper.findByEmail(email);
                }
            } catch (Exception e) {
                System.err.println("【ERROR】查询用户异常: " + e.getMessage());
                response.put("success", false);
                response.put("message", "系统异常，请稍后重试");
                return response;
            }

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

            // 调试信息：打印密码相关信息
            System.out.println("【DEBUG】数据库存储的密码: " + user.getPasswordHash());
            System.out.println("【DEBUG】输入的密码: " + password);
            System.out.println("【DEBUG】密码长度 - 数据库: " + (user.getPasswordHash() != null ? user.getPasswordHash().length() : 0));
            System.out.println("【DEBUG】密码是否以$2a$开头: " + (user.getPasswordHash() != null && user.getPasswordHash().startsWith("$2a$")));

            // 密码验证逻辑 - 修复版本（添加了密码自动修复逻辑）
            boolean passwordMatch;

            // 如果密码是BCrypt格式，使用BCrypt验证
            if (user.getPasswordHash().startsWith("$2a$")) {
                passwordMatch = passwordEncoder.matches(password, user.getPasswordHash());
                System.out.println("【DEBUG】使用BCrypt验证，结果: " + passwordMatch);
            } else {
                // 如果是明文密码，直接比较
                passwordMatch = password.equals(user.getPasswordHash());
                System.out.println("【DEBUG】使用明文比较，结果: " + passwordMatch);

                // 🔥 关键修复：如果是明文密码且验证成功，自动转换为BCrypt格式
                if (passwordMatch) {
                    String hashedPassword = passwordEncoder.encode(password);
                    userMapper.updatePassword(user.getId(), hashedPassword);
                    System.out.println("【DEBUG】自动将明文密码转换为BCrypt格式并更新数据库");
                    System.out.println("【DEBUG】新密码哈希: " + hashedPassword);
                }
            }

            System.out.println("【DEBUG】最终密码验证结果: " + passwordMatch);

            if (passwordMatch) {
                // === 新增：检查邮箱是否为空，如果为空，使用手机号生成临时邮箱 ===
                String userEmail = user.getEmail();
                if (userEmail == null || userEmail.trim().isEmpty()) {
                    System.out.println("【WARN】用户 " + user.getId() + " 邮箱为空，使用手机号生成临时邮箱");
                    // 可以选择在这里更新数据库，或者只是记录警告
                    if (user.getPhone() != null && !user.getPhone().trim().isEmpty()) {
                        userEmail = user.getPhone() + "@qushetai.com";
                        // 可选：更新数据库
                        // userMapper.updateEmail(user.getId(), user.getEmail());
                    } else {
                        // 如果手机号也为空，使用用户ID生成邮箱
                        userEmail = "user_" + user.getId() + "@qushetai.com";
                    }
                    System.out.println("【DEBUG】生成的临时邮箱: " + userEmail);
                }
                // === 新增结束 ===

                // 生成JWT token而不是临时token
                String token = jwtUtil.generateToken(userEmail, user.getId());
                System.out.println("【DEBUG】生成的JWT token: " + token);

                // 安全构建用户信息
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("email", userEmail);
                userInfo.put("nickname", user.getNickname() != null ? user.getNickname() : "");
                userInfo.put("isAdmin", user.getIsAdmin() != null ? user.getIsAdmin() : 0);

                System.out.println("【DEBUG】构建的用户信息: " + userInfo);

                // 🔥 关键修复：统一响应格式，使用 "userInfo" 而不是 "user"
                Map<String, Object> data = new HashMap<>();
                data.put("token", token);
                data.put("userInfo", userInfo);  // 🔥 修复：改为 "userInfo"

                response.put("success", true);
                response.put("message", "登录成功");
                response.put("data", data);

                // 🔥 添加详细的数据验证日志
                System.out.println("【DEBUG】=== 密码登录响应数据验证 ===");
                System.out.println("【DEBUG】token类型: " + (token != null ? token.getClass().getName() : "null"));
                System.out.println("【DEBUG】token值: " + token);
                System.out.println("【DEBUG】userInfo类型: " + (userInfo != null ? userInfo.getClass().getName() : "null"));
                System.out.println("【DEBUG】userInfo值: " + userInfo);
                System.out.println("【DEBUG】data Map内容: " + data);
                System.out.println("【DEBUG】完整response: " + response);

                // 验证数据不为null
                if (token == null) {
                    System.err.println("【ERROR】token为null！");
                }
                if (userInfo == null) {
                    System.err.println("【ERROR】userInfo为null！");
                }

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

    // 修改：更新用户空闲时间，添加空数组处理和错误处理
    public Map<String, Object> updateUserFreeTime(Long userId, String freeTimeSlots) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 处理空数组情况
            if (freeTimeSlots == null || freeTimeSlots.trim().isEmpty() || "[]".equals(freeTimeSlots)) {
                // 清空空闲时间
                freeTimeSlots = ""; // 或者 "[]" 根据你的需求
                System.out.println("【DEBUG】清空用户 " + userId + " 的空闲时间");
            }

            int rows = userMapper.updateFreeTime(userId, freeTimeSlots);

            if (rows > 0) {
                result.put("success", true);
                result.put("message", "空闲时间更新成功");
            } else {
                result.put("success", false);
                result.put("message", "空闲时间更新失败");
            }
        } catch (Exception e) {
            // 记录详细错误日志，但不暴露给前端
            System.err.println("【ERROR】更新用户空闲时间失败，用户ID: " + userId);
            System.err.println("【ERROR】SQL错误: " + e.getMessage());

            // 返回友好的错误信息，不暴露SQL细节
            result.put("success", false);
            result.put("message", "更新空闲时间失败：参数格式错误");
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

    // ========== 新增：标签管理方法（处理JSON格式标签数组） ==========

    /**
     * 获取用户标签列表（返回 List<String>）
     */
    public List<String> getUserTags(Long userId) {
        try {
            User user = userMapper.findById(userId);
            if (user != null && user.getInterestTags() != null && !user.getInterestTags().trim().isEmpty()) {
                String tagsJson = user.getInterestTags();
                System.out.println("【DEBUG】UserService.getUserTags - 原始标签JSON: " + tagsJson);

                // 解析JSON字符串为List
                if (tagsJson.startsWith("[") && tagsJson.endsWith("]")) {
                    List<String> tags = objectMapper.readValue(tagsJson, new TypeReference<List<String>>() {});
                    System.out.println("【DEBUG】UserService.getUserTags - 解析后的标签: " + tags);
                    return tags;
                } else {
                    // 如果是逗号分隔的字符串
                    List<String> tags = Arrays.asList(tagsJson.split(","));
                    System.out.println("【DEBUG】UserService.getUserTags - 分割后的标签: " + tags);
                    return tags;
                }
            }
        } catch (Exception e) {
            System.err.println("【ERROR】解析用户标签失败: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    /**
     * 保存用户标签列表（替换整个标签数组）
     */
    public Map<String, Object> saveUserTags(Long userId, List<String> tags) {
        Map<String, Object> result = new HashMap<>();
        try {
            System.out.println("【DEBUG】UserService.saveUserTags - 保存用户标签，userId=" + userId + ", tags=" + tags);

            // 将List转换为JSON字符串
            String tagsJson = objectMapper.writeValueAsString(tags);
            System.out.println("【DEBUG】转换后的JSON: " + tagsJson);

            int rows = userMapper.updateInterests(userId, tagsJson);
            if (rows > 0) {
                result.put("success", true);
                result.put("message", "标签保存成功");
                result.put("tags", tags);  // 返回保存的标签
            } else {
                result.put("success", false);
                result.put("message", "标签保存失败");
            }
        } catch (Exception e) {
            System.err.println("【ERROR】保存用户标签失败: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "服务器错误: " + e.getMessage());
        }
        return result;
    }

    /**
     * 删除用户指定的标签
     */
    public Map<String, Object> deleteUserTags(Long userId, List<String> tagsToDelete) {
        Map<String, Object> result = new HashMap<>();
        try {
            System.out.println("【DEBUG】UserService.deleteUserTags - 删除用户标签，userId=" + userId + ", tagsToDelete=" + tagsToDelete);

            // 获取现有标签
            List<String> currentTags = getUserTags(userId);
            System.out.println("【DEBUG】当前标签: " + currentTags);

            // 移除要删除的标签
            List<String> updatedTags = new ArrayList<>(currentTags);
            for (String tagToDelete : tagsToDelete) {
                updatedTags.remove(tagToDelete);
            }
            System.out.println("【DEBUG】删除后的标签: " + updatedTags);

            // 保存更新后的标签
            return saveUserTags(userId, updatedTags);
        } catch (Exception e) {
            System.err.println("【ERROR】删除用户标签失败: " + e.getMessage());
            result.put("success", false);
            result.put("message", "删除标签失败: " + e.getMessage());
            return result;
        }
    }
}