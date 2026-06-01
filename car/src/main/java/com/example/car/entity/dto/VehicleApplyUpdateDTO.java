package com.example.car.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 修改用车申请请求 DTO
 *
 * 安全说明：id 必填，其余字段为申请人可编辑的内容子集。
 * 服务端控制字段（申请单号、申请人ID、申请状态等）不可通过此 DTO 修改，
 * 防止越权篡改。
 */
@Data
@Schema(description = "修改用车申请请求")
public class VehicleApplyUpdateDTO {

    @NotNull(message = "申请ID不能为空")
    @Schema(description = "申请ID")
    private Long id;

    @Schema(description = "用车事由")
    private String purpose;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "计划开始时间")
    private LocalDateTime planStartTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "计划结束时间")
    private LocalDateTime planEndTime;

    @Schema(description = "出发地点")
    private String startLocation;

    @Schema(description = "目的地点")
    private String endLocation;

    @Min(value = 1, message = "乘车人数至少为1人")
    @Schema(description = "乘车人数")
    private Integer passengerCount;

    @Schema(description = "乘车人员姓名")
    private String passengerNames;

    @Schema(description = "备注")
    private String remark;
}
