package com.example.car.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.car.common.exception.BusinessException;
import com.example.car.entity.GeoFence;
import com.example.car.entity.dto.GeoFenceQueryDTO;
import com.example.car.entity.vo.GeoFenceVO;
import com.example.car.mapper.GeoFenceMapper;
import com.example.car.service.GeoFenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class GeoFenceServiceImpl extends ServiceImpl<GeoFenceMapper, GeoFence> 
        implements GeoFenceService {
    
    @Override
    public Page<GeoFenceVO> pageQuery(GeoFenceQueryDTO queryDTO) {
        LambdaQueryWrapper<GeoFence> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getFenceName()), 
                    GeoFence::getFenceName, queryDTO.getFenceName())
               .eq(queryDTO.getFenceType() != null, 
                    GeoFence::getFenceType, queryDTO.getFenceType())
               .eq(queryDTO.getStatus() != null, 
                    GeoFence::getStatus, queryDTO.getStatus())
               .orderByDesc(GeoFence::getCreateTime);
        
        Page<GeoFence> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page = this.page(page, wrapper);
        
        Page<GeoFenceVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(this::convertToVO)
                .toList());
        
        return voPage;
    }
    
    @Override
    public GeoFenceVO getDetailById(Long id) {
        GeoFence geoFence = this.getById(id);
        if (geoFence == null) {
            throw new BusinessException("围栏不存在");
        }
        return convertToVO(geoFence);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addFence(GeoFence geoFence) {
        return this.save(geoFence);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateFence(GeoFence geoFence) {
        GeoFence existing = this.getById(geoFence.getId());
        if (existing == null) {
            throw new BusinessException("围栏不存在");
        }
        return this.updateById(geoFence);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFence(Long id) {
        GeoFence geoFence = this.getById(id);
        if (geoFence == null) {
            throw new BusinessException("围栏不存在");
        }
        // 使用物理删除（从数据库中真正删除）
        return this.baseMapper.deleteById(id) > 0;
    }
    
    private GeoFenceVO convertToVO(GeoFence geoFence) {
        GeoFenceVO vo = new GeoFenceVO();
        BeanUtils.copyProperties(geoFence, vo);
        
        // 设置围栏类型描述
        switch (geoFence.getFenceType()) {
            case 1 -> vo.setFenceTypeDesc("圆形");
            case 2 -> vo.setFenceTypeDesc("多边形");
            case 3 -> vo.setFenceTypeDesc("矩形");
            default -> vo.setFenceTypeDesc("未知");
        }
        
        // 设置报警类型描述
        switch (geoFence.getAlarmType()) {
            case 1 -> vo.setAlarmTypeDesc("进入报警");
            case 2 -> vo.setAlarmTypeDesc("离开报警");
            case 3 -> vo.setAlarmTypeDesc("进出都报警");
            default -> vo.setAlarmTypeDesc("未知");
        }
        
        vo.setStatusDesc(geoFence.getStatus() == 1 ? "启用" : "禁用");
        
        return vo;
    }
}
