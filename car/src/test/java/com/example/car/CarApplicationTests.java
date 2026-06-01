package com.example.car;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Spring 上下文冒烟测试
 *
 * 激活 "test" profile 的作用：
 *  1. WebSocketConfig（@Profile("!test")）不加载，
 *     避免 ServerEndpointExporter 因缺少真实 Servlet 容器而报错。
 *  2. application-test.yml 中的配置（H2 内存库等）覆盖主配置，
 *     测试无需依赖真实 MySQL 和 Redis 服务。
 *  3. StartupSecurityChecker 不执行安全自检，
 *     避免测试环境触发默认密钥告警。
 */
@SpringBootTest
@ActiveProfiles("test")
class CarApplicationTests {

    @Test
    void contextLoads() {
        // 验证 Spring 上下文能够正常启动，无 Bean 初始化错误
    }
}
