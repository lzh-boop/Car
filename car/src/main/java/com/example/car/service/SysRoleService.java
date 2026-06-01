package com.example.car.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.car.entity.SysRole;
import com.example.car.entity.dto.SysRoleQueryDTO;
import com.example.car.entity.vo.SysRoleVO;

/**
 * 系统角色服务接口
 */
public interface SysRoleService extends IService<SysRole> {
    
    /**
     * 分页查询角色列表
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    Page<SysRoleVO> pageQuery(SysRoleQueryDTO queryDTO);
    
    /**
     * 根据ID查询角色详情
     * @param id 角色ID
     * @return 角色详情
     */
    SysRoleVO getDetailById(Long id);
    
    /**
     * 新增角色
     * @param sysRole 角色信息
     * @return 是否成功
     */
    boolean addRole(SysRole sysRole);
    
    /**
     * 更新角色信息
     * @param sysRole 角色信息
     * @return 是否成功
     */
    boolean updateRole(SysRole sysRole);
    
    /**
     * 删除角色（逻辑删除）
     * @param id 角色ID
     * @return 是否成功
     */
    boolean deleteRole(Long id);
}
