package com.example.car.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.car.entity.VehicleInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 车辆信息 Mapper
 */
@Mapper
public interface VehicleInfoMapper extends BaseMapper<VehicleInfo> {
    
}
