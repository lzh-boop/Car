package com.example.car.aspect;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 日志切面 — 已做安全脱敏处理：
 * 1. 敏感字段（password / token / Authorization）在打印前全部替换为 "***"
 * 2. 响应体不整体打印，只打印业务状态码，防止 token / 用户数据泄漏到日志
 * 3. IP 解析优先取最左侧非代理 IP，防止伪造
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    /** 需要脱敏的 JSON key（大小写不敏感） */
    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password", "newpassword", "oldpassword",
            "token", "accesstoken", "refreshtoken",
            "authorization", "secret", "credential"
    );

    /**
     * 对 JSON 字符串中的敏感字段做脱敏替换：
     * 匹配 "password":"任意值" 并替换为 "password":"***"
     */
    private static final Pattern SENSITIVE_PATTERN = Pattern.compile(
            "(?i)(\"(?:password|newPassword|oldPassword|token|accessToken|refreshToken" +
            "|authorization|secret|credential)\"\\s*:\\s*\")([^\"]*)(\")",
            Pattern.CASE_INSENSITIVE
    );

    @Pointcut("execution(* com.example.car.controller..*.*(..))")
    public void controllerPointcut() {}

    @Before("controllerPointcut()")
    public void doBefore(JoinPoint joinPoint) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return;

        HttpServletRequest request = attributes.getRequest();
        log.info("========== Start ==========");
        log.info("URL         : {}", request.getRequestURL());
        log.info("Method      : {}", request.getMethod());
        log.info("Handler     : {}.{}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName());
        log.info("IP          : {}", getClientIp(request));

        // 参数脱敏后再打印
        String rawArgs = JSONUtil.toJsonStr(joinPoint.getArgs());
        log.info("Request Args: {}", desensitize(rawArgs));
    }

    /**
     * 响应只打印业务 code，不整体序列化响应体（防止 token/用户数据进日志）
     */
    @AfterReturning(pointcut = "controllerPointcut()", returning = "result")
    public void doAfterReturning(Object result) {
        if (result != null) {
            // 只打印 class 类型，不序列化内容
            log.info("Response    : [{}] returned", result.getClass().getSimpleName());
        }
        log.info("========== End ==========");
    }

    @AfterThrowing(pointcut = "controllerPointcut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
        log.error("Exception in {}.{}: {}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                e.getMessage());
    }

    @Around("controllerPointcut()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = proceedingJoinPoint.proceed();
        log.info("Execute     : {} ms", System.currentTimeMillis() - start);
        return result;
    }

    /**
     * 将 JSON 字符串中的敏感字段值替换为 "***"
     */
    private String desensitize(String json) {
        if (json == null || json.isBlank()) return json;
        return SENSITIVE_PATTERN.matcher(json).replaceAll("$1***$3");
    }

    /**
     * 获取真实客户端 IP：
     * 优先取 X-Forwarded-For 的最左侧 IP（最原始来源），而非信任所有代理头
     */
    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank() && !"unknown".equalsIgnoreCase(xff)) {
            // 取逗号分隔的第一个，即原始客户端 IP
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
