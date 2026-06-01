package com.example.car.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.car.common.exception.BusinessException;
import com.example.car.entity.FenceAlarm;
import com.example.car.entity.dto.FenceAlarmQueryDTO;
import com.example.car.entity.vo.FenceAlarmVO;
import com.example.car.mapper.FenceAlarmMapper;
import com.example.car.service.FenceAlarmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Slf4j
@Service
public class FenceAlarmServiceImpl extends ServiceImpl<FenceAlarmMapper, FenceAlarm> 
        implements FenceAlarmService {
    
    @Override
    public Page<FenceAlarmVO> pageQuery(FenceAlarmQueryDTO queryDTO) {
        LambdaQueryWrapper<FenceAlarm> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getFenceId() != null, 
                    FenceAlarm::getFenceId, queryDTO.getFenceId())
               .like(StringUtils.hasText(queryDTO.getFenceName()), 
                    FenceAlarm::getFenceName, queryDTO.getFenceName())
               .eq(queryDTO.getVehicleId() != null, 
                    FenceAlarm::getVehicleId, queryDTO.getVehicleId())
               .like(StringUtils.hasText(queryDTO.getVehicleNo()), 
                    FenceAlarm::getVehicleNo, queryDTO.getVehicleNo())
               .eq(queryDTO.getAlarmType() != null, 
                    FenceAlarm::getAlarmType, queryDTO.getAlarmType())
               .eq(queryDTO.getIsHandled() != null, 
                    FenceAlarm::getIsHandled, queryDTO.getIsHandled())
               .ge(queryDTO.getStartTime() != null, 
                    FenceAlarm::getAlarmTime, queryDTO.getStartTime())
               .le(queryDTO.getEndTime() != null, 
                    FenceAlarm::getAlarmTime, queryDTO.getEndTime())
               .orderByDesc(FenceAlarm::getAlarmTime);
        
        Page<FenceAlarm> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page = this.page(page, wrapper);
        
        Page<FenceAlarmVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(this::convertToVO)
                .toList());
        
        return voPage;
    }
    
    @Override
    public FenceAlarmVO getDetailById(Long id) {
        FenceAlarm fenceAlarm = this.getById(id);
        if (fenceAlarm == null) {
            throw new BusinessException("报警记录不存在");
        }
        return convertToVO(fenceAlarm);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleAlarm(Long id, Long handlerId, String handleRemark) {
        FenceAlarm fenceAlarm = this.getById(id);
        if (fenceAlarm == null) {
            throw new BusinessException("报警记录不存在");
        }
        
        if (fenceAlarm.getIsHandled() == 1) {
            throw new BusinessException("该报警已处理");
        }
        
        fenceAlarm.setIsHandled(1);
        fenceAlarm.setHandlerId(handlerId);
        fenceAlarm.setHandleTime(LocalDateTime.now());
        fenceAlarm.setHandleRemark(handleRemark);
        
        return this.updateById(fenceAlarm);
    }
    
    private FenceAlarmVO convertToVO(FenceAlarm fenceAlarm) {
        FenceAlarmVO vo = new FenceAlarmVO();
        BeanUtils.copyProperties(fenceAlarm, vo);
        
        vo.setAlarmTypeDesc(fenceAlarm.getAlarmType() == 1 ? "进入" : "离开");
        vo.setIsHandledDesc(fenceAlarm.getIsHandled() == 1 ? "已处理" : "未处理");
        
        return vo;
    }
}
