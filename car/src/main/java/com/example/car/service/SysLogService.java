package com.example.car.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.car.entity.SysLog;
import com.example.car.entity.dto.SysLogQueryDTO;
import com.example.car.entity.vo.SysLogVO;

public interface SysLogService extends IService<SysLog> {
    Page<SysLogVO> pageQuery(SysLogQueryDTO queryDTO);
    SysLogVO getDetailById(Long id);
}
