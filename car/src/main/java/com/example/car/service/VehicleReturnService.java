package com.example.car.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.car.entity.VehicleReturn;
import com.example.car.entity.dto.VehicleReturnDoDTO;
import com.example.car.entity.dto.VehicleReturnQueryDTO;
import com.example.car.entity.vo.VehicleReturnVO;

public interface VehicleReturnService extends IService<VehicleReturn> {

    Page<VehicleReturnVO> pageQuery(VehicleReturnQueryDTO queryDTO);

    VehicleReturnVO getDetailById(Long id);

    /**
     * Perform a vehicle return.
     * Security: only the fields in VehicleReturnDoDTO (mileageAfter, fuelLevel,
     * vehicleCondition, remark) may be supplied by the client; linkage fields
     * (vehicleId, vehicleNo, dispatchId, dispatchNo, driverId, returnStatus)
     * are resolved server-side from the existing record.
     */
    boolean doReturn(VehicleReturnDoDTO dto);

    boolean deleteReturn(Long id);
}
