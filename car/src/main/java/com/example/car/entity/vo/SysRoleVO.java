package com.example.car.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统角色VO
 */
@Data
@Schema(description = "系统角色信息")
public class SysRoleVO {
    
    @Schema(name = "角色ID", description = "角色ID")
    private Long id;
    
    @Schema(name = "角色名称", description = "角色名称")
    private String roleName;
    
    @Schema(name = "角色标识", description = "角色标识")
    private String roleKey;
    
    @Schema(name = "排序", description = "排序")
    private Integer sort;
    
    @Schema(name = "状态", description = "状态：0-禁用，1-启用")
    private Integer status;
    
    @Schema(name = "状态描述", description = "状态描述")
    private String statusDesc;
    
    @Schema(name = "备注", description = "备注")
    private String remark;
    
    @Schema(name = "创建时间", description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(name = "更新时间", description = "更新时间")
    private LocalDateTime updateTime;
}
