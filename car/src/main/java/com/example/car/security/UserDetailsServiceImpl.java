package com.example.car.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.car.entity.SysUser;
import com.example.car.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring Security 用户详情服务
 * 安全修复：从数据库 role 字段加载真实角色，不再固定 ROLE_USER
 * 支持 ROLE_ADMIN（系统管理员）和 ROLE_USER（普通用户）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper sysUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        SysUser sysUser = sysUserMapper.selectOne(wrapper);

        if (sysUser == null) {
            // 统一错误信息，不暴露"用户不存在"细节
            log.warn("[Auth] 登录失败，用户不存在: {}", username);
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        if (sysUser.getStatus() != null && sysUser.getStatus() == 0) {
            log.warn("[Auth] 登录失败，账号已禁用: {}", username);
            throw new UsernameNotFoundException("账号已被禁用");
        }

        // 从数据库 role 字段加载角色，默认为 ROLE_USER
        String roleValue = sysUser.getRole();
        String grantedRole = (roleValue != null && !roleValue.isBlank())
                ? "ROLE_" + roleValue.toUpperCase()
                : "ROLE_USER";

        return User.builder()
                .username(sysUser.getUsername())
                .password(sysUser.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority(grantedRole)))
                .build();
    }
}
