package com.example.car.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "北斗定位查询条件")
public class BeidouLocationQueryDTO {

    @Schema(description = "北斗终端编号", example = "BD123456789")
    private String terminalNo;

    @Schema(description = "车辆ID", example = "1")
    private Long vehicleId;

    @Schema(description = "车牌号", example = "京A12345")
    private String vehicleNo;

    @Schema(description = "开始时间", example = "2024-01-01 00:00:00")
    private LocalDateTime startTime;

    @Schema(description = "结束时间", example = "2024-01-31 23:59:59")
    private LocalDateTime endTime;

    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;
}
