package com.example.car.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.car.entity.VehicleInfo;
import com.example.car.entity.dto.VehicleQueryDTO;
import com.example.car.entity.vo.VehicleVO;

/**
 * 车辆信息服务接口
 */
public interface VehicleInfoService extends IService<VehicleInfo> {
    
    /**
     * 分页查询车辆列表
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    Page<VehicleVO> pageQuery(VehicleQueryDTO queryDTO);
    
    /**
     * 根据ID查询车辆详情
     * @param vehicleNo 车辆车牌号
     * @return 车辆详情
     */
    VehicleVO getVehicleByVehicleNo(String vehicleNo);
    
    /**
     * 新增车辆
     * @param vehicleInfo 车辆信息
     * @return 是否成功
     */
    boolean addVehicle(VehicleInfo vehicleInfo);
    
    /**
     * 更新车辆信息
     * @param vehicleInfo 车辆信息
     * @return 是否成功
     */
    boolean updateVehicle(VehicleInfo vehicleInfo);
    
    /**
     * 删除车辆（逻辑删除）
     * @param vehicleNo 车辆车牌号
     * @return 是否成功
     */
    boolean deleteVehicle(String vehicleNo);
}
