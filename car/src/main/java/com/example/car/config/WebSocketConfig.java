package com.example.car.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket 配置类
 *
 * 问题修复（B5）：测试环境（"test" profile）下排除此配置。
 * ServerEndpointExporter 需要 Servlet 容器提供的 ServerContainer 支持，
 * 而 mvn test 的 mock 环境中不存在真实 Servlet 容器，
 * 会导致测试报错："No jakarta.websocket.server.ServerContainer available"。
 *
 * 通过 @Profile("!test") 确保此 Bean 仅在非测试环境中创建，
 * 生产行为不受任何影响。
 */
@Configuration
@Profile("!test")
public class WebSocketConfig {

    /**
     * 注入 ServerEndpointExporter，
     * 自动注册所有使用 @ServerEndpoint 注解的 WebSocket 端点 Bean
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
