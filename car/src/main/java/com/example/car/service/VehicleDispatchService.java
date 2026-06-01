package com.example.car.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.car.entity.VehicleDispatch;
import com.example.car.entity.dto.VehicleDispatchQueryDTO;
import com.example.car.entity.vo.VehicleDispatchVO;

/**
 * 车辆调度服务接口
 */
public interface VehicleDispatchService extends IService<VehicleDispatch> {
    
    /**
     * 分页查询调度列表
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    Page<VehicleDispatchVO> pageQuery(VehicleDispatchQueryDTO queryDTO);
    
    /**
     * 根据ID查询调度详情
     * @param id 调度ID
     * @return 调度详情
     */
    VehicleDispatchVO getDetailById(Long id);
    
    /**
     * 创建调度单
     * @param vehicleDispatch 调度信息
     * @return 是否成功
     */
    boolean createDispatch(VehicleDispatch vehicleDispatch);
    
    /**
     * 更新调度单
     * @param vehicleDispatch 调度信息
     * @return 是否成功
     */
    boolean updateDispatch(VehicleDispatch vehicleDispatch);
    
    /**
     * 开始出车
     * @param id 调度ID
     * @return 是否成功
     */
    boolean startDispatch(Long id);
    
    /**
     * 完成调度
     * @param id 调度ID
     * @return 是否成功
     */
    boolean completeDispatch(Long id);
    
    /**
     * 取消调度
     * @param id 调度ID
     * @return 是否成功
     */
    boolean cancelDispatch(Long id);
    
    /**
     * 删除调度（逻辑删除）
     * @param id 调度ID
     * @return 是否成功
     */
    boolean deleteDispatch(Long id);
}
