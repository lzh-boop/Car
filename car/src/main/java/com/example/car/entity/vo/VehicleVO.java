package com.example.car.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 车辆信息VO
 */
@Data
@Schema(description = "车辆信息")
public class VehicleVO {
    
    @Schema(name = "车辆ID", description = "车辆ID")
    private Long id;
    
    @Schema(name = "车牌号", description = "车牌号")
    private String vehicleNo;
    
    @Schema(name = "车辆类型", description = "车辆类型")
    private String vehicleType;
    
    @Schema(name = "品牌", description = "品牌")
    private String brand;
    
    @Schema(name = "型号", description = "型号")
    private String model;
    
    @Schema(name = "颜色", description = "颜色")
    private String color;
    
    @Schema(name = "购置日期", description = "购置日期")
    private LocalDate purchaseDate;
    
    @Schema(name = "购置价格", description = "购置价格（单位：元）")
    private BigDecimal purchasePrice;
    
    @Schema(name = "所属部门名称", description = "所属部门名称")
    private String deptName;
    
    @Schema(name = "状态", description = "状态：0-空闲，1-在用，2-维修，3-报废")
    private Integer status;

    @Schema(name = "状态描述", description = "状态描述")
    private String statusDesc;

    @Schema(name = "座位数", description = "座位数")
    private Integer seats;

    @Schema(name = "里程(km)", description = "里程")
    private Integer mileage;

    @Schema(name = "备注", description = "备注")
    private String remark;

    @Schema(name = "北斗终端编号", description = "北斗终端编号")
    private String terminalNo;
    
    @Schema(name = "是否在线", description = "是否在线")
    private Boolean online;
}
