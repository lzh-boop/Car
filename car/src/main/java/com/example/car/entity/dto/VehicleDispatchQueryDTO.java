package com.example.car.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 车辆调度查询DTO
 */
@Data
@Schema(description = "车辆调度查询条件")
public class VehicleDispatchQueryDTO {
    
    @Schema(name = "调度单号", description = "调度单号（模糊查询）", example = "DD202401010001")
    private String dispatchNo;
    
    @Schema(name = "车辆ID", description = "车辆ID", example = "1")
    private Long vehicleId;
    
    @Schema(name = "车牌号", description = "车牌号（模糊查询）", example = "京A12345")
    private String vehicleNo;
    
    @Schema(name = "驾驶员ID", description = "驾驶员ID", example = "1")
    private Long driverId;
    
    @Schema(name = "驾驶员姓名", description = "驾驶员姓名（模糊查询）", example = "张三")
    private String driverName;
    
    @Schema(name = "调度状态", description = "调度状态：0-待出车，1-行驶中，2-已完成，3-已取消", example = "1")
    private Integer dispatchStatus;
    
    @Schema(name = "开始时间", description = "开始时间（查询范围）", example = "2024-01-01 00:00:00")
    private LocalDateTime startTime;
    
    @Schema(name = "结束时间", description = "结束时间（查询范围）", example = "2024-01-31 23:59:59")
    private LocalDateTime endTime;
    
    @Schema(name = "页码", description = "页码", example = "1")
    private Integer pageNum = 1;
    
    @Schema(name = "每页数量", description = "每页数量", example = "10")
    private Integer pageSize = 10;
}
