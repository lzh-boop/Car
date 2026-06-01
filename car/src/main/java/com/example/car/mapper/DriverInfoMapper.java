package com.example.car.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.car.entity.DriverInfo;
import org.apache.ibatis.annotations.Mapper;

/**
 * 驾驶员信息 Mapper
 */
@Mapper
public interface DriverInfoMapper extends BaseMapper<DriverInfo> {
    
}
