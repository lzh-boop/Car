package com.example.car.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.car.common.exception.BusinessException;
import com.example.car.entity.SysLog;
import com.example.car.entity.dto.SysLogQueryDTO;
import com.example.car.entity.vo.SysLogVO;
import com.example.car.mapper.SysLogMapper;
import com.example.car.service.SysLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> 
        implements SysLogService {
    
    @Override
    public Page<SysLogVO> pageQuery(SysLogQueryDTO queryDTO) {
        LambdaQueryWrapper<SysLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getUserId() != null, 
                    SysLog::getUserId, queryDTO.getUserId())
               .like(StringUtils.hasText(queryDTO.getUsername()), 
                    SysLog::getUsername, queryDTO.getUsername())
               .like(StringUtils.hasText(queryDTO.getOperation()), 
                    SysLog::getOperation, queryDTO.getOperation())
               .ge(queryDTO.getStartTime() != null, 
                    SysLog::getCreateTime, queryDTO.getStartTime())
               .le(queryDTO.getEndTime() != null, 
                    SysLog::getCreateTime, queryDTO.getEndTime())
               .orderByDesc(SysLog::getCreateTime);
        
        Page<SysLog> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page = this.page(page, wrapper);
        
        Page<SysLogVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(this::convertToVO)
                .toList());
        
        return voPage;
    }
    
    @Override
    public SysLogVO getDetailById(Long id) {
        SysLog sysLog = this.getById(id);
        if (sysLog == null) {
            throw new BusinessException("日志不存在");
        }
        return convertToVO(sysLog);
    }
    
    private SysLogVO convertToVO(SysLog sysLog) {
        SysLogVO vo = new SysLogVO();
        BeanUtils.copyProperties(sysLog, vo);
        return vo;
    }
}
