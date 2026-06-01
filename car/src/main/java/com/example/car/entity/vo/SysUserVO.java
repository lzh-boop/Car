package com.example.car.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统用户 VO
 *
 * 角色字段说明：
 *  - role  (String)       ：权威鉴权角色，直接来自 sys_user.role 字段（"ADMIN" 或 "USER"），
 *                           与 UserDetailsServiceImpl 用于 Spring Security 权限判断的来源完全一致。
 *  - roles (List<String>) ：展示用角色标识列表，来自 sys_user_role + sys_role 关系表，
 *                           历史数据可能为空，不影响鉴权结果。
 *
 * 两个字段同时返回，方便管理员发现鉴权角色与关系表角色之间的数据不一致问题。
 */
@Data
@Schema(description = "系统用户信息")
public class SysUserVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 权威鉴权角色，来源于 sys_user.role 字段，与 Spring Security 鉴权保持一致。
     * 取值：ADMIN（管理员）/ USER（普通用户）
     */
    @Schema(description = "鉴权角色（ADMIN/USER），与 Spring Security 权限来源一致")
    private String role;

    /**
     * 展示用角色标识列表，来源于 sys_user_role + sys_role 关系表（历史遗留，可能为空）。
     * 访问权限控制以 role 字段为准，此列表仅用于展示。
     */
    @Schema(description = "展示用角色标识列表，来自角色关系表（可能为空）")
    private List<String> roles;
}
