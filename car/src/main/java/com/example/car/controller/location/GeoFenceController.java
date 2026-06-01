package com.example.car.controller.location;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.car.common.result.Result;
import com.example.car.entity.GeoFence;
import com.example.car.entity.dto.GeoFenceQueryDTO;
import com.example.car.entity.vo.GeoFenceVO;
import com.example.car.service.GeoFenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 电子围栏控制器
 * 所有接口仅 ADMIN 角色可访问
 */
@Tag(name = "电子围栏", description = "电子围栏管理相关接口")
@RestController
@RequestMapping("/api/location/fence")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class GeoFenceController {
    
    private final GeoFenceService geoFenceService;
    
    @Operation(summary = "分页查询围栏列表")
    @GetMapping("/list")
    public Result<Page<GeoFenceVO>> pageQuery(GeoFenceQueryDTO queryDTO) {
        Page<GeoFenceVO> page = geoFenceService.pageQuery(queryDTO);
        return Result.success(page);
    }
    
    @Operation(summary = "查询围栏详情")
    @GetMapping("/{id}")
    public Result<GeoFenceVO> getDetail(
            @Parameter(description = "围栏ID") @PathVariable Long id) {
        GeoFenceVO vo = geoFenceService.getDetailById(id);
        return Result.success(vo);
    }
    
    @Operation(summary = "新增围栏")
    @PostMapping
    public Result<Void> add(@Validated @RequestBody GeoFence geoFence) {
        boolean success = geoFenceService.addFence(geoFence);
        return success ? Result.success() : Result.error("新增围栏失败");
    }
    
    @Operation(summary = "更新围栏")
    @PutMapping
    public Result<Void> update(@Validated @RequestBody GeoFence geoFence) {
        boolean success = geoFenceService.updateFence(geoFence);
        return success ? Result.success() : Result.error("更新围栏失败");
    }
    
    @Operation(summary = "删除围栏")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "围栏ID") @PathVariable Long id) {
        boolean success = geoFenceService.deleteFence(id);
        return success ? Result.success() : Result.error("删除围栏失败");
    }
}
