package com.example.car.controller.auth;

import com.example.car.common.exception.BusinessException;
import com.example.car.common.result.Result;
import com.example.car.entity.SysUser;
import com.example.car.entity.dto.LoginDTO;
import com.example.car.entity.dto.UpdatePasswordDTO;
import com.example.car.entity.vo.LoginVO;
import com.example.car.entity.vo.SysUserVO;
import com.example.car.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "用户认证", description = "登录、登出、用户信息、修改密码等接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserService sysUserService;

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "用户名 + 密码登录，返回 Bearer Token")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = sysUserService.login(loginDTO);
        return Result.success("登录成功", loginVO);
    }

    /**
     * 用户登出
     */
    @Operation(summary = "用户登出", description = "退出登录，Token 立即失效",
            security = @SecurityRequirement(name = "Authorization"))
    @PostMapping("/logout")
    public Result<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = extractToken(authHeader);
        sysUserService.logout(token);
        return Result.success("登出成功", null);
    }

    /**
     * 获取当前登录用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "根据 Token 返回当前登录用户详情（含角色、部门）",
            security = @SecurityRequirement(name = "Authorization"))
    @GetMapping("/info")
    public Result<SysUserVO> getUserInfo() {
        String username = currentUsername();
        SysUser sysUser = sysUserService.getByUsername(username);
        if (sysUser == null) {
            return Result.error("用户不存在");
        }
        SysUserVO vo = sysUserService.getDetailById(sysUser.getId());
        return Result.success(vo);
    }

    /**
     * 修改当前用户密码
     */
    @Operation(summary = "修改密码", description = "当前登录用户修改自己的密码，需要验证原密码",
            security = @SecurityRequirement(name = "Authorization"))
    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody UpdatePasswordDTO dto) {
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new BusinessException("两次输入的新密码不一致");
        }
        String username = currentUsername();
        sysUserService.updatePassword(username, dto.getOldPassword(), dto.getNewPassword());
        return Result.success("密码修改成功", null);
    }

    // ------------------------------------------------------------------ helper

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    private String extractToken(String authHeader) {
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
