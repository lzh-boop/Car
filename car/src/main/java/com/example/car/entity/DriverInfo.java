package com.example.car.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 驾驶员信息实体类
 */
@Data
@TableName("driver_info")
public class DriverInfo {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String driverName;
    
    private String phone;
    
    private String idCard;
    
    private String licenseNo;
    
    private String licenseType;
    
    private LocalDate licenseDate;
    
    private Long deptId;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
