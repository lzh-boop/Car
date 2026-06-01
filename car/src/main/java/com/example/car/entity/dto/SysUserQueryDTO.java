package com.example.car.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 系统用户查询DTO
 */
@Data
@Schema(description = "系统用户查询条件")
public class SysUserQueryDTO {
    
    @Schema(name = "用户名", description = "用户名（模糊查询）", example = "admin")
    private String username;
    
    @Schema(name = "真实姓名", description = "真实姓名（模糊查询）", example = "张三")
    private String realName;
    
    @Schema(name = "手机号", description = "手机号", example = "13800138000")
    private String phone;
    
    @Schema(name = "部门ID", description = "部门ID", example = "1")
    private Long deptId;
    
    @Schema(name = "状态", description = "状态：0-禁用，1-启用", example = "1")
    private Integer status;
    
    @Schema(name = "页码", description = "页码", example = "1")
    private Integer pageNum = 1;
    
    @Schema(name = "每页数量", description = "每页数量", example = "10")
    private Integer pageSize = 10;
}
