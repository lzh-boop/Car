package com.example.car.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("vehicle_maintenance")
public class VehicleMaintenance {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long vehicleId;
    
    private String vehicleNo;
    
    private Integer maintenanceType;
    
    private LocalDate maintenanceDate;
    
    private String maintenanceItem;
    
    private BigDecimal maintenanceCost;
    
    private BigDecimal currentMileage;
    
    private String serviceProvider;
    
    private LocalDate nextMaintenanceDate;
    
    private BigDecimal nextMaintenanceMileage;
    
    private String remark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
