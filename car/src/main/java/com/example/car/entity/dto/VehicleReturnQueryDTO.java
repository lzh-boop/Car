package com.example.car.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 还车记录查询DTO
 */
@Data
@Schema(description = "还车记录查询条件")
public class VehicleReturnQueryDTO {

    @Schema(description = "车牌号（模糊查询）")
    private String vehicleNo;

    @Schema(description = "驾驶员姓名（模糊查询）")
    private String driverName;

    @Schema(description = "还车状态：0-待还车 1-已还车")
    private Integer returnStatus;

    @Schema(description = "页码")
    private Integer pageNum = 1;

    @Schema(description = "每页数量")
    private Integer pageSize = 10;
}
