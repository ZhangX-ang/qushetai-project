package com.qushetai.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    // 定义一些明显的公开路径（用于调试）
    private final List<String> PUBLIC_PATHS = Arrays.asList(
            "/auth/",
            "/api/auth/",
            "/api/users/login",
            "/api/users/register",
            "/api/users/login-pwd",
            "/api/activities/test",
            "/api/activities/public/",
            "/api/activities/health",
            "/activities/test",
            "/activities/public/",
            "/activities/health",
            "/test/",
            "/hello",
            "/health",
            "/status",
            "/api/health",
            "/recommendations/public/"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        // 调试日志：请求开始
        System.out.println("\n【DEBUG】=== JWT过滤器开始处理请求 ===");
        System.out.println("【DEBUG】请求URI: " + requestUri);
        System.out.println("【DEBUG】请求方法: " + request.getMethod());
        System.out.println("【DEBUG】请求来源: " + request.getRemoteAddr());

        // 检查是否是公开路径
        boolean isPublicPath = isPublicPath(requestUri);
        System.out.println("【DEBUG】是公开路径吗? " + (isPublicPath ? "是" : "否"));

        try {
            String jwt = getJwtFromRequest(request);

            // 调试日志：打印请求的URL和是否有Token
            System.out.println("【DEBUG】Authorization头: " + request.getHeader("Authorization"));
            System.out.println("【DEBUG】JWT Token: " + (jwt != null ? "存在(" + (jwt.length() > 20 ? jwt.substring(0, 20) + "..." : jwt) + ")" : "null"));

            if (jwt != null && jwtUtil.validateToken(jwt)) {
                String username = jwtUtil.getUsernameFromToken(jwt);
                Long userId = jwtUtil.getUserIdFromToken(jwt);

                System.out.println("【DEBUG】从Token解析 - 用户ID: " + userId + ", 用户名: " + username);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                // 将用户ID存入request属性，方便后续使用
                request.setAttribute("userId", userId);
                System.out.println("【DEBUG】设置用户ID到request: " + userId);
                System.out.println("【DEBUG】安全上下文已设置认证");
            } else {
                if (jwt == null) {
                    System.out.println("【DEBUG】未提供JWT Token，将作为匿名用户处理");
                } else {
                    System.out.println("【DEBUG】JWT Token无效或已过期");
                }
                // 确保安全上下文为空（避免使用之前的认证信息）
                SecurityContextHolder.clearContext();
                System.out.println("【DEBUG】安全上下文已清除");
            }
        } catch (Exception e) {
            System.err.println("【ERROR】JwtAuthenticationFilter异常: " + e.getMessage());
            e.printStackTrace();
            // 发生异常时也清除安全上下文
            SecurityContextHolder.clearContext();
            System.out.println("【DEBUG】发生异常，安全上下文已清除");
        }

        // 🔥 这里必须调用，否则请求会卡住
        System.out.println("【DEBUG】正在调用filterChain.doFilter()...");
        filterChain.doFilter(request, response);
        System.out.println("【DEBUG】=== JWT过滤器处理完成 ===\n");
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private boolean isPublicPath(String requestUri) {
        // 检查请求URI是否以任何公开路径开头
        for (String publicPath : PUBLIC_PATHS) {
            if (requestUri.startsWith(publicPath)) {
                return true;
            }
        }
        return false;
    }
}