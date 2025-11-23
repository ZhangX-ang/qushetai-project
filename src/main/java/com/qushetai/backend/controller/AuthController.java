package com.qushetai.backend.controller;

import com.qushetai.backend.config.JwtUtil;
import com.qushetai.backend.entity.User;
import com.qushetai.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        return userService.sendVerificationCode(contact, type);
    }

    // 验证码登录 - 统一响应格式
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            String contact = request.get("contact");
            String code = request.get("code");

            // 这里应该调用验证码验证逻辑
            // 假设验证通过
            User user = userService.findByEmail(contact);
            if (user != null) {
                String token = jwtUtil.generateToken(user.getEmail(), user.getId());

                // 构建用户信息
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("email", user.getEmail());
                userInfo.put("nickname", user.getNickname() != null ? user.getNickname() : "");
                userInfo.put("isAdmin", user.getIsAdmin() != null ? user.getIsAdmin() : 0);

                // 构建响应数据
                Map<String, Object> data = new HashMap<>();
                data.put("token", token);
                data.put("userInfo", userInfo);

                Map<String, Object> response = buildResponse(200, true, "登录成功", data);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = buildResponse(400, false, "用户不存在", null);
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "验证码登录失败";
            Map<String, Object> response = buildResponse(500, false, errorMessage, null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 密码登录 - 统一响应格式
    @PostMapping("/login-pwd")
    public ResponseEntity<?> loginWithPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");

            User user = userService.findByEmail(email);
            if (user != null && user.getPasswordHash() != null) {
                // 验证密码
                if (passwordEncoder.matches(password, user.getPasswordHash())) {
                    String token = jwtUtil.generateToken(user.getEmail(), user.getId());

                    // 构建用户信息
                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("id", user.getId());
                    userInfo.put("email", user.getEmail());
                    userInfo.put("nickname", user.getNickname() != null ? user.getNickname() : "");
                    userInfo.put("isAdmin", user.getIsAdmin() != null ? user.getIsAdmin() : 0);

                    // 构建响应数据
                    Map<String, Object> data = new HashMap<>();
                    data.put("token", token);
                    data.put("userInfo", userInfo);

                    Map<String, Object> response = buildResponse(200, true, "登录成功", data);
                    return ResponseEntity.ok(response);
                } else {
                    Map<String, Object> response = buildResponse(400, false, "密码错误", null);
                    return ResponseEntity.badRequest().body(response);
                }
            } else {
                Map<String, Object> response = buildResponse(400, false, "用户不存在或未设置密码", null);
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "密码登录失败";
            Map<String, Object> response = buildResponse(500, false, errorMessage, null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    // 注册接口 - 统一响应格式
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            String nickname = request.get("nickname");

            // 检查用户是否已存在
            User existingUser = userService.findByEmail(email);
            if (existingUser != null) {
                Map<String, Object> response = buildResponse(400, false, "邮箱已被注册", null);
                return ResponseEntity.badRequest().body(response);
            }

            // 创建新用户
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setPasswordHash(passwordEncoder.encode(password));
            newUser.setNickname(nickname != null ? nickname : email.split("@")[0]);
            newUser.setIsAdmin(0); // 新注册用户默认不是管理员

            // 保存用户
            boolean saved = userService.saveUser(newUser);
            if (saved) {
                String token = jwtUtil.generateToken(newUser.getEmail(), newUser.getId());

                // 构建用户信息
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", newUser.getId());
                userInfo.put("email", newUser.getEmail());
                userInfo.put("nickname", newUser.getNickname() != null ? newUser.getNickname() : "");
                userInfo.put("isAdmin", newUser.getIsAdmin() != null ? newUser.getIsAdmin() : 0);

                // 构建响应数据
                Map<String, Object> data = new HashMap<>();
                data.put("token", token);
                data.put("userInfo", userInfo);

                Map<String, Object> response = buildResponse(200, true, "注册成功", data);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = buildResponse(500, false, "注册失败", null);
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "注册失败";
            Map<String, Object> response = buildResponse(500, false, errorMessage, null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 健康检查接口（测试依赖注入）
     */
    @GetMapping("/health-check")
    public ResponseEntity<?> healthCheck() {
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("service", "AuthController");
            health.put("status", "running");
            health.put("timestamp", System.currentTimeMillis());

            // 安全构建依赖信息
            Map<String, Object> dependencies = new HashMap<>();
            dependencies.put("userService", userService != null ? "injected" : "null");
            dependencies.put("jwtUtil", jwtUtil != null ? "injected" : "null");
            dependencies.put("passwordEncoder", passwordEncoder != null ? "injected" : "null");
            health.put("dependencies", dependencies);

            System.out.println("【DEBUG】健康检查: " + health);

            Map<String, Object> response = buildResponse(200, true, "服务正常", health);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("【ERROR】健康检查异常: " + e.getMessage());
            Map<String, Object> response = buildResponse(500, false, "健康检查异常: " + e.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 硬编码测试token（临时解决方案）- 统一响应格式
     */
    @GetMapping("/hardcoded-token")
    public ResponseEntity<?> hardcodedToken() {
        try {
            System.out.println("【DEBUG】生成硬编码测试token");

            // 硬编码用户信息（基于数据库中的真实用户）
            Long userId = 1L;
            String userEmail = "admin@qushetai.com";
            String userNickname = "于睿";

            // 直接使用JwtUtil生成token（如果jwtUtil为null会报错）
            if (jwtUtil == null) {
                System.err.println("【ERROR】jwtUtil为null，使用模拟token");

                // 构建用户信息
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", userId);
                userInfo.put("email", userEmail);
                userInfo.put("nickname", userNickname);
                userInfo.put("isAdmin", 1);

                // 构建响应数据
                Map<String, Object> data = new HashMap<>();
                data.put("token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbkBxdXNoZXRhaS5jb20iLCJ1c2VySWQiOjEsImlhdCI6MTcxMDAwMDAwMCwiZXhwIjoxNzEwMDg2NDAwfQ.simulated_token_for_testing");
                data.put("userInfo", userInfo);
                data.put("note", "这是一个模拟token，仅用于测试接口连通性");

                Map<String, Object> response = buildResponse(200, true, "模拟token生成成功（jwtUtil未注入）", data);
                return ResponseEntity.ok(response);
            } else {
                // 正常生成token
                String token = jwtUtil.generateToken(userEmail, userId);

                // 构建用户信息
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", userId);
                userInfo.put("email", userEmail);
                userInfo.put("nickname", userNickname);
                userInfo.put("isAdmin", 1);

                // 构建响应数据
                Map<String, Object> data = new HashMap<>();
                data.put("token", token);
                data.put("userInfo", userInfo);

                Map<String, Object> response = buildResponse(200, true, "硬编码token生成成功", data);
                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            System.err.println("【ERROR】硬编码token异常: " + e.getMessage());
            Map<String, Object> response = buildResponse(500, false, "硬编码token失败: " + e.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取测试token（开发用）- 修复版本 - 统一响应格式
     */
    @GetMapping("/get-test-token")
    public ResponseEntity<?> getTestToken() {
        try {
            System.out.println("【DEBUG】开始获取测试token");

            // 详细检查所有依赖
            System.out.println("【DEBUG】userService: " + userService);
            System.out.println("【DEBUG】jwtUtil: " + jwtUtil);
            System.out.println("【DEBUG】passwordEncoder: " + passwordEncoder);

            if (userService == null) {
                System.err.println("【ERROR】userService 注入失败！");
                Map<String, Object> response = buildResponse(500, false, "userService 注入失败", null);
                return ResponseEntity.badRequest().body(response);
            }

            if (jwtUtil == null) {
                System.err.println("【ERROR】jwtUtil 注入失败！");
                Map<String, Object> response = buildResponse(500, false, "jwtUtil 注入失败", null);
                return ResponseEntity.badRequest().body(response);
            }

            // 查找管理员用户
            System.out.println("【DEBUG】尝试通过邮箱查找: admin@qushetai.com");
            User adminUser = userService.findByEmail("admin@qushetai.com");
            System.out.println("【DEBUG】邮箱查找结果: " + adminUser);

            if (adminUser == null) {
                System.out.println("【DEBUG】邮箱查找失败，尝试通过ID=1查找");
                adminUser = userService.findUserById(1L);
                System.out.println("【DEBUG】ID查找结果: " + adminUser);
            }

            if (adminUser != null) {
                System.out.println("【DEBUG】找到用户，开始生成token");
                System.out.println("【DEBUG】用户信息 - ID: " + adminUser.getId() + ", Email: " + adminUser.getEmail());

                // 详细检查用户权限字段
                System.out.println("【DEBUG】用户权限字段 - isAdmin: " + adminUser.getIsAdmin());
                System.out.println("【DEBUG】用户权限字段类型: " + (adminUser.getIsAdmin() != null ? adminUser.getIsAdmin().getClass().getName() : "null"));

                // 临时修复：如果权限字段为null或0，强制设置为1（管理员）
                Integer actualIsAdmin = adminUser.getIsAdmin();
                if (actualIsAdmin == null || actualIsAdmin == 0) {
                    System.out.println("【DEBUG】检测到权限字段异常，临时修复为管理员权限");
                    actualIsAdmin = 1;
                }
                System.out.println("【DEBUG】最终权限值: " + actualIsAdmin);

                String token = jwtUtil.generateToken(adminUser.getEmail(), adminUser.getId());
                System.out.println("【DEBUG】Token生成成功: " + token);

                // 构建用户信息
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", adminUser.getId() != null ? adminUser.getId() : 0);
                userInfo.put("email", adminUser.getEmail() != null ? adminUser.getEmail() : "");
                userInfo.put("nickname", adminUser.getNickname() != null ? adminUser.getNickname() : "");
                userInfo.put("isAdmin", actualIsAdmin);

                // 构建响应数据
                Map<String, Object> data = new HashMap<>();
                data.put("token", token);
                data.put("userInfo", userInfo);
                data.put("usage", "在请求头中添加: Authorization: Bearer " + token);

                Map<String, Object> response = buildResponse(200, true, "测试token生成成功", data);
                return ResponseEntity.ok(response);
            } else {
                System.out.println("【DEBUG】未找到任何管理员用户");
                Map<String, Object> response = buildResponse(404, false, "未找到管理员用户", null);
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            System.err.println("【ERROR】获取测试token异常: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> response = buildResponse(500, false, "获取测试token失败: " + e.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 调试用户权限信息 - 统一响应格式
     */
    @GetMapping("/debug-user-permission")
    public ResponseEntity<?> debugUserPermission() {
        try {
            System.out.println("【DEBUG】开始调试用户权限信息");

            User adminUser = userService.findByEmail("admin@qushetai.com");
            if (adminUser != null) {
                // 构建详细的调试信息
                Map<String, Object> debugInfo = new HashMap<>();
                debugInfo.put("userExists", true);
                debugInfo.put("userId", adminUser.getId());
                debugInfo.put("userEmail", adminUser.getEmail());
                debugInfo.put("userNickname", adminUser.getNickname());
                debugInfo.put("isAdminRaw", adminUser.getIsAdmin());
                debugInfo.put("isAdminType", adminUser.getIsAdmin() != null ? adminUser.getIsAdmin().getClass().getName() : "null");
                debugInfo.put("userClass", adminUser.getClass().getName());
                debugInfo.put("userToString", adminUser.toString());

                Map<String, Object> response = buildResponse(200, true, "用户权限调试信息", debugInfo);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = buildResponse(404, false, "未找到管理员用户", null);
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            System.err.println("【ERROR】调试用户权限异常: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> response = buildResponse(500, false, "调试用户权限失败: " + e.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 创建测试管理员用户（开发用）- 统一响应格式
     */
    @PostMapping("/create-test-user")
    public ResponseEntity<?> createTestUser() {
        try {
            System.out.println("【DEBUG】开始创建测试用户");

            // 检查是否已存在
            User existingUser = userService.findByEmail("test@qushetai.com");
            if (existingUser != null) {
                // 构建用户信息
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", existingUser.getId());
                userInfo.put("email", existingUser.getEmail());
                userInfo.put("nickname", existingUser.getNickname() != null ? existingUser.getNickname() : "");
                userInfo.put("isAdmin", existingUser.getIsAdmin() != null ? existingUser.getIsAdmin() : 0);

                Map<String, Object> response = buildResponse(200, true, "测试用户已存在", userInfo);
                return ResponseEntity.ok(response);
            }

            // 创建新用户
            User newUser = new User();
            newUser.setEmail("test@qushetai.com");
            newUser.setPasswordHash(passwordEncoder.encode("123456"));
            newUser.setNickname("测试用户");
            newUser.setIsAdmin(1);

            System.out.println("【DEBUG】准备保存用户: " + newUser.getEmail());
            boolean saved = userService.saveUser(newUser);
            System.out.println("【DEBUG】用户保存结果: " + saved);

            if (saved) {
                // 构建用户信息
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", newUser.getId());
                userInfo.put("email", newUser.getEmail());
                userInfo.put("nickname", newUser.getNickname() != null ? newUser.getNickname() : "");
                userInfo.put("isAdmin", newUser.getIsAdmin() != null ? newUser.getIsAdmin() : 0);

                Map<String, Object> response = buildResponse(200, true, "测试用户创建成功", userInfo);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = buildResponse(500, false, "测试用户创建失败", null);
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            System.err.println("【ERROR】创建测试用户异常: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> response = buildResponse(500, false, "创建测试用户失败: " + e.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 为C同学生成测试Token（开发用）- 统一响应格式
     */
    @GetMapping("/test-token-for-c")
    public ResponseEntity<?> getTestTokenForC() {
        try {
            System.out.println("【DEBUG】开始为C同学生成测试Token");

            // 查找或创建测试用户
            User testUser = userService.findByEmail("c_test@qushetai.com");
            if (testUser == null) {
                System.out.println("【DEBUG】未找到C测试用户，开始创建新用户");

                // 创建测试用户
                testUser = new User();
                testUser.setEmail("c_test@qushetai.com");
                testUser.setPasswordHash(passwordEncoder.encode("123456"));
                testUser.setNickname("C测试用户");
                testUser.setInterestTags("[\"技术\", \"学习\", \"社交\"]");
                testUser.setIsAdmin(0);

                System.out.println("【DEBUG】准备保存C测试用户");
                boolean saved = userService.saveUser(testUser);
                System.out.println("【DEBUG】C测试用户保存结果: " + saved);

                if (!saved) {
                    Map<String, Object> response = buildResponse(500, false, "创建测试用户失败", null);
                    return ResponseEntity.badRequest().body(response);
                }

                // 重新获取用户以确保ID被设置
                testUser = userService.findByEmail("c_test@qushetai.com");
                System.out.println("【DEBUG】重新获取的C测试用户: " + testUser);
            } else {
                System.out.println("【DEBUG】找到现有C测试用户: " + testUser);
            }

            // 生成Token
            String token = jwtUtil.generateToken(testUser.getEmail(), testUser.getId());
            System.out.println("【DEBUG】为C测试用户生成Token成功: " + token);

            // 构建用户信息
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", testUser.getId());
            userInfo.put("email", testUser.getEmail());
            userInfo.put("nickname", testUser.getNickname());
            userInfo.put("interestTags", testUser.getInterestTags());
            userInfo.put("isAdmin", testUser.getIsAdmin());

            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("userInfo", userInfo);
            data.put("usage", "在请求头中添加: Authorization: Bearer " + token);

            Map<String, Object> response = buildResponse(200, true, "C同学测试Token生成成功", data);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("【ERROR】生成C同学测试Token失败: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> response = buildResponse(500, false, "生成测试Token失败: " + e.getMessage(), null);
            return ResponseEntity.badRequest().body(response);
        }
    }
}