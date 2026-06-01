package com.example.car.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "系统部门查询条件")
public class SysDeptQueryDTO {
    
    @Schema(name = "部门名称", description = "部门名称（模糊查询）", example = "技术部")
    private String deptName;
    
    @Schema(name = "父部门ID", description = "父部门ID", example = "0")
    private Long parentId;
    
    @Schema(name = "状态", description = "状态：0-禁用，1-启用", example = "1")
    private Integer status;
    
    @Schema(name = "页码", description = "页码", example = "1")
    private Integer pageNum = 1;
    
    @Schema(name = "每页数量", description = "每页数量", example = "10")
    private Integer pageSize = 10;
}
