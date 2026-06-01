package com.example.car.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "行程记录信息")
public class TripRecordVO {
    
    @Schema(name = "行程ID", description = "行程记录ID")
    private Long id;
    
    @Schema(name = "调度ID", description = "关联调度ID")
    private Long dispatchId;
    
    @Schema(name = "车辆ID", description = "车辆ID")
    private Long vehicleId;
    
    @Schema(name = "车牌号", description = "车牌号")
    private String vehicleNo;
    
    @Schema(name = "驾驶员ID", description = "驾驶员ID")
    private Long driverId;
    
    @Schema(name = "驾驶员姓名", description = "驾驶员姓名")
    private String driverName;
    
    @Schema(name = "开始时间", description = "行程开始时间")
    private LocalDateTime startTime;
    
    @Schema(name = "结束时间", description = "行程结束时间")
    private LocalDateTime endTime;
    
    @Schema(name = "起点位置", description = "起点位置")
    private String startLocation;
    
    @Schema(name = "起点经度", description = "起点经度")
    private BigDecimal startLongitude;
    
    @Schema(name = "起点纬度", description = "起点纬度")
    private BigDecimal startLatitude;
    
    @Schema(name = "终点位置", description = "终点位置")
    private String endLocation;
    
    @Schema(name = "终点经度", description = "终点经度")
    private BigDecimal endLongitude;
    
    @Schema(name = "终点纬度", description = "终点纬度")
    private BigDecimal endLatitude;
    
    @Schema(name = "起始里程", description = "起始里程（公里）")
    private BigDecimal startMileage;
    
    @Schema(name = "结束里程", description = "结束里程（公里）")
    private BigDecimal endMileage;
    
    @Schema(name = "行程距离", description = "行程距离（公里）")
    private BigDecimal tripDistance;
    
    @Schema(name = "油耗", description = "油耗（升）")
    private BigDecimal fuelConsumption;
    
    @Schema(name = "行程状态", description = "行程状态：1-进行中，2-已结束")
    private Integer tripStatus;
    
    @Schema(name = "状态描述", description = "行程状态描述")
    private String tripStatusDesc;
    
    @Schema(name = "创建时间", description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(name = "更新时间", description = "更新时间")
    private LocalDateTime updateTime;
}
