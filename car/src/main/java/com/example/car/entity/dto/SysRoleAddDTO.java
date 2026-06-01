package com.example.car.entity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 角色新增DTO
 * 用于新增操作，不包含id、createTime、updateTime等系统字段
 */
@Data
@Schema(description = "角色新增请求")
public class SysRoleAddDTO {
    @JsonProperty("角色名称")
    @Schema(name = "角色名称", description = "角色名称", example = "管理员")
    private String roleName;
    
    @JsonProperty("角色标识")
    @Schema(name = "角色标识", description = "角色标识", example = "admin")
    private String roleKey;

    @JsonProperty("排序")
    @Schema(name = "排序", description = "排序", example = "1")
    private Integer sort;

    @JsonProperty("状态")
    @Schema(name = "状态", description = "状态：0-禁用，1-启用", example = "1")
    private Integer status;
    
    @JsonProperty("备注")
    @Schema(name = "备注", description = "备注", example = "系统管理员角色")
    private String remark;
}
