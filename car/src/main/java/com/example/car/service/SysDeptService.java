package com.example.car.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.car.entity.SysDept;
import com.example.car.entity.dto.SysDeptQueryDTO;
import com.example.car.entity.vo.SysDeptVO;

public interface SysDeptService extends IService<SysDept> {
    Page<SysDeptVO> pageQuery(SysDeptQueryDTO queryDTO);
    SysDeptVO getDetailById(Long id);
    boolean addDept(SysDept sysDept);
    boolean updateDept(SysDept sysDept);
    boolean deleteDept(Long id);
}
