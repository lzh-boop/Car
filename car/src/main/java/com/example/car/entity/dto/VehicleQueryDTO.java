package com.example.car.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 车辆查询DTO
 */
@Data
@Schema(description = "车辆查询条件")
public class VehicleQueryDTO {
    
    @Schema(name = "车牌号", description = "车牌号（精确查询）", example = "京A12345")
    private String vehicleNo;
    
    @Schema(name = "车辆类型", description = "车辆类型（模糊查询，如：轿车、SUV、货车等）", example = "轿车")
    private String vehicleType;

    @Schema(name = "状态", description = "车辆状态：0-空闲，1-在用，2-维修，3-报废", example = "0")
    private Integer status;
    
    @Schema(name = "页码", description = "页码", example = "1")
    private Integer pageNum = 1;
    
    @Schema(name = "每页数量", description = "每页数量", example = "10")
    private Integer pageSize = 10;
}
