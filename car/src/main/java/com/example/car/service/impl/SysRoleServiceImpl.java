package com.example.car.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.car.common.exception.BusinessException;
import com.example.car.entity.SysRole;
import com.example.car.entity.SysUserRole;
import com.example.car.entity.dto.SysRoleQueryDTO;
import com.example.car.entity.vo.SysRoleVO;
import com.example.car.mapper.SysRoleMapper;
import com.example.car.mapper.SysUserRoleMapper;
import com.example.car.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 系统角色服务实现类
 *
 * 安全修复（B7）：新增/修改角色时强制校验 roleKey 唯一性。
 * 重复的 roleKey 会导致权限语义歧义（例如两个不同权限的 "admin" 角色同时存在）。
 *
 * Bug 修复（B8 - 删除）：物理删除角色前校验是否仍有用户关联，
 * 防止 sys_user_role 产生孤立关联数据。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole>
        implements SysRoleService {

    private final SysUserRoleMapper sysUserRoleMapper;

    @Override
    public Page<SysRoleVO> pageQuery(SysRoleQueryDTO queryDTO) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getRoleKey()),
                        SysRole::getRoleKey, queryDTO.getRoleKey())
               .like(StringUtils.hasText(queryDTO.getRoleName()),
                        SysRole::getRoleName, queryDTO.getRoleName())
               .orderByAsc(SysRole::getSort);

        Page<SysRole> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page = this.page(page, wrapper);

        Page<SysRoleVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::convertToVO).toList());
        return voPage;
    }

    @Override
    public SysRoleVO getDetailById(Long id) {
        SysRole sysRole = this.getById(id);
        if (sysRole == null) throw new BusinessException("角色不存在");
        return convertToVO(sysRole);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addRole(SysRole sysRole) {
        // 安全修复 B7：强制校验 roleKey 唯一性，重复会导致权限语义歧义
        if (StringUtils.hasText(sysRole.getRoleKey())) {
            long count = this.count(new LambdaQueryWrapper<SysRole>()
                    .eq(SysRole::getRoleKey, sysRole.getRoleKey().trim()));
            if (count > 0) {
                throw new BusinessException("角色标识 [" + sysRole.getRoleKey() + "] 已存在，请使用其他标识");
            }
        }
        return this.save(sysRole);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateRole(SysRole sysRole) {
        SysRole existing = this.getById(sysRole.getId());
        if (existing == null) throw new BusinessException("角色不存在");

        // 安全修复 B7：若修改了 roleKey，校验新标识是否与其他角色重复
        if (StringUtils.hasText(sysRole.getRoleKey())
                && !sysRole.getRoleKey().trim().equals(existing.getRoleKey())) {
            long count = this.count(new LambdaQueryWrapper<SysRole>()
                    .eq(SysRole::getRoleKey, sysRole.getRoleKey().trim())
                    .ne(SysRole::getId, sysRole.getId()));
            if (count > 0) {
                throw new BusinessException("角色标识 [" + sysRole.getRoleKey() + "] 已存在，请使用其他标识");
            }
        }

        // 保留原创建时间
        sysRole.setCreateTime(existing.getCreateTime());
        return this.updateById(sysRole);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRole(Long id) {
        SysRole sysRole = this.getById(id);
        if (sysRole == null) throw new BusinessException("角色不存在");

        // 不允许删除 admin 角色
        if ("admin".equals(sysRole.getRoleKey())) {
            throw new BusinessException("不允许删除管理员角色");
        }

        // Bug 修复 B8：删除前校验是否仍有用户关联该角色，有则拒绝删除
        long assignedUsers = sysUserRoleMapper.selectCount(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getRoleId, id));
        if (assignedUsers > 0) {
            throw new BusinessException("删除失败：仍有 " + assignedUsers + " 个用户关联此角色，请先解除关联");
        }

        return this.baseMapper.deleteById(id) > 0;
    }

    /**
     * 实体转VO
     */
    private SysRoleVO convertToVO(SysRole sysRole) {
        SysRoleVO vo = new SysRoleVO();
        BeanUtils.copyProperties(sysRole, vo);
        vo.setStatusDesc(switch (sysRole.getStatus()) {
            case 0 -> "禁用";
            case 1 -> "启用";
            default -> "未知";
        });
        return vo;
    }
}
