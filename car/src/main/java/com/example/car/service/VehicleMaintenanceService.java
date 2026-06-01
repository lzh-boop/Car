package com.example.car.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.car.entity.VehicleMaintenance;
import com.example.car.entity.dto.VehicleMaintenanceQueryDTO;
import com.example.car.entity.vo.VehicleMaintenanceVO;

public interface VehicleMaintenanceService extends IService<VehicleMaintenance> {
    Page<VehicleMaintenanceVO> pageQuery(VehicleMaintenanceQueryDTO queryDTO);
    boolean addMaintenance(VehicleMaintenance vehicleMaintenance);
    boolean updateMaintenance(VehicleMaintenance vehicleMaintenance);
    boolean deleteMaintenance(String vehicleNo);
    /** 按主键 id 删除单条记录（前端一车一档使用） */
    boolean deleteMaintenanceById(Long id);
    VehicleMaintenanceVO getMaintenanceByVehicleNo(String vehicleNo);
}
