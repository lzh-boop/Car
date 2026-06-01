package com.example.car.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.car.common.result.Result;
import com.example.car.entity.VehicleDispatch;
import com.example.car.entity.VehicleInfo;
import com.example.car.entity.VehicleMaintenance;
import com.example.car.entity.dto.VehicleAddDTO;
import com.example.car.entity.dto.VehicleQueryDTO;
import com.example.car.entity.dto.VehicleUpdateDTO;
import com.example.car.entity.vo.VehicleStatsVO;
import com.example.car.entity.vo.VehicleVO;
import com.example.car.mapper.VehicleDispatchMapper;
import com.example.car.mapper.VehicleMaintenanceMapper;
import com.example.car.service.VehicleInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 车辆信息管理控制器
 * 所有接口仅 ADMIN 角色可访问（车辆档案属于管理员功能）
 */
@Tag(name = "车辆管理", description = "车辆信息管理相关接口")
@RestController
@RequestMapping("/api/vehicle")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class VehicleInfoController {

    private final VehicleInfoService vehicleInfoService;
    private final VehicleDispatchMapper vehicleDispatchMapper;
    private final VehicleMaintenanceMapper vehicleMaintenanceMapper;

    /**
     * 分页查询车辆列表
     * 普通用户也可查询（用于申请用车时选择空闲车辆），ADMIN 可查看全部。
     * 方法级 @PreAuthorize 覆盖类级别的 ADMIN 限制。
     */
    @Operation(summary = "分页查询车辆列表", description = "根据条件分页查询车辆信息，所有登录用户可访问")
    @GetMapping("/list")
    @PreAuthorize("isAuthenticated()")
    public Result<Page<VehicleVO>> pageQuery(VehicleQueryDTO queryDTO) {
        Page<VehicleVO> page = vehicleInfoService.pageQuery(queryDTO);
        return Result.success(page);
    }

    @Operation(summary = "根据车牌号查询车辆详情", description = "根据车牌号查询车辆详细信息")
    @GetMapping("/{vehicleNo}")
    public Result<VehicleVO> getVehicleByVehicleNo(
            @Parameter(description = "车辆车牌号", required = true)
            @PathVariable String vehicleNo) {
        VehicleVO vehicleVO = vehicleInfoService.getVehicleByVehicleNo(vehicleNo);
        return Result.success(vehicleVO);
    }

    @Operation(summary = "新增车辆", description = "添加新的车辆信息")
    @PostMapping
    public Result<Void> add(@Validated @RequestBody VehicleAddDTO addDTO) {
        VehicleInfo vehicleInfo = new VehicleInfo();
        BeanUtils.copyProperties(addDTO, vehicleInfo);
        vehicleInfo.setId(null);
        vehicleInfo.setCreateTime(null);
        vehicleInfo.setUpdateTime(null);
        boolean success = vehicleInfoService.addVehicle(vehicleInfo);
        return success ? Result.success() : Result.error("新增车辆失败");
    }

    @Operation(summary = "更新车辆信息", description = "修改车辆信息")
    @PutMapping
    public Result<Void> update(@Validated @RequestBody VehicleUpdateDTO updateDTO) {
        VehicleInfo vehicleInfo = new VehicleInfo();
        BeanUtils.copyProperties(updateDTO, vehicleInfo);
        vehicleInfo.setCreateTime(null);
        vehicleInfo.setUpdateTime(null);
        boolean success = vehicleInfoService.updateVehicle(vehicleInfo);
        return success ? Result.success() : Result.error("更新车辆失败");
    }

    @Operation(summary = "根据车牌号删除车辆", description = "根据车牌号删除指定车辆信息")
    @DeleteMapping("/{vehicleNo}")
    public Result<Void> delete(
            @Parameter(description = "车辆车牌号") @PathVariable String vehicleNo) {
        boolean success = vehicleInfoService.deleteVehicle(vehicleNo);
        return success ? Result.success() : Result.error("根据车牌号删除车辆失败");
    }

    // ─── 统计报表接口 ────────────────────────────────────────────────────────────

    @Operation(summary = "车辆运维统计报表",
               description = "返回每辆车的调度次数、保养/维修/保险费用汇总，供统计报表页使用")
    @GetMapping("/stats")
    public Result<List<VehicleStatsVO>> vehicleStats() {
        // 1. 查询所有车辆
        List<VehicleInfo> vehicles = vehicleInfoService.list();

        // 2. 查询所有调度记录，按 vehicleNo 分组统计次数
        List<VehicleDispatch> allDispatches = vehicleDispatchMapper.selectList(null);
        Map<String, Long> dispatchCountMap = allDispatches.stream()
                .filter(d -> d.getVehicleNo() != null)
                .collect(Collectors.groupingBy(VehicleDispatch::getVehicleNo, Collectors.counting()));

        // 3. 查询所有维护记录，按 vehicleNo + maintenanceType 分组统计费用
        List<VehicleMaintenance> allMaintenance = vehicleMaintenanceMapper.selectList(null);
        // 保养(1)费用
        Map<String, BigDecimal> maintainCostMap = allMaintenance.stream()
                .filter(m -> m.getVehicleNo() != null && Integer.valueOf(1).equals(m.getMaintenanceType()))
                .collect(Collectors.groupingBy(VehicleMaintenance::getVehicleNo,
                        Collectors.reducing(BigDecimal.ZERO,
                                m -> m.getMaintenanceCost() != null ? m.getMaintenanceCost() : BigDecimal.ZERO,
                                BigDecimal::add)));
        // 维修(2)费用
        Map<String, BigDecimal> repairCostMap = allMaintenance.stream()
                .filter(m -> m.getVehicleNo() != null && Integer.valueOf(2).equals(m.getMaintenanceType()))
                .collect(Collectors.groupingBy(VehicleMaintenance::getVehicleNo,
                        Collectors.reducing(BigDecimal.ZERO,
                                m -> m.getMaintenanceCost() != null ? m.getMaintenanceCost() : BigDecimal.ZERO,
                                BigDecimal::add)));
        // 保险(4)费用
        Map<String, BigDecimal> insuranceCostMap = allMaintenance.stream()
                .filter(m -> m.getVehicleNo() != null && Integer.valueOf(4).equals(m.getMaintenanceType()))
                .collect(Collectors.groupingBy(VehicleMaintenance::getVehicleNo,
                        Collectors.reducing(BigDecimal.ZERO,
                                m -> m.getMaintenanceCost() != null ? m.getMaintenanceCost() : BigDecimal.ZERO,
                                BigDecimal::add)));

        // 4. 组装 VO
        List<VehicleStatsVO> result = vehicles.stream().map(v -> {
            VehicleStatsVO vo = new VehicleStatsVO();
            vo.setVehicleNo(v.getVehicleNo());
            vo.setVehicleType(v.getVehicleType());
            vo.setBrand(v.getBrand());
            vo.setStatus(v.getStatus());
            vo.setMileage(v.getMileage());
            vo.setDispatchCount(dispatchCountMap.getOrDefault(v.getVehicleNo(), 0L));
            vo.setMaintainCost(maintainCostMap.getOrDefault(v.getVehicleNo(), BigDecimal.ZERO));
            vo.setRepairCost(repairCostMap.getOrDefault(v.getVehicleNo(), BigDecimal.ZERO));
            vo.setInsuranceCost(insuranceCostMap.getOrDefault(v.getVehicleNo(), BigDecimal.ZERO));
            // 状态描述
            vo.setStatusDesc(switch (v.getStatus() == null ? -1 : v.getStatus()) {
                case 0 -> "空闲";
                case 1 -> "在用";
                case 2 -> "维修";
                case 3 -> "报废";
                default -> "未知";
            });
            return vo;
        }).collect(Collectors.toList());

        return Result.success(result);
    }
}
