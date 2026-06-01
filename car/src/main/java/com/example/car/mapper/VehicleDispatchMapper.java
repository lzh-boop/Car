package com.example.car.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.car.entity.VehicleDispatch;
import org.apache.ibatis.annotations.Mapper;

/**
 * 车辆调度 Mapper
 */
@Mapper
public interface VehicleDispatchMapper extends BaseMapper<VehicleDispatch> {
    
}
