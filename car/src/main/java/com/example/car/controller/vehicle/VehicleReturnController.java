package com.example.car.controller.vehicle;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.car.common.result.Result;
import com.example.car.entity.dto.VehicleReturnDoDTO;
import com.example.car.entity.dto.VehicleReturnQueryDTO;
import com.example.car.entity.vo.VehicleReturnVO;
import com.example.car.service.VehicleReturnService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 还车管理控制器
 *
 * 安全改进：还车接口（doReturn）改为接收 VehicleReturnDoDTO 而非完整实体，
 * 服务端关键关联字段（vehicleId、vehicleNo、dispatchId、dispatchNo、
 * driverId、driverName、returnStatus）从数据库中加载，前端无法篡改关联关系。
 */
@Tag(name = "还车管理", description = "还车记录相关接口")
@RestController
@RequestMapping("/api/return")
@RequiredArgsConstructor
public class VehicleReturnController {

    private final VehicleReturnService vehicleReturnService;

    @Operation(summary = "分页查询还车记录")
    @GetMapping("/list")
    public Result<Page<VehicleReturnVO>> list(VehicleReturnQueryDTO queryDTO) {
        return Result.success(vehicleReturnService.pageQuery(queryDTO));
    }

    @Operation(summary = "查询还车详情")
    @GetMapping("/{id}")
    public Result<VehicleReturnVO> detail(@PathVariable Long id) {
        return Result.success(vehicleReturnService.getDetailById(id));
    }

    /**
     * 执行还车操作
     * 前端仅提供：mileageAfter（实际里程）、fuelLevel（油量状态）、
     * vehicleCondition（车辆状况）、remark（备注）。
     * 其他字段（车辆编号、调度单号等）均从数据库已有记录加载，防止越权篡改。
     */
    @Operation(summary = "执行还车", description = "填写还车信息，将车辆状态恢复为空闲")
    @PutMapping("/do")
    public Result<Void> doReturn(@Validated @RequestBody VehicleReturnDoDTO dto) {
        boolean ok = vehicleReturnService.doReturn(dto);
        return ok ? Result.success() : Result.error("还车操作失败");
    }

    @Operation(summary = "删除还车记录")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean ok = vehicleReturnService.deleteReturn(id);
        return ok ? Result.success() : Result.error("删除失败");
    }
}
