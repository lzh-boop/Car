package com.example.car.entity.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用车申请VO
 */
@Data
@Schema(description = "用车申请信息")
public class VehicleApplyVO {
    
    @Schema(name = "申请ID", description = "申请ID")
    private Long id;
    
    @Schema(name = "申请单号", description = "申请单号")
    private String applyNo;

    @Schema(name = "车牌号", description = "申请使用的车牌号")
    private String vehicleNo;

    @Schema(name = "申请人ID", description = "申请人ID")
    private Long applyUserId;
    
    @Schema(name = "申请人姓名", description = "申请人姓名")
    private String applyUserName;
    
    @Schema(name = "申请部门ID", description = "申请部门ID")
    private Long deptId;
    
    @Schema(name = "部门名称", description = "部门名称")
    private String deptName;
    
    @Schema(name = "用车事由", description = "用车事由")
    private String purpose;
    
    @Schema(name = "乘车人数", description = "乘车人数")
    private Integer passengerCount;
    
    @Schema(name = "乘车人员", description = "乘车人员名单")
    private String passengerNames;
    
    @Schema(name = "出发地", description = "出发地")
    private String startLocation;
    
    @Schema(name = "目的地", description = "目的地")
    private String endLocation;
    
    @Schema(name = "计划开始时间", description = "计划开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planStartTime;

    @Schema(name = "计划结束时间", description = "计划结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime planEndTime;
    
    @Schema(name = "申请状态", description = "申请状态：0-待审批，1-已通过，2-已拒绝，3-已取消")
    private Integer applyStatus;
    
    @Schema(name = "状态描述", description = "申请状态描述")
    private String applyStatusDesc;
    
    @Schema(name = "申请时间", description = "申请时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyTime;
    
    @Schema(name = "备注", description = "备注")
    private String remark;
}
