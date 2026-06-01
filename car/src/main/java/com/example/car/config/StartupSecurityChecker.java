package com.example.car.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 启动安全自检组件
 *
 * 风险说明（B10）：application.yml 的默认值仍是真实可用的凭据。
 * 若运维人员未配置环境变量就部署到生产环境，服务将以公开已知的密钥启动
 * （数据库密码 "123456"、仓库默认 JWT Secret、Redis 密码等），存在高安全风险。
 *
 * 本组件在 Spring 上下文完全启动后（ApplicationReadyEvent）自动执行检测：
 *  - 若发现任何敏感配置仍为默认值 → 打印 WARN 级别告警日志
 *  - 若当前激活的是 "prod" 生产环境 Profile 且存在默认凭据 → 抛出异常强制终止启动，
 *    防止以默认密钥上线生产环境
 *
 * 测试环境（"test" profile）下不执行检测，避免 mvn test 产生误报。
 */
@Slf4j
@Component
@Profile("!test")
public class StartupSecurityChecker {

    // 仓库占位符常量：与 application.yml 中的占位符一致；
    // 检测到这些值说明运维未通过环境变量或 application-local.yml 注入真实配置
    private static final String DEFAULT_DB_PASSWORD    = "CHANGE_ME_DB_PASSWORD";
    private static final String DEFAULT_JWT_SECRET     = "CHANGE_ME_JWT_SECRET_USE_BASE64_ENCODED_32_BYTE_RANDOM_STRING";
    private static final String DEFAULT_AMAP_KEY       = "CHANGE_ME_AMAP_KEY";

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${amap.web-service-key:}")
    private String amapKey;

    /** 当前激活的 Spring Profiles 列表 */
    @Value("#{T(java.util.Arrays).asList('${spring.profiles.active:}'.split(','))}")
    private java.util.List<String> activeProfiles;

    /**
     * 应用完全启动后执行安全自检。
     * 生产环境（prod profile）下如有默认凭据则直接抛异常终止进程。
     */
    @EventListener(ApplicationReadyEvent.class)
    public void check() {
        boolean isProd  = activeProfiles.contains("prod");
        boolean hasRisk = false;

        if (DEFAULT_DB_PASSWORD.equals(dbPassword)) {
            log.warn("[安全告警] 数据库密码仍为占位符 '{}'，请通过环境变量 DB_PASSWORD 或 application-local.yml 注入真实密码！",
                    DEFAULT_DB_PASSWORD);
            hasRisk = true;
        }
        if (DEFAULT_JWT_SECRET.equals(jwtSecret)) {
            log.warn("[安全告警] JWT Secret 仍为占位符，请通过环境变量 JWT_SECRET 设置强随机密钥！" +
                    "推荐生成方式：openssl rand -base64 32");
            hasRisk = true;
        }
        if (isProd && (redisPassword == null || redisPassword.isEmpty())) {
            log.warn("[安全告警] 生产环境 Redis 未设置密码！请通过环境变量 REDIS_PASSWORD 注入！");
            hasRisk = true;
        }
        if (DEFAULT_AMAP_KEY.equals(amapKey)) {
            log.warn("[安全告警] 高德地图 API Key 仍为占位符，请到 https://console.amap.com 申请并通过环境变量 AMAP_KEY 注入！");
            hasRisk = true;
        }

        if (isProd && hasRisk) {
            log.error("============================================================");
            log.error("[安全告警] 生产环境检测到默认凭据！强烈建议配置环境变量：");
            log.error("  DB_PASSWORD、JWT_SECRET、REDIS_PASSWORD、AMAP_KEY");
            log.error("============================================================");
        }

        if (!hasRisk) {
            log.info("[安全自检] 启动安全检测通过，未发现默认凭据。");
        }
    }
}
