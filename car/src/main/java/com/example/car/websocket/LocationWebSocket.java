package com.example.car.websocket;

import com.example.car.common.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GPS 实时位置推送 WebSocket 端点
 *
 * 安全设计：
 *  1. 客户端连接地址：/ws/location/{username}?token=<jwt>
 *  2. 连接建立时（onOpen）立即验证 Token，Token 缺失或无效则强制关闭连接。
 *  3. Token 中解析出的用户名（服务端验证过的，可信）必须与路径参数 {username} 一致，
 *     防止用户 A 订阅用户 B 的推送流（跨用户订阅攻击）。
 *  4. sessionMap 以 Token 中的用户名为键（服务端密码学验证的可信值），
 *     绝不使用路径参数原始值（客户端可伪造任意值）。
 */
@Slf4j
@Component
@ServerEndpoint("/ws/location/{username}")
public class LocationWebSocket implements ApplicationContextAware {

    /**
     * 当前活跃会话表，以 JWT Subject（用户名）为键。
     * 使用服务端密码学验证的值作为键，而非客户端传入的路径参数。
     */
    private static final ConcurrentHashMap<String, Session> sessionMap = new ConcurrentHashMap<>();

    // @ServerEndpoint 实例不由 Spring 管理，通过静态引用在启动时注入 ApplicationContext
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        LocationWebSocket.applicationContext = ctx;
    }

    private static JwtUtil getJwtUtil() {
        return applicationContext.getBean(JwtUtil.class);
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("username") String pathUsername) {
        String token = getQueryParam(session, "token");

        // 拒绝未携带 Token 的连接
        if (token == null || token.isBlank()) {
            log.warn("[WebSocket] 拒绝连接（无 Token），路径用户名={}", pathUsername);
            closeWithError(session, "缺少认证 Token");
            return;
        }

        String tokenUser;
        try {
            JwtUtil jwtUtil = getJwtUtil();
            if (!jwtUtil.validateToken(token)) {
                log.warn("[WebSocket] 拒绝连接（Token 无效或已过期），路径用户名={}", pathUsername);
                closeWithError(session, "Token 无效或已过期");
                return;
            }
            tokenUser = jwtUtil.getUsernameFromToken(token);
            if (tokenUser == null || tokenUser.isBlank()) {
                log.warn("[WebSocket] 拒绝连接（无法解析 Token 主体），路径用户名={}", pathUsername);
                closeWithError(session, "Token 解析失败");
                return;
            }
        } catch (Exception e) {
            log.error("[WebSocket] Token 验证异常，路径用户名={}", pathUsername, e);
            closeWithError(session, "认证异常");
            return;
        }

        // Token 用户名必须与路径参数一致，防止跨用户订阅
        if (!tokenUser.equals(pathUsername)) {
            log.warn("[WebSocket] 拒绝连接（用户名不匹配）：Token 用户='{}' 路径用户='{}'",
                    tokenUser, pathUsername);
            closeWithError(session, "用户身份不匹配");
            return;
        }

        // 以经过密码学验证的 tokenUser 为键，而非可伪造的路径参数
        sessionMap.put(tokenUser, session);
        log.info("[WebSocket] 连接成功：用户={}，当前在线={}", tokenUser, sessionMap.size());
        sendMessage(session, "connected");
    }

    @OnClose
    public void onClose(@PathParam("username") String pathUsername) {
        // 清理已关闭的会话条目（认证失败时条目可能已不存在）
        sessionMap.values().removeIf(s -> !s.isOpen());
        log.info("[WebSocket] 断开连接：路径用户名={}，当前在线={}", pathUsername, sessionMap.size());
    }

    @OnMessage
    public void onMessage(String message, @PathParam("username") String pathUsername) {
        // 此端点为服务端单向推送，丢弃客户端消息；仅记录消息长度，防止日志注入
        log.debug("[WebSocket] 收到客户端消息：用户={}，长度={}", pathUsername, message.length());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("[WebSocket] 会话发生错误", error);
    }

    /**
     * 向指定用户（经过验证的用户名）发送消息
     */
    public static void sendToUser(String username, String message) {
        Session session = sessionMap.get(username);
        if (session != null && session.isOpen()) {
            sendMessage(session, message);
        }
    }

    /**
     * 广播消息给所有在线用户
     */
    public static void sendToAll(String message) {
        sessionMap.values().forEach(s -> sendMessage(s, message));
    }

    private static void sendMessage(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            log.error("[WebSocket] 消息发送失败", e);
        }
    }

    /**
     * 关闭未授权连接，先发送错误提示，再以 VIOLATED_POLICY 原因码关闭会话
     */
    private void closeWithError(Session session, String reason) {
        try {
            session.getBasicRemote().sendText("error: " + reason);
            session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, reason));
        } catch (IOException e) {
            log.error("[WebSocket] 关闭未授权会话失败", e);
        }
    }

    /**
     * 从 WebSocket 会话的查询字符串中提取指定参数值
     */
    private String getQueryParam(Session session, String key) {
        String query = session.getQueryString();
        if (query == null || query.isBlank()) return null;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2 && key.equals(kv[0].trim())) {
                return kv[1].trim();
            }
        }
        return null;
    }
}
