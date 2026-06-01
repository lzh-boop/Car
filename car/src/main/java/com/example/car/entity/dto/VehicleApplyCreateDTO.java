package com.example.car.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建用车申请请求 DTO
 *
 * 安全说明：只暴露申请人可以填写的字段。
 * 服务端写入字段（applyNo 申请单号、applyUserId 申请人ID、applyStatus 状态、
 * applyTime 申请时间、deptId 部门ID）不在此 DTO 中，由 Service 层自动填充，
 * 防止前端越权赋值。
 */
@Data
@Schema(description = "创建用车申请请求")
public class VehicleApplyCreateDTO {

    @NotBlank(message = "车牌号不能为空")
    @Schema(description = "车牌号")
    private String vehicleNo;

    @NotBlank(message = "用车事由不能为空")
    @Schema(description = "用车事由")
    private String purpose;

    @NotNull(message = "计划开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "计划开始时间")
    private LocalDateTime planStartTime;

    @NotNull(message = "计划结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "计划结束时间")
    private LocalDateTime planEndTime;

    @Schema(description = "出发地点")
    private String startLocation;

    @Schema(description = "目的地点")
    private String endLocation;

    @Min(value = 1, message = "乘车人数至少为1人")
    @Schema(description = "乘车人数（含驾驶员）")
    private Integer passengerCount;

    @Schema(description = "乘车人员姓名")
    private String passengerNames;

    @Schema(description = "备注")
    private String remark;
}
