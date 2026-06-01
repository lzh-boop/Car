package com.example.car.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.car.common.result.Result;
import com.example.car.entity.SysRole;
import com.example.car.entity.dto.SysRoleAddDTO;
import com.example.car.entity.dto.SysRoleQueryDTO;
import com.example.car.entity.dto.SysRoleUpdateDTO;
import com.example.car.entity.vo.SysRoleVO;
import com.example.car.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 角色管理控制器
 */
@Tag(name = "角色管理", description = "角色信息管理相关接口")
@RestController
@RequestMapping("/api/system/role")
@RequiredArgsConstructor
public class RoleController {
    
    private final SysRoleService sysRoleService;
    
    @Operation(summary = "分页查询角色列表", description = "根据条件分页查询系统角色")
    @GetMapping("/list")
    public Result<Page<SysRoleVO>> pageQuery(SysRoleQueryDTO queryDTO) {
        Page<SysRoleVO> page = sysRoleService.pageQuery(queryDTO);
        return Result.success(page);
    }
    
    @Operation(summary = "查询角色详情", description = "根据ID查询角色详细信息")
    @GetMapping("/{id}")
    public Result<SysRoleVO> getDetail(
            @Parameter(description = "角色ID", required = true)
            @PathVariable Long id) {
        SysRoleVO vo = sysRoleService.getDetailById(id);
        return Result.success(vo);
    }
    
    @Operation(summary = "新增角色", description = "添加新的系统角色")
    @PostMapping
    public Result<Void> add(@Validated @RequestBody SysRoleAddDTO addDTO) {
        // 将DTO转换为Entity
        SysRole sysRole = new SysRole();
        BeanUtils.copyProperties(addDTO, sysRole);
        // 确保id为null（由数据库自动生成）
        // createTime和updateTime为null，由MetaObjectHandler自动填充为当前时间
        sysRole.setId(null);
        
        boolean success = sysRoleService.addRole(sysRole);
        return success ? Result.success() : Result.error("新增角色失败");
    }
    
    @Operation(summary = "更新角色信息", description = "更新角色信息")
    @PutMapping
    public Result<Void> update(@Validated @RequestBody SysRoleUpdateDTO updateDTO) {
        // 将DTO转换为Entity
        SysRole sysRole = new SysRole();
        BeanUtils.copyProperties(updateDTO, sysRole);
        // 确保createTime、updateTime为null（由系统自动处理）
        sysRole.setCreateTime(null);
        sysRole.setUpdateTime(null);
        
        boolean success = sysRoleService.updateRole(sysRole);
        return success ? Result.success() : Result.error("更新角色失败");
    }
    
    @Operation(summary = "删除角色", description = "删除指定角色")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "角色ID", required = true)
            @PathVariable Long id) {
        boolean success = sysRoleService.deleteRole(id);
        return success ? Result.success() : Result.error("删除角色失败");
    }
}
