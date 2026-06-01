package com.example.car.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.car.entity.GeoFence;
import com.example.car.entity.dto.GeoFenceQueryDTO;
import com.example.car.entity.vo.GeoFenceVO;

public interface GeoFenceService extends IService<GeoFence> {
    Page<GeoFenceVO> pageQuery(GeoFenceQueryDTO queryDTO);
    GeoFenceVO getDetailById(Long id);
    boolean addFence(GeoFence geoFence);
    boolean updateFence(GeoFence geoFence);
    boolean deleteFence(Long id);
}
