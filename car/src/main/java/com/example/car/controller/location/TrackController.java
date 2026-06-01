package com.example.car.controller.location;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.car.common.exception.BusinessException;
import com.example.car.common.result.Result;
import com.example.car.entity.dto.BeidouLocationQueryDTO;
import com.example.car.entity.vo.BeidouLocationVO;
import com.example.car.service.BeidouLocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 北斗轨迹查询控制器
 * 安全修复：
 * 1. pageSize 加上限（分页接口 ≤500，回放接口固定 ≤5000）
 * 2. 时间范围限制（最多 7 天），防止全表扫描打库
 * 3. 仅 ADMIN 角色可访问轨迹数据
 */
@Tag(name = "北斗轨迹查询", description = "基于北斗定位的车辆历史轨迹查询与回放接口")
@RestController
@RequestMapping("/api/location/track")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class TrackController {

    private final BeidouLocationService beidouLocationService;

    /** 分页接口单次最大返回条数 */
    private static final int MAX_PAGE_SIZE = 500;

    /** 回放/今日轨迹接口单次最大返回条数 */
    private static final int MAX_PLAYBACK_SIZE = 5000;

    /** 时间范围最大天数（超出则拒绝，避免全表扫描） */
    private static final long MAX_RANGE_DAYS = 7;

    @Operation(summary = "查询历史轨迹", description = "根据车辆ID和时间范围查询北斗历史轨迹点")
    @GetMapping("/history")
    public Result<Page<BeidouLocationVO>> getHistory(
            @Parameter(description = "车辆ID", required = true)
            @RequestParam Long vehicleId,
            @Parameter(description = "开始时间（yyyy-MM-dd HH:mm:ss）", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间（yyyy-MM-dd HH:mm:ss）", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量（最大 500）") @RequestParam(defaultValue = "100") Integer pageSize) {

        validateTimeRange(startTime, endTime);

        // 强制限制 pageSize
        int safePageSize = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);

        BeidouLocationQueryDTO queryDTO = new BeidouLocationQueryDTO();
        queryDTO.setVehicleId(vehicleId);
        queryDTO.setStartTime(startTime);
        queryDTO.setEndTime(endTime);
        queryDTO.setPageNum(Math.max(pageNum, 1));
        queryDTO.setPageSize(safePageSize);

        return Result.success(beidouLocationService.pageQuery(queryDTO));
    }

    @Operation(summary = "北斗轨迹回放", description = "获取指定时间段内的完整北斗轨迹点，用于地图回放（最多 5000 条，时间跨度 ≤7 天）")
    @GetMapping("/playback")
    public Result<List<BeidouLocationVO>> playback(
            @Parameter(description = "车辆ID", required = true)
            @RequestParam Long vehicleId,
            @Parameter(description = "开始时间", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @Parameter(description = "结束时间", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {

        validateTimeRange(startTime, endTime);

        BeidouLocationQueryDTO queryDTO = new BeidouLocationQueryDTO();
        queryDTO.setVehicleId(vehicleId);
        queryDTO.setStartTime(startTime);
        queryDTO.setEndTime(endTime);
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(MAX_PLAYBACK_SIZE);

        Page<BeidouLocationVO> page = beidouLocationService.pageQuery(queryDTO);
        return Result.success(page.getRecords());
    }

    @Operation(summary = "获取车辆今日北斗轨迹", description = "查询车辆今天的完整北斗行驶轨迹（最多 5000 条）")
    @GetMapping("/today/{vehicleId}")
    public Result<List<BeidouLocationVO>> getTodayTrack(
            @Parameter(description = "车辆ID", required = true)
            @PathVariable Long vehicleId) {

        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay   = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(0);

        BeidouLocationQueryDTO queryDTO = new BeidouLocationQueryDTO();
        queryDTO.setVehicleId(vehicleId);
        queryDTO.setStartTime(startOfDay);
        queryDTO.setEndTime(endOfDay);
        queryDTO.setPageNum(1);
        queryDTO.setPageSize(MAX_PLAYBACK_SIZE);

        Page<BeidouLocationVO> page = beidouLocationService.pageQuery(queryDTO);
        return Result.success(page.getRecords());
    }

    /**
     * 校验时间范围：endTime 不能早于 startTime，跨度不能超过 MAX_RANGE_DAYS
     */
    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (endTime.isBefore(startTime)) {
            throw new BusinessException("结束时间不能早于开始时间");
        }
        long days = java.time.Duration.between(startTime, endTime).toDays();
        if (days > MAX_RANGE_DAYS) {
            throw new BusinessException("时间范围不能超过 " + MAX_RANGE_DAYS + " 天，请缩小查询范围");
        }
    }
}
