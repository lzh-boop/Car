package com.example.car.common.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

/**
 * 高德地图逆地理编码工具类
 * 将北斗/GPS 经纬度坐标转换为详细中文地址
 * 使用高德 Web 服务 API（后端调用，Key 类型：Web服务）
 */
@Slf4j
@Component
public class AmapGeocodingUtil {

    /** 高德 Web 服务 Key（Web服务类型） */
    @Value("${amap.web-service-key}")
    private String webServiceKey;

    /** 高德逆地理编码 REST 接口地址 */
    @Value("${amap.geocoding-url}")
    private String geocodingUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 根据经纬度解析详细地址（逆地理编码）
     *
     * @param longitude 经度（GCJ-02 坐标系，北斗/高德通用）
     * @param latitude  纬度
     * @return 详细地址字符串，解析失败时返回 null
     */
    public String resolveAddress(BigDecimal longitude, BigDecimal latitude) {
        if (longitude == null || latitude == null) {
            return null;
        }
        try {
            // 拼接请求 URL
            // extensions=all 返回周边 POI、道路等详细信息
            String url = String.format(
                    "%s?key=%s&location=%s,%s&poitype=&radius=500&extensions=base&batch=false&roadlevel=0",
                    geocodingUrl, webServiceKey,
                    longitude.toPlainString(), latitude.toPlainString()
            );

            log.debug("高德逆地理编码请求: longitude={}, latitude={}", longitude, latitude);

            String response = restTemplate.getForObject(url, String.class);
            if (response == null) {
                log.warn("高德逆地理编码返回空响应");
                return null;
            }

            JSONObject json = JSON.parseObject(response);
            String status = json.getString("status");
            if (!"1".equals(status)) {
                log.warn("高德逆地理编码失败: info={}", json.getString("info"));
                return null;
            }

            JSONObject regeocode = json.getJSONObject("regeocode");
            if (regeocode == null) {
                return null;
            }

            // 优先返回格式化地址（省+市+区+街道+门牌号）
            String formattedAddress = regeocode.getString("formatted_address");
            if (formattedAddress != null && !formattedAddress.isBlank()) {
                log.debug("高德逆地理编码成功: {} -> {}", longitude + "," + latitude, formattedAddress);
                return formattedAddress;
            }

            return null;

        } catch (Exception e) {
            log.error("高德逆地理编码异常: longitude={}, latitude={}, error={}",
                    longitude, latitude, e.getMessage());
            return null;
        }
    }

    /**
     * 根据经纬度（double 类型重载）解析详细地址
     */
    public String resolveAddress(double longitude, double latitude) {
        return resolveAddress(BigDecimal.valueOf(longitude), BigDecimal.valueOf(latitude));
    }
}
