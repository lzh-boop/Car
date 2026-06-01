package com.example.car.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 电子围栏实体类
 */
@Data
@TableName("geo_fence")
public class GeoFence {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String fenceName;
    
    private Integer fenceType;
    
    private String centerPoint;
    
    private Integer radius;
    
    private String polygonPoints;
    
    private String bindVehicles;
    
    private Integer alarmType;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
