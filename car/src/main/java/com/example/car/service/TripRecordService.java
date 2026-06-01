package com.example.car.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.car.entity.TripRecord;
import com.example.car.entity.dto.TripRecordQueryDTO;
import com.example.car.entity.vo.TripRecordVO;

import java.math.BigDecimal;

public interface TripRecordService extends IService<TripRecord> {
    Page<TripRecordVO> pageQuery(TripRecordQueryDTO queryDTO);
    TripRecordVO getDetailById(Long id);
    boolean addTrip(TripRecord tripRecord);
    boolean updateTrip(TripRecord tripRecord);
    boolean endTrip(Long id, BigDecimal endMileage, BigDecimal fuelConsumption);
    boolean deleteTrip(Long id);
}
