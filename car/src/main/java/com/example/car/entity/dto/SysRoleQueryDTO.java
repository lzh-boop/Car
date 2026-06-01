package com.example.car.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 系统角色查询DTO
 */
@Data
@Schema(description = "系统角色查询条件")
public class SysRoleQueryDTO {
    
    @Schema(name = "角色名称", description = "角色名称（模糊查询）", example = "管理员")
    private String roleName;
    
    @Schema(name = "角色标识", description = "角色标识（模糊查询）", example = "admin")
    private String roleKey;
    
    @Schema(name = "页码", description = "页码", example = "1")
    private Integer pageNum = 1;
    
    @Schema(name = "每页数量", description = "每页数量", example = "10")
    private Integer pageSize = 10;
}
