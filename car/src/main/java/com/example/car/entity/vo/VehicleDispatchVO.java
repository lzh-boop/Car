package com.example.car.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 车辆调度VO
 */
@Data
@Schema(description = "车辆调度信息")
public class VehicleDispatchVO {
    
    @Schema(name = "调度ID", description = "调度ID")
    private Long id;
    
    @Schema(name = "关联申请单ID", description = "关联用车申请单ID")
    private Long applyId;
    
    @Schema(name = "调度单号", description = "调度单号")
    private String dispatchNo;
    
    @Schema(name = "车辆ID", description = "车辆ID")
    private Long vehicleId;
    
    @Schema(name = "车牌号", description = "车牌号")
    private String vehicleNo;

    @Schema(name = "车辆类型", description = "车辆类型")
    private String vehicleType;

    @Schema(name = "车辆品牌", description = "车辆品牌")
    private String brand;

    @Schema(name = "驾驶员ID", description = "驾驶员ID")
    private Long driverId;
    
    @Schema(name = "驾驶员姓名", description = "驾驶员姓名")
    private String driverName;
    
    @Schema(name = "出发地", description = "出发地")
    private String startLocation;
    
    @Schema(name = "目的地", description = "目的地")
    private String endLocation;
    
    @Schema(name = "计划开始时间", description = "计划开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planStartTime;

    @Schema(name = "计划结束时间", description = "计划结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planEndTime;

    @Schema(name = "实际开始时间", description = "实际开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualStartTime;

    @Schema(name = "实际结束时间", description = "实际结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualEndTime;
    
    @Schema(name = "调度状态", description = "调度状态：0-待出车，1-行驶中，2-已完成，3-已取消")
    private Integer dispatchStatus;
    
    @Schema(name = "状态描述", description = "调度状态描述")
    private String dispatchStatusDesc;
    
    @Schema(name = "创建时间", description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(name = "更新时间", description = "更新时间")
    private LocalDateTime updateTime;
}
