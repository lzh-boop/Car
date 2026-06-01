package com.example.car.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.car.common.constant.Constants;
import com.example.car.common.exception.BusinessException;
import com.example.car.entity.TripRecord;
import com.example.car.entity.dto.TripRecordQueryDTO;
import com.example.car.entity.vo.TripRecordVO;
import com.example.car.mapper.TripRecordMapper;
import com.example.car.service.TripRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
public class TripRecordServiceImpl extends ServiceImpl<TripRecordMapper, TripRecord> 
        implements TripRecordService {
    
    @Override
    public Page<TripRecordVO> pageQuery(TripRecordQueryDTO queryDTO) {
        LambdaQueryWrapper<TripRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getVehicleId() != null, 
                    TripRecord::getVehicleId, queryDTO.getVehicleId())
               .like(StringUtils.hasText(queryDTO.getVehicleNo()), 
                    TripRecord::getVehicleNo, queryDTO.getVehicleNo())
               .eq(queryDTO.getDriverId() != null, 
                    TripRecord::getDriverId, queryDTO.getDriverId())
               .like(StringUtils.hasText(queryDTO.getDriverName()), 
                    TripRecord::getDriverName, queryDTO.getDriverName())
               .eq(queryDTO.getTripStatus() != null, 
                    TripRecord::getTripStatus, queryDTO.getTripStatus())
               .ge(queryDTO.getStartTime() != null, 
                    TripRecord::getStartTime, queryDTO.getStartTime())
               .le(queryDTO.getEndTime() != null, 
                    TripRecord::getEndTime, queryDTO.getEndTime())
               .orderByDesc(TripRecord::getStartTime);
        
        Page<TripRecord> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page = this.page(page, wrapper);
        
        Page<TripRecordVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(this::convertToVO)
                .toList());
        
        return voPage;
    }
    
    @Override
    public TripRecordVO getDetailById(Long id) {
        TripRecord tripRecord = this.getById(id);
        if (tripRecord == null) {
            throw new BusinessException("行程记录不存在");
        }
        return convertToVO(tripRecord);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addTrip(TripRecord tripRecord) {
        tripRecord.setTripStatus(Constants.TripStatus.RUNNING);
        return this.save(tripRecord);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTrip(TripRecord tripRecord) {
        TripRecord existing = this.getById(tripRecord.getId());
        if (existing == null) {
            throw new BusinessException("行程记录不存在");
        }
        return this.updateById(tripRecord);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean endTrip(Long id, BigDecimal endMileage, BigDecimal fuelConsumption) {
        TripRecord tripRecord = this.getById(id);
        if (tripRecord == null) {
            throw new BusinessException("行程记录不存在");
        }
        
        if (!Constants.TripStatus.RUNNING.equals(tripRecord.getTripStatus())) {
            throw new BusinessException("只有进行中的行程才能结束");
        }
        
        tripRecord.setEndTime(LocalDateTime.now());
        tripRecord.setEndMileage(endMileage);
        tripRecord.setFuelConsumption(fuelConsumption);
        
        if (tripRecord.getStartMileage() != null && endMileage != null) {
            tripRecord.setTripDistance(endMileage.subtract(tripRecord.getStartMileage()));
        }
        
        tripRecord.setTripStatus(Constants.TripStatus.ENDED);
        return this.updateById(tripRecord);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTrip(Long id) {
        TripRecord tripRecord = this.getById(id);
        if (tripRecord == null) {
            throw new BusinessException("行程记录不存在");
        }
        // 使用物理删除（从数据库中真正删除）
        return this.baseMapper.deleteById(id) > 0;
    }
    
    private TripRecordVO convertToVO(TripRecord tripRecord) {
        TripRecordVO vo = new TripRecordVO();
        BeanUtils.copyProperties(tripRecord, vo);
        vo.setTripStatusDesc(tripRecord.getTripStatus() == 1 ? "进行中" : "已结束");
        return vo;
    }
}
