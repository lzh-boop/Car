package com.example.car.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 车辆运维统计 VO（每辆车一行）
 */
@Data
@Schema(description = "车辆运维统计信息")
public class VehicleStatsVO {

    @Schema(description = "车牌号")
    private String vehicleNo;

    @Schema(description = "车辆类型")
    private String vehicleType;

    @Schema(description = "品牌")
    private String brand;

    @Schema(description = "当前状态：0-空闲 1-在用 2-维修 3-报废")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "总里程(km)")
    private Integer mileage;

    @Schema(description = "调度次数")
    private Long dispatchCount;

    @Schema(description = "保养费用合计(元)")
    private BigDecimal maintainCost;

    @Schema(description = "维修费用合计(元)")
    private BigDecimal repairCost;

    @Schema(description = "保险费用合计(元)")
    private BigDecimal insuranceCost;
}
