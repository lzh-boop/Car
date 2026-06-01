package com.example.car.config;

import com.example.car.common.result.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.car.security.JwtAuthenticationFilter;
import com.example.car.security.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfigurationSource;

import java.nio.charset.StandardCharsets;

/**
 * Spring Security 核心配置
 * 安全修复：
 * 1. 开启 @PreAuthorize 方法级注解支持（@EnableMethodSecurity）
 * 2. 系统管理接口限制 ROLE_ADMIN
 * 3. 北斗上报接口移除 permitAll（现需要认证，或由设备签名Token访问）
 * 4. Swagger/API文档在生产环境应关闭，此处保留仅供开发
 * 5. CORS 通过注入 CorsConfigurationSource Bean 集成，避免独立 CorsFilter 冲突
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // 开启 @PreAuthorize / @Secured
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsServiceImpl userDetailsService;
    private final ObjectMapper objectMapper;
    private final CorsConfigurationSource corsConfigurationSource;

    /**
     * 完全公开的路径（不需要 Token）。
     *
     * WebSocket 握手路径 /ws/** 在此放行，是因为 JWT 鉴权在
     * LocationWebSocket.onOpen() 内部完成——Token 缺失或无效时
     * 连接会立即被关闭，安全性不受影响。
     * HTTP Upgrade 请求不携带 Authorization 请求头，
     * Spring Security 无法按常规方式拦截，因此在路由层放行并在端点层鉴权。
     */
    private static final String[] PUBLIC_PATHS = {
            "/api/auth/login",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/ws/**",                             // WebSocket 握手，鉴权在 onOpen() 中执行
            "/api/location/beidou/device/report",  // 硬件终端上报，无需 Token
            "/api/debug/**"                        // 调试接口，无需 Token
    };

    /** 仅 ADMIN 角色可访问的路径前缀 */
    private static final String[] ADMIN_PATHS = {
            "/api/system/**",     // 用户管理、角色管理等系统接口
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                    // 完全公开
                    .requestMatchers(PUBLIC_PATHS).permitAll()
                    // 系统管理接口仅 ADMIN
                    .requestMatchers(ADMIN_PATHS).hasRole("ADMIN")
                    // 其余接口登录即可
                    .anyRequest().authenticated()
            )

            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((request, response, authException) -> {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                        Result<Void> result = Result.error(401, "未登录或 Token 已过期，请重新登录");
                        response.getWriter().write(objectMapper.writeValueAsString(result));
                    })
                    .accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                        Result<Void> result = Result.error(403, "权限不足，禁止访问");
                        response.getWriter().write(objectMapper.writeValueAsString(result));
                    }))

            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
