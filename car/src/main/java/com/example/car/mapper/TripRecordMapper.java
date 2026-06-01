package com.example.car.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.car.entity.TripRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TripRecordMapper extends BaseMapper<TripRecord> {
}
