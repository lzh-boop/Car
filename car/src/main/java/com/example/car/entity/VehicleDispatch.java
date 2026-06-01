package com.example.car.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 车辆调度实体类
 */
@Data
@TableName("vehicle_dispatch")
public class VehicleDispatch {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long applyId;
    
    private String dispatchNo;
    
    private Long vehicleId;
    
    private String vehicleNo;
    
    private Long driverId;
    
    private String driverName;
    
    private String startLocation;
    
    private String endLocation;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planEndTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime actualEndTime;
    
    private Integer dispatchStatus;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
