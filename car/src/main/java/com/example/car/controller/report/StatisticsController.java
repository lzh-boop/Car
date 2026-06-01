package com.example.car.controller.report;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.car.common.constant.Constants;
import com.example.car.common.result.Result;
import com.example.car.entity.DriverInfo;
import com.example.car.entity.VehicleDispatch;
import com.example.car.entity.VehicleInfo;
import com.example.car.entity.VehicleReturn;
import com.example.car.mapper.DriverInfoMapper;
import com.example.car.mapper.VehicleDispatchMapper;
import com.example.car.mapper.VehicleInfoMapper;
import com.example.car.mapper.VehicleReturnMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计报表控制器
 * Bug 修复（B8）：将所有 TODO 桩接口替换为真实的数据库聚合查询。
 * 所有接口仅 ADMIN 角色可访问
 */
@Tag(name = "统计报表", description = "统计分析报表相关接口")
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class StatisticsController {

    private final VehicleInfoMapper vehicleInfoMapper;
    private final VehicleDispatchMapper vehicleDispatchMapper;
    private final VehicleReturnMapper vehicleReturnMapper;
    private final DriverInfoMapper driverInfoMapper;

    /**
     * 车辆使用率统计
     * 返回各状态车辆数量及使用率（在用数 / 总数）
     */
    @Operation(summary = "车辆使用率统计")
    @GetMapping("/vehicle-usage")
    public Result<Map<String, Object>> vehicleUsage() {
        long total       = vehicleInfoMapper.selectCount(null);
        long free        = vehicleInfoMapper.selectCount(
                new LambdaQueryWrapper<VehicleInfo>().eq(VehicleInfo::getStatus, Constants.VehicleStatus.FREE));
        long inUse       = vehicleInfoMapper.selectCount(
                new LambdaQueryWrapper<VehicleInfo>().eq(VehicleInfo::getStatus, Constants.VehicleStatus.IN_USE));
        long maintenance = vehicleInfoMapper.selectCount(
                new LambdaQueryWrapper<VehicleInfo>().eq(VehicleInfo::getStatus, Constants.VehicleStatus.MAINTENANCE));
        long scrapped    = vehicleInfoMapper.selectCount(
                new LambdaQueryWrapper<VehicleInfo>().eq(VehicleInfo::getStatus, Constants.VehicleStatus.SCRAPPED));

        // 使用率 = 在用数 / 总数，保留两位小数（百分比）
        double usageRate = total > 0 ? Math.round((double) inUse / total * 10000d) / 100d : 0d;

        Map<String, Object> data = new HashMap<>();
        data.put("total",       total);       // 车辆总数
        data.put("free",        free);        // 空闲数
        data.put("inUse",       inUse);       // 在用数
        data.put("maintenance", maintenance); // 维修数
        data.put("scrapped",    scrapped);    // 报废数
        data.put("usageRate",   usageRate);   // 使用率（%），例如 33.33
        return Result.success(data);
    }

    /**
     * 里程统计
     * 返回所有已完成还车记录的总里程、行程次数、平均每次里程
     */
    @Operation(summary = "里程统计")
    @GetMapping("/mileage")
    public Result<Map<String, Object>> mileage() {
        // 查询所有已还车记录
        List<VehicleReturn> completed = vehicleReturnMapper.selectList(
                new LambdaQueryWrapper<VehicleReturn>().eq(VehicleReturn::getReturnStatus, 1));

        long totalMileage = 0;
        int  tripCount    = 0;
        for (VehicleReturn r : completed) {
            // 仅统计还车里程大于出车里程的有效记录
            if (r.getMileageAfter() != null && r.getMileageBefore() != null
                    && r.getMileageAfter() > r.getMileageBefore()) {
                totalMileage += (r.getMileageAfter() - r.getMileageBefore());
                tripCount++;
            }
        }
        // 平均每次里程，保留一位小数
        double avgMileage = tripCount > 0
                ? Math.round((double) totalMileage / tripCount * 10d) / 10d
                : 0d;

        Map<String, Object> data = new HashMap<>();
        data.put("totalMileage",      totalMileage); // 总里程（公里）
        data.put("completedTrips",    tripCount);    // 有效行程次数
        data.put("avgMileagePerTrip", avgMileage);   // 平均每次里程
        return Result.success(data);
    }

    /**
     * 油耗/油量状态统计
     * 返回已还车记录中各油量状态（充足/偏少/需加油）的分布情况
     */
    @Operation(summary = "油耗统计")
    @GetMapping("/fuel")
    public Result<Map<String, Object>> fuel() {
        // 查询所有已还车记录
        List<VehicleReturn> completed = vehicleReturnMapper.selectList(
                new LambdaQueryWrapper<VehicleReturn>().eq(VehicleReturn::getReturnStatus, 1));

        long sufficient  = completed.stream().filter(r -> Integer.valueOf(0).equals(r.getFuelLevel())).count(); // 油量充足
        long low         = completed.stream().filter(r -> Integer.valueOf(1).equals(r.getFuelLevel())).count(); // 油量偏少
        long needsRefuel = completed.stream().filter(r -> Integer.valueOf(2).equals(r.getFuelLevel())).count(); // 需要加油

        Map<String, Object> data = new HashMap<>();
        data.put("total",        completed.size()); // 总还车次数
        data.put("sufficient",   sufficient);       // 油量充足次数
        data.put("low",          low);              // 油量偏少次数
        data.put("needsRefuel",  needsRefuel);      // 需加油次数
        return Result.success(data);
    }

    /**
     * 驾驶员统计
     * 返回驾驶员总数、在职数、进行中调度数、已完成调度数
     */
    @Operation(summary = "驾驶员统计")
    @GetMapping("/driver")
    public Result<Map<String, Object>> driver() {
        long totalDrivers  = driverInfoMapper.selectCount(null);
        long activeDrivers = driverInfoMapper.selectCount(
                new LambdaQueryWrapper<DriverInfo>().eq(DriverInfo::getStatus, 1)); // 状态：1-正常

        // 进行中调度：待出车 + 行驶中
        long activeDispatches = vehicleDispatchMapper.selectCount(
                new LambdaQueryWrapper<VehicleDispatch>()
                        .in(VehicleDispatch::getDispatchStatus,
                                Constants.DispatchStatus.PENDING,
                                Constants.DispatchStatus.IN_PROGRESS));
        // 已完成调度
        long completedDispatches = vehicleDispatchMapper.selectCount(
                new LambdaQueryWrapper<VehicleDispatch>()
                        .eq(VehicleDispatch::getDispatchStatus, Constants.DispatchStatus.COMPLETED));

        Map<String, Object> data = new HashMap<>();
        data.put("totalDrivers",        totalDrivers);        // 驾驶员总数
        data.put("activeDrivers",       activeDrivers);       // 在职驾驶员数
        data.put("activeDispatches",    activeDispatches);    // 进行中调度数
        data.put("completedDispatches", completedDispatches); // 已完成调度数
        return Result.success(data);
    }
}
