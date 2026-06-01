package com.example.car.controller.vehicle;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.car.common.result.Result;
import com.example.car.entity.VehicleMaintenance;
import com.example.car.entity.dto.VehicleMaintenanceAddDTO;
import com.example.car.entity.dto.VehicleMaintenanceQueryDTO;
import com.example.car.entity.dto.VehicleMaintenanceUpdateDTO;
import com.example.car.entity.vo.VehicleMaintenanceVO;
import com.example.car.service.VehicleMaintenanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 车辆维护控制器
 * 所有接口仅 ADMIN 角色可访问
 */
@Tag(name = "车辆维护", description = "车辆维护记录管理相关接口（保养/维修/年检/保险）")
@RestController
@RequestMapping("/api/vehicle/maintenance")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class VehicleMaintenanceController {

    private final VehicleMaintenanceService vehicleMaintenanceService;

    @Operation(summary = "分页查询维护记录",
               description = "maintenanceType: 1-保养 2-维修 3-年检 4-保险，不传则查全部")
    @GetMapping("/list")
    public Result<Page<VehicleMaintenanceVO>> pageQuery(VehicleMaintenanceQueryDTO queryDTO) {
        Page<VehicleMaintenanceVO> page = vehicleMaintenanceService.pageQuery(queryDTO);
        return Result.success(page);
    }

    @Operation(summary = "根据车牌号查询最新维护详情")
    @GetMapping("/vehicle/{vehicleNo}")
    public Result<VehicleMaintenanceVO> getDetailByVehicleNo(
            @Parameter(description = "车辆车牌号") @PathVariable String vehicleNo) {
        VehicleMaintenanceVO vo = vehicleMaintenanceService.getMaintenanceByVehicleNo(vehicleNo);
        return Result.success(vo);
    }

    @Operation(summary = "新增维护记录")
    @PostMapping
    public Result<Void> add(@Validated @RequestBody VehicleMaintenanceAddDTO addDTO) {
        VehicleMaintenance vehicleMaintenance = new VehicleMaintenance();
        BeanUtils.copyProperties(addDTO, vehicleMaintenance);
        vehicleMaintenance.setId(null);
        vehicleMaintenance.setCreateTime(null);
        vehicleMaintenance.setUpdateTime(null);
        boolean success = vehicleMaintenanceService.addMaintenance(vehicleMaintenance);
        return success ? Result.success() : Result.error("新增维护记录失败");
    }

    @Operation(summary = "更新维护记录")
    @PutMapping
    public Result<Void> update(@Validated @RequestBody VehicleMaintenanceUpdateDTO updateDTO) {
        VehicleMaintenance vehicleMaintenance = new VehicleMaintenance();
        BeanUtils.copyProperties(updateDTO, vehicleMaintenance);
        vehicleMaintenance.setCreateTime(null);
        vehicleMaintenance.setUpdateTime(null);
        boolean success = vehicleMaintenanceService.updateMaintenance(vehicleMaintenance);
        return success ? Result.success() : Result.error("更新维护记录失败");
    }

    @Operation(summary = "按 ID 删除单条维护记录（前端一车一档使用）")
    @DeleteMapping("/{id}")
    public Result<Void> deleteById(
            @Parameter(description = "维护记录ID") @PathVariable Long id) {
        boolean success = vehicleMaintenanceService.deleteMaintenanceById(id);
        return success ? Result.success() : Result.error("删除维护记录失败");
    }

    @Operation(summary = "按车牌号删除该车所有维护记录")
    @DeleteMapping("/vehicle/{vehicleNo}")
    public Result<Void> deleteByVehicleNo(
            @Parameter(description = "车辆车牌号") @PathVariable String vehicleNo) {
        boolean success = vehicleMaintenanceService.deleteMaintenance(vehicleNo);
        return success ? Result.success() : Result.error("删除维护记录失败");
    }
}
