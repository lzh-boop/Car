package com.example.car.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用车申请实体类
 */
@Data
@TableName("vehicle_apply")
public class VehicleApply {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String applyNo;

    private String vehicleNo;

    private Long applyUserId;
    
    private String applyUserName;
    
    private Long deptId;
    
    private String deptName;
    
    private String purpose;
    
    private Integer passengerCount;
    
    private String passengerNames;
    
    private String startLocation;
    
    private String endLocation;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planEndTime;

    private Integer applyStatus;

    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyTime;
    
    private String remark;
}
