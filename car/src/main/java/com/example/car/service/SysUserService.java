package com.example.car.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.car.entity.SysUser;
import com.example.car.entity.dto.LoginDTO;
import com.example.car.entity.dto.SysUserQueryDTO;
import com.example.car.entity.vo.LoginVO;
import com.example.car.entity.vo.SysUserVO;

/**
 * 系统用户服务接口
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 用户登录
     * @param loginDTO 登录参数
     * @return 登录响应（含Token）
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 用户登出（使Token失效）
     * @param token 当前Token
     */
    void logout(String token);

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    SysUser getByUsername(String username);

    /**
     * 分页查询用户列表
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    Page<SysUserVO> pageQuery(SysUserQueryDTO queryDTO);
    
    /**
     * 根据ID查询用户详情
     * @param id 用户ID
     * @return 用户详情
     */
    SysUserVO getDetailById(Long id);
    
    /**
     * 新增用户
     * @param sysUser 用户信息
     * @return 是否成功
     */
    boolean addUser(SysUser sysUser);
    
    /**
     * 更新用户信息
     * @param sysUser 用户信息
     * @return 是否成功
     */
    boolean updateUser(SysUser sysUser);
    
    /**
     * 删除用户（逻辑删除）
     * @param id 用户ID
     * @return 是否成功
     */
    boolean deleteUser(Long id);
    
    /**
     * 重置密码
     * @param id 用户ID
     * @param newPassword 新密码
     * @return 是否成功
     */
    boolean resetPassword(Long id, String newPassword);
    
    /**
     * 修改用户状态
     * @param id 用户ID
     * @param status 状态
     * @return 是否成功
     */
    boolean changeStatus(Long id, Integer status);

    /**
     * 修改当前登录用户自己的密码
     * @param username    当前用户名
     * @param oldPassword 原密码（用于二次验证）
     * @param newPassword 新密码
     */
    void updatePassword(String username, String oldPassword, String newPassword);
}
