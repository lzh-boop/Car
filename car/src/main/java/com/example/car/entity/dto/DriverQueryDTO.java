package com.example.car.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 驾驶员查询DTO
 */
@Data
@Schema(description = "驾驶员查询条件")
public class DriverQueryDTO {
    
    @Schema(name = "驾驶员姓名", description = "驾驶员姓名（模糊查询）", example = "王五")
    private String driverName;
    
    @Schema(name = "手机号", description = "手机号", example = "13800138000")
    private String phone;
    
    @Schema(name = "驾驶证号", description = "驾驶证号", example = "110105199001011234")
    private String licenseNo;
    
    @Schema(name = "准驾车型", description = "准驾车型", example = "C1")
    private String licenseType;
    
    @Schema(name = "所属部门ID", description = "所属部门ID", example = "1")
    private Long deptId;
    
    @Schema(name = "状态", description = "状态：1-正常，2-停用", example = "1")
    private Integer status;
    
    @Schema(name = "页码", description = "页码", example = "1")
    private Integer pageNum = 1;
    
    @Schema(name = "每页数量", description = "每页数量", example = "10")
    private Integer pageSize = 10;
}
