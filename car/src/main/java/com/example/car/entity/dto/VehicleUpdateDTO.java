package com.example.car.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 车辆更新DTO
 */
@Data
@Schema(description = "车辆更新信息")
public class VehicleUpdateDTO {

    @Schema(description = "车牌号（必填，用于定位要更新的车辆）", example = "京A12345")
    private String vehicleNo;

    @Schema(description = "车辆类型（如：轿车、SUV、货车等）", example = "轿车")
    private String vehicleType;

    @Schema(description = "品牌", example = "大众")
    private String brand;

    @Schema(description = "型号", example = "帕萨特")
    private String model;

    @Schema(description = "颜色", example = "白色")
    private String color;

    @Schema(description = "车架号（VIN码）", example = "LSVAA4182E2123456")
    private String vin;

    @Schema(description = "发动机号", example = "EA888123456")
    private String engineNo;

    @Schema(description = "购置日期", example = "2023-01-15")
    private LocalDate purchaseDate;

    @Schema(description = "购置价格（单位：元）", example = "180000.00")
    private BigDecimal purchasePrice;

    @Schema(description = "所属部门ID", example = "1")
    private Long deptId;

    @Schema(description = "车辆状态：0-空闲，1-在用，2-维修，3-报废", example = "0")
    private Integer status;

    @Schema(description = "座位数", example = "5")
    private Integer seats;

    @Schema(description = "里程(km)", example = "0")
    private Integer mileage;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "北斗终端编号", example = "BD123456789")
    private String terminalNo;
}
