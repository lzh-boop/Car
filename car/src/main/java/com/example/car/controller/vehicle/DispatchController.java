package com.example.car.controller.vehicle;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.car.common.result.Result;
import com.example.car.entity.VehicleDispatch;
import com.example.car.entity.dto.VehicleDispatchQueryDTO;
import com.example.car.entity.vo.VehicleDispatchVO;
import com.example.car.service.VehicleDispatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 车辆调度控制器
 * 所有接口仅 ADMIN 角色可访问
 */
@Tag(name = "车辆调度", description = "车辆调度管理相关接口")
@RestController
@RequestMapping("/api/dispatch")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DispatchController {
    
    private final VehicleDispatchService vehicleDispatchService;
    
    @Operation(summary = "分页查询调度列表", description = "根据条件分页查询车辆调度")
    @GetMapping("/list")
    public Result<Page<VehicleDispatchVO>> pageQuery(VehicleDispatchQueryDTO queryDTO) {
        Page<VehicleDispatchVO> page = vehicleDispatchService.pageQuery(queryDTO);
        return Result.success(page);
    }
    
    @Operation(summary = "查询调度详情", description = "根据ID查询调度详细信息")
    @GetMapping("/{id}")
    public Result<VehicleDispatchVO> getDetail(
            @Parameter(description = "调度ID", required = true)
            @PathVariable Long id) {
        VehicleDispatchVO vo = vehicleDispatchService.getDetailById(id);
        return Result.success(vo);
    }
    
    @Operation(summary = "创建调度单", description = "创建新的车辆调度任务")
    @PostMapping("/create")
    public Result<Void> create(@Validated @RequestBody VehicleDispatch vehicleDispatch) {
        boolean success = vehicleDispatchService.createDispatch(vehicleDispatch);
        return success ? Result.success() : Result.error("创建调度单失败");
    }
    
    @Operation(summary = "更新调度单", description = "修改待出车状态的调度单")
    @PutMapping("/update")
    public Result<Void> update(@Validated @RequestBody VehicleDispatch vehicleDispatch) {
        boolean success = vehicleDispatchService.updateDispatch(vehicleDispatch);
        return success ? Result.success() : Result.error("更新调度单失败");
    }
    
    @Operation(summary = "开始出车", description = "将调度单状态改为行驶中")
    @PutMapping("/start/{id}")
    public Result<Void> start(
            @Parameter(description = "调度ID", required = true)
            @PathVariable Long id) {
        boolean success = vehicleDispatchService.startDispatch(id);
        return success ? Result.success() : Result.error("开始出车失败");
    }
    
    @Operation(summary = "完成调度", description = "将调度单状态改为已完成")
    @PutMapping("/complete/{id}")
    public Result<Void> complete(
            @Parameter(description = "调度ID", required = true)
            @PathVariable Long id) {
        boolean success = vehicleDispatchService.completeDispatch(id);
        return success ? Result.success() : Result.error("完成调度失败");
    }
    
    @Operation(summary = "取消调度", description = "将调度单状态改为已取消")
    @PutMapping("/cancel/{id}")
    public Result<Void> cancel(
            @Parameter(description = "调度ID", required = true)
            @PathVariable Long id) {
        boolean success = vehicleDispatchService.cancelDispatch(id);
        return success ? Result.success() : Result.error("取消调度失败");
    }
    
    @Operation(summary = "删除调度", description = "删除指定调度单（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "调度ID", required = true)
            @PathVariable Long id) {
        boolean success = vehicleDispatchService.deleteDispatch(id);
        return success ? Result.success() : Result.error("删除调度失败");
    }
}
