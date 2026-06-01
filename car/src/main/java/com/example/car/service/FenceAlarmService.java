package com.example.car.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.car.entity.FenceAlarm;
import com.example.car.entity.dto.FenceAlarmQueryDTO;
import com.example.car.entity.vo.FenceAlarmVO;

public interface FenceAlarmService extends IService<FenceAlarm> {
    Page<FenceAlarmVO> pageQuery(FenceAlarmQueryDTO queryDTO);
    FenceAlarmVO getDetailById(Long id);
    boolean handleAlarm(Long id, Long handlerId, String handleRemark);
}
