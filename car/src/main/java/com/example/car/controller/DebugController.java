package com.example.car.controller;

import com.example.car.entity.dto.DeviceReportDTO;
import com.example.car.service.BeidouLocationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 万能调试接口 —— 接收任何 Content-Type、任何格式的请求，全部打印到日志。
 * 部署排查完成后应删除此控制器。
 */
@Slf4j
@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

    private final ObjectMapper objectMapper;
    private final BeidouLocationService beidouLocationService;

    @RequestMapping(value = "/receive", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
    public Map<String, Object> receiveAnything(HttpServletRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", LocalDateTime.now().toString());
        result.put("method", request.getMethod());
        result.put("contentType", request.getContentType());
        result.put("remoteAddr", request.getRemoteAddr());
        result.put("requestURI", request.getRequestURI());
        result.put("queryString", request.getQueryString());

        Map<String, String> headers = new LinkedHashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.put(name, request.getHeader(name));
        }
        result.put("headers", headers);

        String body = "";
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            body = sb.toString();
        } catch (Exception e) {
            body = "读取body失败: " + e.getMessage();
        }
        result.put("body", body);

        log.info("===== 调试接口收到请求 =====");
        log.info("Method: {} | ContentType: {} | RemoteAddr: {}", request.getMethod(), request.getContentType(), request.getRemoteAddr());
        log.info("Headers: {}", headers);
        log.info("Body: {}", body);
        log.info("===========================");

        result.put("status", "OK - 数据已记录到日志");
        return result;
    }

    /**
     * 万能版设备上报：接收任何格式，尝试解析为 DeviceReportDTO 并存库。
     * 成功则存库，失败则只打印日志不报错。
     */
    @RequestMapping(value = "/device-report", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT})
    public Map<String, Object> debugDeviceReport(HttpServletRequest request) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("timestamp", LocalDateTime.now().toString());
        result.put("method", request.getMethod());
        result.put("contentType", request.getContentType());

        String body = "";
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            body = sb.toString();
        } catch (Exception e) {
            body = "读取body失败: " + e.getMessage();
        }
        result.put("rawBody", body);

        log.info("===== 调试设备上报 =====");
        log.info("Method: {} | ContentType: {} | From: {}", request.getMethod(), request.getContentType(), request.getRemoteAddr());
        log.info("原始Body: {}", body);

        String trimmed = body == null ? "" : body.trim();
        if (trimmed.isEmpty()) {
            // GET 或无 body：只记录，不报解析失败
            result.put("parsed", Map.of());
            result.put("savedToDb", false);
            result.put("note", "无请求体，未尝试解析与存库");
            log.info("无请求体，跳过 JSON 解析");
        } else if (!looksLikeJson(trimmed)) {
            // text/plain、NMEA、hello 等：视为已接收，不调用 Jackson
            result.put("parsed", Map.of());
            result.put("savedToDb", false);
            result.put("note", "正文非 JSON 对象/数组，已记录 rawBody，未存库");
            log.info("正文非 JSON（首字节非 {{ [），跳过解析: 长度={}", trimmed.length());
        } else {
            try {
                DeviceReportDTO dto = objectMapper.readValue(trimmed, DeviceReportDTO.class);
                log.info("JSON解析成功: device_id={}, lat={}, lng={}", dto.getDeviceId(), dto.getLatitude(), dto.getLongitude());
                result.put("parsed", Map.of(
                        "device_id", String.valueOf(dto.getDeviceId()),
                        "latitude", String.valueOf(dto.getLatitude()),
                        "longitude", String.valueOf(dto.getLongitude())
                ));

                boolean saved = beidouLocationService.saveDeviceReport(dto);
                result.put("savedToDb", saved);
                log.info("存库结果: {}", saved);
            } catch (Exception e) {
                log.warn("JSON 格式像对象但解析失败: {}", e.getMessage());
                result.put("parseError", e.getMessage());
                result.put("savedToDb", false);
            }
        }

        log.info("========================");
        result.put("status", "OK");
        return result;
    }

    /** 仅当正文像 JSON 对象/数组时才交给 Jackson，避免 plain text 产生误报 */
    private static boolean looksLikeJson(String s) {
        if (s == null || s.isEmpty()) return false;
        char c = s.charAt(0);
        return c == '{' || c == '[';
    }
}
