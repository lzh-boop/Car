package com.example.car.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.car.common.exception.BusinessException;
import com.example.car.entity.SysDept;
import com.example.car.entity.dto.SysDeptQueryDTO;
import com.example.car.entity.vo.SysDeptVO;
import com.example.car.mapper.SysDeptMapper;
import com.example.car.service.SysDeptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> 
        implements SysDeptService {
    
    @Override
    public Page<SysDeptVO> pageQuery(SysDeptQueryDTO queryDTO) {
        LambdaQueryWrapper<SysDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getDeptName()), 
                    SysDept::getDeptName, queryDTO.getDeptName())
               .eq(queryDTO.getParentId() != null, 
                    SysDept::getParentId, queryDTO.getParentId())
               .eq(queryDTO.getStatus() != null, 
                    SysDept::getStatus, queryDTO.getStatus())
               .orderByAsc(SysDept::getOrderNum);
        
        Page<SysDept> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page = this.page(page, wrapper);
        
        Page<SysDeptVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(this::convertToVO)
                .toList());
        
        return voPage;
    }
    
    @Override
    public SysDeptVO getDetailById(Long id) {
        SysDept sysDept = this.getById(id);
        if (sysDept == null) {
            throw new BusinessException("部门不存在");
        }
        return convertToVO(sysDept);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addDept(SysDept sysDept) {
        return this.save(sysDept);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDept(SysDept sysDept) {
        SysDept existing = this.getById(sysDept.getId());
        if (existing == null) {
            throw new BusinessException("部门不存在");
        }
        return this.updateById(sysDept);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDept(Long id) {
        SysDept sysDept = this.getById(id);
        if (sysDept == null) {
            throw new BusinessException("部门不存在");
        }
        // 使用物理删除（从数据库中真正删除）
        return this.baseMapper.deleteById(id) > 0;
    }
    
    private SysDeptVO convertToVO(SysDept sysDept) {
        SysDeptVO vo = new SysDeptVO();
        BeanUtils.copyProperties(sysDept, vo);
        vo.setStatusDesc(sysDept.getStatus() == 1 ? "启用" : "禁用");
        return vo;
    }
}
