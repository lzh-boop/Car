package com.example.car.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.car.common.constant.Constants;
import com.example.car.common.exception.BusinessException;
import com.example.car.common.util.JwtUtil;
import com.example.car.common.util.RedisUtil;
import com.example.car.entity.SysUser;
import com.example.car.entity.dto.LoginDTO;
import com.example.car.entity.dto.SysUserQueryDTO;
import com.example.car.entity.vo.LoginVO;
import com.example.car.entity.vo.SysUserVO;
import com.example.car.entity.SysDept;
import com.example.car.entity.SysRole;
import com.example.car.entity.SysUserRole;
import com.example.car.mapper.SysDeptMapper;
import com.example.car.mapper.SysRoleMapper;
import com.example.car.mapper.SysUserMapper;
import com.example.car.mapper.SysUserRoleMapper;
import com.example.car.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 系统用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser>
        implements SysUserService {

    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final SysDeptMapper sysDeptMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    /** Redis 中存储已登出 Token 的黑名单前缀 */
    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getUsername(), loginDTO.getPassword()));
        } catch (DisabledException e) {
            throw new BusinessException("账号已被禁用");
        } catch (BadCredentialsException e) {
            throw new BusinessException("用户名或密码错误");
        }

        String username = authentication.getName();
        String token = jwtUtil.generateToken(username);

        SysUser sysUser = getByUsername(username);

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setExpiresIn(jwtExpiration);
        vo.setUsername(username);
        if (sysUser != null) {
            vo.setRealName(sysUser.getRealName());
            vo.setAvatar(sysUser.getAvatar());
            // Return the canonical role from sys_user.role (same source used by
            // UserDetailsServiceImpl) so frontend display matches security checks.
            String role = sysUser.getRole();
            vo.setRole((role != null && !role.isBlank()) ? role.toUpperCase() : "USER");
        }
        return vo;
    }

    @Override
    public void logout(String token) {
        if (StringUtils.hasText(token)) {
            long remainSeconds = jwtUtil.getTokenRemainingSeconds(token);
            if (remainSeconds > 0) {
                redisUtil.set(TOKEN_BLACKLIST_PREFIX + token, "1", remainSeconds);
            }
        }
    }

    @Override
    public SysUser getByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        return this.getOne(wrapper);
    }

    @Override
    public Page<SysUserVO> pageQuery(SysUserQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getUsername()), 
                    SysUser::getUsername, queryDTO.getUsername())
               .like(StringUtils.hasText(queryDTO.getRealName()), 
                    SysUser::getRealName, queryDTO.getRealName())
               .like(StringUtils.hasText(queryDTO.getPhone()), 
                    SysUser::getPhone, queryDTO.getPhone())
               .eq(queryDTO.getDeptId() != null, 
                    SysUser::getDeptId, queryDTO.getDeptId())
               .eq(queryDTO.getStatus() != null, 
                    SysUser::getStatus, queryDTO.getStatus())
               .orderByDesc(SysUser::getCreateTime);
        
        // 分页查询
        Page<SysUser> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page = this.page(page, wrapper);
        
        // 转换为VO
        Page<SysUserVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(this::convertToVO)
                .toList());
        
        return voPage;
    }
    
    @Override
    public SysUserVO getDetailById(Long id) {
        SysUser sysUser = this.getById(id);
        if (sysUser == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToVO(sysUser);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addUser(SysUser sysUser) {
        // 校验用户名是否已存在
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, sysUser.getUsername());
        if (this.count(wrapper) > 0) {
            throw new BusinessException("用户名已存在");
        }
        
        // 校验手机号是否已存在
        if (StringUtils.hasText(sysUser.getPhone())) {
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getPhone, sysUser.getPhone());
            if (this.count(wrapper) > 0) {
                throw new BusinessException("手机号已存在");
            }
        }
        
        sysUser.setPassword(passwordEncoder.encode(sysUser.getPassword()));

        // 设置默认状态
        if (sysUser.getStatus() == null) {
            sysUser.setStatus(Constants.UserStatus.ENABLED);
        }
        
        return this.save(sysUser);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(SysUser sysUser) {
        // 校验用户是否存在
        SysUser existing = this.getById(sysUser.getId());
        if (existing == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 如果修改了用户名，需要校验新用户名是否已存在
        if (StringUtils.hasText(sysUser.getUsername()) && 
            !existing.getUsername().equals(sysUser.getUsername())) {
            LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getUsername, sysUser.getUsername())
                   .ne(SysUser::getId, sysUser.getId());
            if (this.count(wrapper) > 0) {
                throw new BusinessException("用户名已存在");
            }
        }
        
        // 如果修改了手机号，需要校验新手机号是否已存在
        if (StringUtils.hasText(sysUser.getPhone()) && 
            !existing.getPhone().equals(sysUser.getPhone())) {
            LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysUser::getPhone, sysUser.getPhone())
                   .ne(SysUser::getId, sysUser.getId());
            if (this.count(wrapper) > 0) {
                throw new BusinessException("手机号已存在");
            }
        }
        
        // 不允许通过此接口修改密码
        sysUser.setPassword(null);
        
        return this.updateById(sysUser);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long id) {
        // 校验用户是否存在
        SysUser sysUser = this.getById(id);
        if (sysUser == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 不允许删除admin用户
        if ("admin".equals(sysUser.getUsername())) {
            throw new BusinessException("不允许删除管理员账号");
        }
        
        // 使用物理删除（从数据库中真正删除）
        return this.baseMapper.deleteById(id) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(Long id, String newPassword) {
        // 校验用户是否存在
        SysUser sysUser = this.getById(id);
        if (sysUser == null) {
            throw new BusinessException("用户不存在");
        }
        
        sysUser.setPassword(passwordEncoder.encode(newPassword));
        return this.updateById(sysUser);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changeStatus(Long id, Integer status) {
        // 校验用户是否存在
        SysUser sysUser = this.getById(id);
        if (sysUser == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 不允许禁用admin用户
        if ("admin".equals(sysUser.getUsername()) && Constants.UserStatus.DISABLED.equals(status)) {
            throw new BusinessException("不允许禁用管理员账号");
        }
        
        sysUser.setStatus(status);
        return this.updateById(sysUser);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(String username, String oldPassword, String newPassword) {
        SysUser sysUser = getByUsername(username);
        if (sysUser == null) {
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, sysUser.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        sysUser.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(sysUser);
    }

    /**
     * Convert entity to VO.
     *
     * Role consistency fix (B6):
     *  - vo.role  is set from sys_user.role  — the same column that
     *    UserDetailsServiceImpl uses for Spring Security authorities.
     *  - vo.roles is populated from the sys_user_role / sys_role relation tables
     *    for display purposes (legacy; may be empty).
     *
     * Having both fields in the VO lets administrators spot discrepancies
     * between the auth role and any historic role-relation data.
     */
    private SysUserVO convertToVO(SysUser sysUser) {
        SysUserVO vo = new SysUserVO();
        BeanUtils.copyProperties(sysUser, vo);

        // Status description
        if (sysUser.getStatus() != null) {
            vo.setStatusDesc(switch (sysUser.getStatus()) {
                case 0 -> "禁用";
                case 1 -> "启用";
                default -> "未知";
            });
        }

        // Department name
        if (sysUser.getDeptId() != null) {
            SysDept dept = sysDeptMapper.selectById(sysUser.getDeptId());
            if (dept != null) {
                vo.setDeptName(dept.getDeptName());
            }
        }

        // Authoritative auth role — same source as UserDetailsServiceImpl
        String authRole = sysUser.getRole();
        vo.setRole((authRole != null && !authRole.isBlank()) ? authRole.toUpperCase() : "USER");

        // Display roles from relation tables (legacy / optional)
        LambdaQueryWrapper<SysUserRole> urWrapper = new LambdaQueryWrapper<>();
        urWrapper.eq(SysUserRole::getUserId, sysUser.getId());
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(urWrapper);
        if (!userRoles.isEmpty()) {
            List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).toList();
            LambdaQueryWrapper<SysRole> roleWrapper = new LambdaQueryWrapper<>();
            roleWrapper.in(SysRole::getId, roleIds);
            List<SysRole> sysRoles = sysRoleMapper.selectList(roleWrapper);
            vo.setRoles(sysRoles.stream().map(SysRole::getRoleKey).toList());
        }

        return vo;
    }
}
