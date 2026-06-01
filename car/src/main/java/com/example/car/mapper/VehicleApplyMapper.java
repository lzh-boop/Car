package com.example.car.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.car.entity.VehicleApply;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用车申请 Mapper
 */
@Mapper
public interface VehicleApplyMapper extends BaseMapper<VehicleApply> {
    
}
