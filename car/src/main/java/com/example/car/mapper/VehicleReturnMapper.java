package com.example.car.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.car.entity.VehicleReturn;
import org.apache.ibatis.annotations.Mapper;

/**
 * 还车记录 Mapper
 */
@Mapper
public interface VehicleReturnMapper extends BaseMapper<VehicleReturn> {
}
