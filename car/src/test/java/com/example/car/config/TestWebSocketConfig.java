package com.example.car.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * 测试专用 WebSocket 配置类
 *
 * 当激活 "test" profile 时，正式的 WebSocketConfig（@Profile("!test")）不会加载。
 * 本类提供一个空实现，返回 null，使 Spring 上下文可以正常启动，
 * 同时跳过需要真实 Servlet 容器才能完成的 @ServerEndpoint 注册。
 *
 * 注意：@Bean 方法返回 null 在 Spring 中是合法的，
 * 表示注册该 Bean 定义但不向容器注入实例。
 */
@TestConfiguration
@Profile("test")
public class TestWebSocketConfig {

    @Bean
    @Primary
    public ServerEndpointExporter serverEndpointExporter() {
        // 测试环境无 Servlet 容器，返回 null 跳过 @ServerEndpoint 注册
        return null;
    }
}
