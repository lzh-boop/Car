package com.example.car.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 围栏报警记录实体类
 */
@Data
@TableName("fence_alarm")
public class FenceAlarm {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long fenceId;
    
    private String fenceName;
    
    private Long vehicleId;
    
    private String vehicleNo;
    
    private Integer alarmType;
    
    private LocalDateTime alarmTime;
    
    private BigDecimal longitude;
    
    private BigDecimal latitude;
    
    private String address;
    
    private Integer isHandled;
    
    private Long handlerId;
    
    private LocalDateTime handleTime;
    
    private String handleRemark;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
