package com.example.car.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 车辆信息实体类
 */
@Data
@TableName("vehicle_info")
public class VehicleInfo {
    
    /**
     * 车辆ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 车牌号
     */
    private String vehicleNo;
    
    /**
     * 车辆类型
     */
    private String vehicleType;
    
    /**
     * 品牌
     */
    private String brand;
    
    /**
     * 型号
     */
    private String model;
    
    /**
     * 颜色
     */
    private String color;
    
    /**
     * 车架号
     */
    private String vin;
    
    /**
     * 发动机号
     */
    private String engineNo;
    
    /**
     * 购置日期
     */
    private LocalDate purchaseDate;
    
    /**
     * 购置价格
     */
    private BigDecimal purchasePrice;
    
    /**
     * 所属部门ID
     */
    private Long deptId;
    
    /**
     * 状态 0-空闲 1-在用 2-维修 3-报废
     */
    private Integer status;

    /**
     * 座位数
     */
    private Integer seats;

    /**
     * 里程(km)
     */
    private Integer mileage;

    /**
     * 备注
     */
    private String remark;

    /**
     * 北斗终端编号
     */
    private String terminalNo;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
