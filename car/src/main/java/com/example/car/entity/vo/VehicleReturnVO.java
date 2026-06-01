package com.example.car.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 还车记录VO
 */
@Data
@Schema(description = "还车记录信息")
public class VehicleReturnVO {

    @Schema(description = "还车记录ID")
    private Long id;

    @Schema(description = "关联调度单ID")
    private Long dispatchId;

    @Schema(description = "调度单号")
    private String dispatchNo;

    @Schema(description = "车辆ID")
    private Long vehicleId;

    @Schema(description = "车牌号")
    private String vehicleNo;

    @Schema(description = "车辆类型")
    private String vehicleType;

    @Schema(description = "车辆品牌")
    private String brand;

    @Schema(description = "驾驶员ID")
    private Long driverId;

    @Schema(description = "驾驶员姓名")
    private String driverName;

    @Schema(description = "目的地")
    private String endLocation;

    @Schema(description = "计划还车时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planEndTime;

    @Schema(description = "实际还车时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualEndTime;

    @Schema(description = "还车前里程(km)")
    private Integer mileageBefore;

    @Schema(description = "还车后里程(km)")
    private Integer mileageAfter;

    @Schema(description = "油量状态：0-充足 1-偏少 2-需加油")
    private Integer fuelLevel;

    @Schema(description = "油量状态描述")
    private String fuelLevelDesc;

    @Schema(description = "车辆状况：0-正常 1-轻微损伤 2-需维修")
    private Integer vehicleCondition;

    @Schema(description = "车辆状况描述")
    private String vehicleConditionDesc;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "还车状态：0-待还车 1-已还车")
    private Integer returnStatus;

    @Schema(description = "还车状态描述")
    private String returnStatusDesc;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
