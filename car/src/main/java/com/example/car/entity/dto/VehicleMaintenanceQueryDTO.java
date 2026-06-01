package com.example.car.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDate;

@Data
@Schema(description = "车辆维护查询条件")
public class VehicleMaintenanceQueryDTO {
    
    @Schema(name = "车牌号", description = "车牌号（模糊查询）", example = "京A12345")
    private String vehicleNo;

    @Schema(name = "维护类型", description = "维护类型：1-保养，2-维修，3-年检，4-保险", example = "1")
    private Integer maintenanceType;
    
    @Schema(name = "页码", description = "页码", example = "1")
    private Integer pageNum = 1;
    
    @Schema(name = "每页数量", description = "每页数量", example = "10")
    private Integer pageSize = 10;
}
