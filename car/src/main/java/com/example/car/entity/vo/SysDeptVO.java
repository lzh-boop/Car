package com.example.car.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "系统部门信息")
public class SysDeptVO {
    
    @Schema(name = "部门ID", description = "部门ID")
    private Long id;
    
    @Schema(name = "父部门ID", description = "父部门ID")
    private Long parentId;
    
    @Schema(name = "祖级列表", description = "祖级列表")
    private String ancestors;
    
    @Schema(name = "部门名称", description = "部门名称")
    private String deptName;
    
    @Schema(name = "显示顺序", description = "显示顺序")
    private Integer orderNum;
    
    @Schema(name = "负责人", description = "负责人")
    private String leader;
    
    @Schema(name = "联系电话", description = "联系电话")
    private String phone;
    
    @Schema(name = "邮箱", description = "邮箱")
    private String email;
    
    @Schema(name = "状态", description = "状态：0-禁用，1-启用")
    private Integer status;
    
    @Schema(name = "状态描述", description = "状态描述")
    private String statusDesc;
    
    @Schema(name = "创建时间", description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(name = "更新时间", description = "更新时间")
    private LocalDateTime updateTime;
}
