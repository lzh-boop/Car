package com.example.car.controller.location;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.car.common.exception.BusinessException;
import com.example.car.common.result.Result;
import com.example.car.entity.SysUser;
import com.example.car.entity.dto.FenceAlarmQueryDTO;
import com.example.car.entity.vo.FenceAlarmVO;
import com.example.car.mapper.SysUserMapper;
import com.example.car.service.FenceAlarmService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 围栏报警控制器
 * 所有接口仅 ADMIN 角色可访问
 */
@Tag(name = "围栏报警", description = "围栏报警记录管理相关接口")
@RestController
@RequestMapping("/api/location/fence/alarm")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class FenceAlarmController {

    private final FenceAlarmService fenceAlarmService;
    private final SysUserMapper sysUserMapper;

    @Operation(summary = "分页查询报警记录")
    @GetMapping("/list")
    public Result<Page<FenceAlarmVO>> pageQuery(FenceAlarmQueryDTO queryDTO) {
        return Result.success(fenceAlarmService.pageQuery(queryDTO));
    }

    @Operation(summary = "查询报警详情")
    @GetMapping("/{id}")
    public Result<FenceAlarmVO> getDetail(
            @Parameter(description = "报警ID") @PathVariable Long id) {
        return Result.success(fenceAlarmService.getDetailById(id));
    }

    /**
     * 处理报警
     * 安全修复：handlerId 不再由前端传入，而是从当前登录 Token 中解析，
     * 防止调用方伪造任意 handlerId 冒充他人处理记录。
     */
    @Operation(summary = "处理报警（handlerId 自动取当前登录用户，无需前端传入）")
    @PutMapping("/handle/{id}")
    public Result<Void> handle(
            @Parameter(description = "报警ID") @PathVariable Long id,
            @RequestParam String handleRemark) {

        // 从 SecurityContext 获取当前登录用户名
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Result.error(401, "未登录");
        }
        String username = auth.getName();

        // 根据用户名查询用户 ID
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) {
            throw new BusinessException("当前登录用户不存在");
        }

        boolean success = fenceAlarmService.handleAlarm(id, user.getId(), handleRemark);
        return success ? Result.success() : Result.error("处理报警失败");
    }
}
