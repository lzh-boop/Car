package com.example.car.controller.vehicle;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.car.common.result.Result;
import com.example.car.entity.DriverInfo;
import com.example.car.entity.dto.DriverQueryDTO;
import com.example.car.entity.vo.DriverVO;
import com.example.car.service.DriverInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 驾驶员管理控制器
 * 所有接口仅 ADMIN 角色可访问
 */
@Tag(name = "驾驶员管理", description = "驾驶员信息管理相关接口")
@RestController
@RequestMapping("/api/vehicle/driver")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DriverController {
    
    private final DriverInfoService driverInfoService;
    
    @Operation(summary = "分页查询驾驶员列表", description = "根据条件分页查询驾驶员信息")
    @GetMapping("/list")
    public Result<Page<DriverVO>> pageQuery(DriverQueryDTO queryDTO) {
        Page<DriverVO> page = driverInfoService.pageQuery(queryDTO);
        return Result.success(page);
    }
    
    @Operation(summary = "查询驾驶员详情", description = "根据ID查询驾驶员详细信息")
    @GetMapping("/{id}")
    public Result<DriverVO> getDetail(
            @Parameter(description = "驾驶员ID", required = true)
            @PathVariable Long id) {
        DriverVO driverVO = driverInfoService.getDetailById(id);
        return Result.success(driverVO);
    }
    
    @Operation(summary = "新增驾驶员", description = "添加新的驾驶员信息")
    @PostMapping
    public Result<Void> add(@Validated @RequestBody DriverInfo driverInfo) {
        boolean success = driverInfoService.addDriver(driverInfo);
        return success ? Result.success() : Result.error("新增驾驶员失败");
    }
    
    @Operation(summary = "更新驾驶员信息", description = "修改驾驶员信息")
    @PutMapping
    public Result<Void> update(@Validated @RequestBody DriverInfo driverInfo) {
        boolean success = driverInfoService.updateDriver(driverInfo);
        return success ? Result.success() : Result.error("更新驾驶员失败");
    }
    
    @Operation(summary = "删除驾驶员", description = "删除指定驾驶员（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "驾驶员ID", required = true)
            @PathVariable Long id) {
        boolean success = driverInfoService.deleteDriver(id);
        return success ? Result.success() : Result.error("删除驾驶员失败");
    }
}
