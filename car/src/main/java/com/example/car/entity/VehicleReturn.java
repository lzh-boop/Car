package com.example.car.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 还车记录实体类
 */
@Data
@TableName("vehicle_return")
public class VehicleReturn {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long dispatchId;

    private String dispatchNo;

    private Long vehicleId;

    private String vehicleNo;

    private Long driverId;

    private String driverName;

    private String endLocation;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planEndTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualEndTime;

    private Integer mileageBefore;

    private Integer mileageAfter;

    /** 油量状态：0-充足 1-偏少 2-需加油 */
    private Integer fuelLevel;

    /** 车辆状况：0-正常 1-轻微损伤 2-需维修 */
    private Integer vehicleCondition;

    private String remark;

    /** 还车状态：0-待还车 1-已还车 */
    private Integer returnStatus;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
