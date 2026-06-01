package com.example.car.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "围栏报警查询条件")
public class FenceAlarmQueryDTO {
    
    @Schema(name = "围栏ID", description = "围栏ID", example = "1")
    private Long fenceId;
    
    @Schema(name = "围栏名称", description = "围栏名称（模糊查询）", example = "公司围栏")
    private String fenceName;
    
    @Schema(name = "车辆ID", description = "车辆ID", example = "1")
    private Long vehicleId;
    
    @Schema(name = "车牌号", description = "车牌号（模糊查询）", example = "京A12345")
    private String vehicleNo;
    
    @Schema(name = "报警类型", description = "报警类型：1-进入，2-离开", example = "1")
    private Integer alarmType;
    
    @Schema(name = "是否处理", description = "是否处理：0-未处理，1-已处理", example = "0")
    private Integer isHandled;
    
    @Schema(name = "开始时间", description = "开始时间", example = "2024-01-01 00:00:00")
    private LocalDateTime startTime;
    
    @Schema(name = "结束时间", description = "结束时间", example = "2024-01-31 23:59:59")
    private LocalDateTime endTime;
    
    @Schema(name = "页码", description = "页码", example = "1")
    private Integer pageNum = 1;
    
    @Schema(name = "每页数量", description = "每页数量", example = "10")
    private Integer pageSize = 10;
}
