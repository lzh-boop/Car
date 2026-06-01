package com.example.car.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.car.common.result.Result;
import com.example.car.entity.SysUser;
import com.example.car.entity.dto.SysUserQueryDTO;
import com.example.car.entity.vo.SysUserVO;
import com.example.car.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 * 所有接口均要求 ROLE_ADMIN 权限：
 *  - 路由层：SecurityConfig 中 /api/system/** 已配置 hasRole("ADMIN")
 *  - 方法层：@PreAuthorize 二重防护，防止路由配置漏掉新接口时权限失效
 */
@Tag(name = "用户管理", description = "系统用户信息管理相关接口")
@RestController
@RequestMapping("/api/system/user")
@RequiredArgsConstructor
public class UserController {

    private final SysUserService sysUserService;

    @Operation(summary = "分页查询用户列表", description = "根据条件分页查询系统用户")
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<SysUserVO>> pageQuery(SysUserQueryDTO queryDTO) {
        return Result.success(sysUserService.pageQuery(queryDTO));
    }

    @Operation(summary = "查询用户详情", description = "根据ID查询用户详细信息")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<SysUserVO> getDetail(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long id) {
        return Result.success(sysUserService.getDetailById(id));
    }

    @Operation(summary = "新增用户", description = "添加新的系统用户")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> add(@Validated @RequestBody SysUser sysUser) {
        boolean ok = sysUserService.addUser(sysUser);
        return ok ? Result.success() : Result.error("新增用户失败");
    }

    @Operation(summary = "更新用户信息", description = "修改用户信息")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@Validated @RequestBody SysUser sysUser) {
        boolean ok = sysUserService.updateUser(sysUser);
        return ok ? Result.success() : Result.error("更新用户失败");
    }

    @Operation(summary = "删除用户", description = "删除指定用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long id) {
        boolean ok = sysUserService.deleteUser(id);
        return ok ? Result.success() : Result.error("删除用户失败");
    }

    @Operation(summary = "重置密码", description = "将用户密码重置为指定密码")
    @PutMapping("/resetPassword/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> resetPassword(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long id,
            @RequestParam String newPassword) {
        boolean ok = sysUserService.resetPassword(id, newPassword);
        return ok ? Result.success() : Result.error("重置密码失败");
    }

    @Operation(summary = "修改用户状态", description = "启用或禁用指定用户")
    @PutMapping("/changeStatus/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> changeStatus(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long id,
            @RequestParam Integer status) {
        boolean ok = sysUserService.changeStatus(id, status);
        return ok ? Result.success() : Result.error("修改状态失败");
    }
}
