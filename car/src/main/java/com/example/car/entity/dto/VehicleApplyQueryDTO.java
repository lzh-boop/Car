package com.example.car.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用车申请查询DTO
 */
@Data
@Schema(description = "用车申请查询条件")
public class VehicleApplyQueryDTO {
    
    @Schema(name = "申请单号", description = "申请单号（模糊查询）", example = "SQ202401010001")
    private String applyNo;
    
    @Schema(name = "申请人ID", description = "申请人ID", example = "1")
    private Long applyUserId;
    
    @Schema(name = "申请人姓名", description = "申请人姓名（模糊查询）", example = "李四")
    private String applyUserName;

    @Schema(name = "车牌号", description = "车牌号（精确查询）", example = "京A12345")
    private String vehicleNo;

    @Schema(name = "申请部门ID", description = "申请部门ID", example = "1")
    private Long deptId;
    
    @Schema(name = "申请状态", description = "申请状态：0-待审批，1-已通过，2-已拒绝，3-已取消", example = "0")
    private Integer applyStatus;
    
    @Schema(name = "开始时间", description = "开始时间（查询范围）", example = "2024-01-01 00:00:00")
    private LocalDateTime startTime;
    
    @Schema(name = "结束时间", description = "结束时间（查询范围）", example = "2024-01-31 23:59:59")
    private LocalDateTime endTime;
    
    @Schema(name = "页码", description = "页码", example = "1")
    private Integer pageNum = 1;
    
    @Schema(name = "每页数量", description = "每页数量", example = "10")
    private Integer pageSize = 10;
}
