package com.example.car.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 行程记录实体类
 */
@Data
@TableName("trip_record")
public class TripRecord {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long dispatchId;
    
    private Long vehicleId;
    
    private String vehicleNo;
    
    private Long driverId;
    
    private String driverName;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private String startLocation;
    
    private BigDecimal startLongitude;
    
    private BigDecimal startLatitude;
    
    private String endLocation;
    
    private BigDecimal endLongitude;
    
    private BigDecimal endLatitude;
    
    private BigDecimal startMileage;
    
    private BigDecimal endMileage;
    
    private BigDecimal tripDistance;
    
    private BigDecimal fuelConsumption;
    
    private Integer tripStatus;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
