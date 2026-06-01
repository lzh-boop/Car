package com.example.car.controller.vehicle;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.car.common.result.Result;
import com.example.car.entity.TripRecord;
import com.example.car.entity.dto.TripRecordQueryDTO;
import com.example.car.entity.vo.TripRecordVO;
import com.example.car.service.TripRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Tag(name = "行程记录", description = "行程记录管理相关接口")
@RestController
@RequestMapping("/api/trip")
@RequiredArgsConstructor
public class TripRecordController {
    
    private final TripRecordService tripRecordService;
    
    @Operation(summary = "分页查询行程记录")
    @GetMapping("/list")
    public Result<Page<TripRecordVO>> pageQuery(TripRecordQueryDTO queryDTO) {
        Page<TripRecordVO> page = tripRecordService.pageQuery(queryDTO);
        return Result.success(page);
    }
    
    @Operation(summary = "查询行程详情")
    @GetMapping("/{id}")
    public Result<TripRecordVO> getDetail(@Parameter(description = "行程ID") @PathVariable Long id) {
        TripRecordVO vo = tripRecordService.getDetailById(id);
        return Result.success(vo);
    }
    
    @Operation(summary = "新增行程记录")
    @PostMapping
    public Result<Void> add(@Validated @RequestBody TripRecord tripRecord) {
        boolean success = tripRecordService.addTrip(tripRecord);
        return success ? Result.success() : Result.error("新增行程失败");
    }
    
    @Operation(summary = "更新行程记录")
    @PutMapping
    public Result<Void> update(@Validated @RequestBody TripRecord tripRecord) {
        boolean success = tripRecordService.updateTrip(tripRecord);
        return success ? Result.success() : Result.error("更新行程失败");
    }
    
    @Operation(summary = "结束行程")
    @PutMapping("/end/{id}")
    public Result<Void> endTrip(
            @Parameter(description = "行程ID") @PathVariable Long id,
            @RequestParam BigDecimal endMileage,
            @RequestParam(required = false) BigDecimal fuelConsumption) {
        boolean success = tripRecordService.endTrip(id, endMileage, fuelConsumption);
        return success ? Result.success() : Result.error("结束行程失败");
    }
    
    @Operation(summary = "删除行程记录")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(description = "行程ID") @PathVariable Long id) {
        boolean success = tripRecordService.deleteTrip(id);
        return success ? Result.success() : Result.error("删除行程失败");
    }
}
