package com.example.car.controller.location;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.car.common.result.Result;
import com.example.car.entity.dto.BeidouLocationQueryDTO;
import com.example.car.entity.dto.DeviceReportDTO;
import com.example.car.entity.vo.BeidouLocationVO;
import com.example.car.service.BeidouLocationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 北斗实时定位控制器
 * 查询/解析接口仅 ADMIN 可访问；设备上报接口（/report）无需鉴权
 */
@Slf4j
@Tag(name = "北斗实时定位", description = "北斗卫星定位数据管理接口（集成高德逆地理编码）")
@RestController
@RequestMapping("/api/location/beidou")
@RequiredArgsConstructor
public class BeidouLocationController {

    private final BeidouLocationService beidouLocationService;
    private final ObjectMapper objectMapper;

    /** 分页查询北斗定位记录（仅 ADMIN） */
    @Operation(summary = "分页查询北斗定位记录", description = "支持按终端号、车牌号、时间范围查询")
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<BeidouLocationVO>> pageQuery(BeidouLocationQueryDTO queryDTO) {
        return Result.success(beidouLocationService.pageQuery(queryDTO));
    }

    /** 获取车辆最新北斗位置（仅 ADMIN） */
    @Operation(summary = "获取车辆最新北斗位置", description = "返回指定车辆最新一条定位数据（含详细地址）")
    @GetMapping("/latest/{vehicleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<BeidouLocationVO> getLatest(
            @Parameter(description = "车辆ID", required = true)
            @PathVariable Long vehicleId) {
        return Result.success(beidouLocationService.getLatestLocation(vehicleId));
    }

    /**
     * 硬件终端设备上报定位数据（无需鉴权）
     * 接收 ESP32 等北斗硬件设备推送的原始 JSON：
     * { "device_id", "latitude", "longitude", "ns_indicator", "ew_indicator",
     *   "altitude", "speed", "course", "satellites", "date", "time" }
     * 后端自动：
     *   1. 根据 device_id 关联 vehicle_info 表，补充 vehicleId / vehicleNo
     *   2. 解析 NMEA 格式的日期时间（UTC -> 北京时间）
     *   3. 存库后异步调用高德逆地理编码填充详细地址
     */
    @Operation(summary = "硬件终端上报北斗定位数据",
               description = "接收北斗硬件设备（如 esp32_sim7670x）上报的原始 JSON 定位数据，自动解析存库并触发逆地理编码")
    @PostMapping(value = "/device/report",
                 consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE, "text/plain;charset=UTF-8"})
    public Result<Void> deviceReport(@RequestBody String rawBody) {
        log.info("收到设备上报原始数据: {}", rawBody);
        try {
            DeviceReportDTO dto = objectMapper.readValue(rawBody, DeviceReportDTO.class);
            log.info("解析后 DTO: device_id={}, lat={}, lng={}", dto.getDeviceId(), dto.getLatitude(), dto.getLongitude());
            boolean success = beidouLocationService.saveDeviceReport(dto);
            return success ? Result.success() : Result.error("设备定位数据上报失败");
        } catch (Exception e) {
            log.error("设备上报数据解析失败", e);
            return Result.error("数据格式错误: " + e.getMessage());
        }
    }

    /**
     * 根据经纬度解析详细地址（前端按需调用）
     * 调用高德 Web 服务 API 逆地理编码，返回省市区街道门牌号等完整地址
     * 若传入 id，同时将解析结果回写到对应定位记录的 address 字段
     */
    @Operation(summary = "经纬度解析为详细地址（仅 ADMIN）",
               description = "调用高德逆地理编码，将北斗经纬度转换为中文详细地址（省市区街道）；传入id时同步更新数据库")
    @GetMapping("/resolve-address")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<String> resolveAddress(
            @Parameter(description = "定位记录ID（可选，传入则同步更新数据库）")
            @RequestParam(required = false) Long id,
            @Parameter(description = "经度", required = true, example = "116.397428")
            @RequestParam Double longitude,
            @Parameter(description = "纬度", required = true, example = "39.90923")
            @RequestParam Double latitude) {
        String address = beidouLocationService.resolveAddress(id, longitude, latitude);
        return Result.success(address);
    }
}
