package com.example.car.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 车辆维护新增DTO
 */
@Data
@Schema(description = "车辆维护新增信息")
public class VehicleMaintenanceAddDTO {

    @Schema(description = "车牌号", example = "京A12345")
    private String vehicleNo;

    @Schema(description = "维护类型：1-保养，2-维修，3-年检，4-保险", example = "1")
    private Integer maintenanceType;

    @Schema(description = "维护日期", example = "2024-01-15")
    private LocalDate maintenanceDate;

    @Schema(description = "维护项目", example = "更换机油、三滤")
    private String maintenanceItem;

    @Schema(description = "维护费用（单位：元）", example = "800.00")
    private BigDecimal maintenanceCost;

    @Schema(description = "当前里程（单位：公里）", example = "50000")
    private BigDecimal currentMileage;

    @Schema(description = "服务商", example = "4S店")
    private String serviceProvider;

    @Schema(description = "下次维护日期", example = "2024-07-15")
    private LocalDate nextMaintenanceDate;

    @Schema(description = "下次维护里程（单位：公里）", example = "60000")
    private BigDecimal nextMaintenanceMileage;

    @Schema(description = "备注", example = "常规保养")
    private String remark;
}
