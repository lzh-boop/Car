package com.example.car.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.car.entity.DriverInfo;
import com.example.car.entity.dto.DriverQueryDTO;
import com.example.car.entity.vo.DriverVO;

/**
 * 驾驶员信息服务接口
 */
public interface DriverInfoService extends IService<DriverInfo> {
    
    /**
     * 分页查询驾驶员列表
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    Page<DriverVO> pageQuery(DriverQueryDTO queryDTO);
    
    /**
     * 根据ID查询驾驶员详情
     * @param id 驾驶员ID
     * @return 驾驶员详情
     */
    DriverVO getDetailById(Long id);
    
    /**
     * 新增驾驶员
     * @param driverInfo 驾驶员信息
     * @return 是否成功
     */
    boolean addDriver(DriverInfo driverInfo);
    
    /**
     * 更新驾驶员信息
     * @param driverInfo 驾驶员信息
     * @return 是否成功
     */
    boolean updateDriver(DriverInfo driverInfo);
    
    /**
     * 删除驾驶员（逻辑删除）
     * @param id 驾驶员ID
     * @return 是否成功
     */
    boolean deleteDriver(Long id);
}
