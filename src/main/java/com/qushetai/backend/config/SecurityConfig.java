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

                                // 消息系统测试
                                "/messages/test",

                                // 管理员公开测试
                                "/admin/public-test",

                                // 活动相关测试
                                "/activities",
                                "/activities/**",
                                "/activities/hello",
                                "/activities/test",
                                "/activities/health",
                                "/activities/public/**",

                                // 用户相关（给C同学测试数据库用）
                                "/api/users/**",

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

                                // 标签服务接口（新增，不影响其他测试）
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