package com.qushetai.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 公开接口 - 开发测试用
                        .requestMatchers(
                                // 基础测试接口
                                "/hello",
                                "/test/**",

                                // 认证相关
                                "/auth/**",
                                "/api/users/register",
                                "/api/users/login",
                                "/api/users/login-pwd",
                                "/api/users/send-code",

                                // 行为数据采集
                                "/api/behavior/**",

                                // 推荐系统
                                "/recommendations/cold-start",
                                "/recommendations/public-test",
                                "/recommendations/public/**",
                                "/recommendations/cache/status",
                                "/recommendations/health",
                                "/recommendations/for-current-user",
                                "/recommendations/by-interests",
                                "/recommendations/explore",
                                "/recommendations/status",
                                "/recommendations/feedback",
                                "/recommendations/admin/user/**",
                                "/recommendations/**",

                                // 监控与调试
                                "/monitor/**",
                                "/debug/**",
                                "/health",
                                "/status",
                                "/api/health",

                                // 消息系统测试
                                "/messages/test",

                                // 管理员公开测试
                                "/admin/public-test",

                                // 活动相关测试
                                "/api/activities",
                                "/api/activities/**",
                                "/api/activities/hello",
                                "/api/activities/test",
                                "/api/activities/health",
                                "/api/activities/public/**",
                                "/api/activities/status",
                                "/activities",
                                "/activities/**",
                                "/activities/hello",
                                "/activities/test",
                                "/activities/health",
                                "/activities/public/**",
                                "/activities/status",

                                // 兴趣标签相关（新增）
                                "/api/interests/**",
                                "/api/user/interests",
                                "/api/tags/available",
                                "/api/tags/available-from-db",
                                "/api/tags/categories",
                                "/api/tags/popular",
                                "/api/tags/{tagName}/activities",

                                // 标签相关接口（新增）
                                "/api/user/tags/available",           // 获取可用标签（公开）
                                "/api/user/tags/available-from-db",   // 从数据库获取可用标签（公开）
                                "/api/user/tags",                     // GET 获取用户标签（为调试暂时公开，生产环境应该需要认证）
                                "/api/tags/**",                       // 标签服务接口

                                // 用户相关（给C同学测试数据库用）
                                "/api/users/**",
                                "/api/users/*/recommendations/by-tags",
                                "/api/users/*/tags/batch",

                                // 推荐系统完整接口（给D同学）
                                "/recommendations/**",

                                // 数据监控完整接口（给C同学）
                                "/monitor/**",

                                // 消息系统完整接口
                                "/messages/**",

                                // 静态资源和错误页面
                                "/favicon.ico",
                                "/error",
                                "/static/**",
                                "/public/**",

                                // Swagger UI (如果以后引入)
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api-docs/**",

                                // 开发工具
                                "/actuator/**",
                                "/h2-console/**",

                                // 标签服务接口（全部放开，确保C同学可以测试）
                                "/api/tags/health",
                                "/api/tags/test",
                                "/api/tags",
                                "/api/tags/search",
                                "/api/tags/**"
                        ).permitAll()
                        // 其他所有请求需要认证
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .logout(logout -> logout.disable())
                // ↓ 关键修复：自定义认证失败处理（返回 JSON 而非重定向）
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(401);
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.getWriter().write(
                                    "{\"success\": false, \"message\": \"请先登录\", \"code\": 401}"
                            );
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}