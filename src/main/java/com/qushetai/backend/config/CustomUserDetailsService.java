package com.qushetai.backend.config;

import com.qushetai.backend.entity.User;
import com.qushetai.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("【DEBUG】CustomUserDetailsService - 开始查找用户: " + username);
        System.out.println("【DEBUG】输入用户名长度: " + username.length());
        System.out.println("【DEBUG】输入用户名类型检测 - 包含@: " + username.contains("@") +
                ", 是11位数字: " + username.matches("\\d{11}") +
                ", 是临时邮箱格式: " + username.matches("\\d+@qushetai\\.com"));

        User user = null;
        String foundBy = "未找到";

        // 如果是邮箱格式（包含@符号）
        if (username.contains("@")) {
            System.out.println("【DEBUG】尝试按邮箱查找用户: " + username);
            user = userService.findByEmail(username);
            if (user != null) {
                foundBy = "邮箱";
                System.out.println("【DEBUG】邮箱查找成功: 用户ID=" + user.getId() + ", 邮箱=" + user.getEmail());
            } else {
                System.out.println("【DEBUG】邮箱查找失败: " + username);
            }
        }

        // 如果邮箱查找失败，尝试手机号查找
        if (user == null && username.matches("\\d{11}")) {
            System.out.println("【DEBUG】尝试按手机号查找用户: " + username);
            user = userService.findByPhone(username);
            if (user != null) {
                foundBy = "手机号";
                System.out.println("【DEBUG】手机号查找成功: 用户ID=" + user.getId() + ", 手机号=" + user.getPhone());
            } else {
                System.out.println("【DEBUG】手机号查找失败: " + username);
            }
        }

        // 如果是临时邮箱格式（手机号@qushetai.com）
        if (user == null && username.matches("\\d+@qushetai\\.com")) {
            System.out.println("【DEBUG】尝试按临时邮箱格式查找用户: " + username);
            // 提取手机号部分
            String phone = username.split("@")[0];
            System.out.println("【DEBUG】从临时邮箱提取的手机号: " + phone + " (长度: " + phone.length() + ")");
            user = userService.findByPhone(phone);
            if (user != null) {
                foundBy = "临时邮箱";
                System.out.println("【DEBUG】临时邮箱查找成功: 用户ID=" + user.getId() + ", 手机号=" + user.getPhone());
            } else {
                System.out.println("【DEBUG】临时邮箱查找失败: " + username);
            }
        }

        // 如果还是找不到，尝试所有可能的查找方式
        if (user == null) {
            System.out.println("【DEBUG】开始备用查找方式...");

            // 尝试直接按用户名查找（可能是ID）
            try {
                Long userId = Long.parseLong(username);
                System.out.println("【DEBUG】尝试按ID查找用户: " + userId);
                user = userService.findUserById(userId);
                if (user != null) {
                    foundBy = "用户ID";
                    System.out.println("【DEBUG】ID查找成功: 用户ID=" + user.getId());
                } else {
                    System.out.println("【DEBUG】ID查找失败: " + userId);
                }
            } catch (NumberFormatException e) {
                System.out.println("【DEBUG】用户名不是有效的数字ID: " + username);
            }

            // 如果仍然找不到，尝试用户名查找（如果有用户名字段）
            if (user == null) {
                System.out.println("【DEBUG】尝试按用户名查找: " + username);
                // 这里假设 UserService 有 findByUsername 方法
                // user = userService.findByUsername(username);
                // if (user != null) {
                //     foundBy = "用户名";
                //     System.out.println("【DEBUG】用户名查找成功: 用户ID=" + user.getId());
                // }
            }
        }

        if (user == null) {
            System.err.println("【ERROR】CustomUserDetailsService - 所有查找方式都失败，用户不存在: " + username);
            System.err.println("【ERROR】已尝试的查找方式: 邮箱、手机号、临时邮箱、用户ID");
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        System.out.println("【DEBUG】CustomUserDetailsService - 用户查找完成");
        System.out.println("【DEBUG】查找方式: " + foundBy);
        System.out.println("【DEBUG】用户详情: ID=" + user.getId() +
                ", 邮箱=" + user.getEmail() +
                ", 手机=" + user.getPhone() +
                ", 密码哈希是否存在: " + (user.getPasswordHash() != null));

        // 构建 UserDetails 对象
        String password = user.getPasswordHash() != null ? user.getPasswordHash() : "";
        System.out.println("【DEBUG】密码长度: " + password.length());

        System.out.println("【DEBUG】开始构建Spring Security User对象...");
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                username, // 使用传入的用户名（可能是邮箱、手机号或临时邮箱）
                password,
                Collections.emptyList() // 这里可以添加用户角色/权限
        );
        System.out.println("【DEBUG】Spring Security User对象构建完成");

        return userDetails;
    }
}