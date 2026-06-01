package com.example.car.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS 跨域配置
 *
 * 提供 CorsConfigurationSource Bean，由 SecurityConfig 中的
 * .cors(cors -> cors.configurationSource(...)) 自动集成到 Spring Security 过滤链，
 * 确保 CORS 预检（OPTIONS）请求在 Security 鉴权之前正确处理。
 *
 * 不再使用独立的 CorsFilter Bean，避免与 Spring Security 内部 CORS 处理冲突。
 */
@Configuration
public class CorsConfig {

    /**
     * 允许的前端来源，多个用逗号分隔。
     * 生产环境通过环境变量注入，例如：
     *   CORS_ALLOWED_ORIGINS=https://your-domain.com
     * 本地开发默认允许 localhost 常用端口（Vite 默认 5173，端口被占用时会递增）。
     */
    @Value("${cors.allowed-origins:http://localhost:5173,http://localhost:5174,http://localhost:5175,http://localhost:5176,http://localhost:3000,http://127.0.0.1:5173,http://127.0.0.1:5175,http://47.103.7.135,http://47.103.7.135:80,http://47.103.7.135:8080}")
    private String allowedOriginsConfig;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 白名单来源
        List<String> origins = Arrays.stream(allowedOriginsConfig.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
        config.setAllowedOrigins(origins);

        // 允许的请求头
        config.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "Accept",
                "X-Requested-With", "Cache-Control"
        ));

        // 允许的 HTTP 方法
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 允许携带凭证
        config.setAllowCredentials(true);

        // 暴露给前端的响应头
        config.addExposedHeader("Authorization");

        // 预检请求缓存时间（秒）
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
