package com.example.car.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.car.common.constant.Constants;
import com.example.car.common.exception.BusinessException;
import com.example.car.entity.DriverInfo;
import com.example.car.entity.VehicleDispatch;
import com.example.car.entity.dto.DriverQueryDTO;
import com.example.car.entity.vo.DriverVO;
import com.example.car.mapper.DriverInfoMapper;
import com.example.car.mapper.VehicleDispatchMapper;
import com.example.car.service.DriverInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 驾驶员信息服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DriverInfoServiceImpl extends ServiceImpl<DriverInfoMapper, DriverInfo>
        implements DriverInfoService {

    private final VehicleDispatchMapper vehicleDispatchMapper;
    
    @Override
    public Page<DriverVO> pageQuery(DriverQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<DriverInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getDriverName()), 
                    DriverInfo::getDriverName, queryDTO.getDriverName())
               .like(StringUtils.hasText(queryDTO.getPhone()), 
                    DriverInfo::getPhone, queryDTO.getPhone())
               .like(StringUtils.hasText(queryDTO.getLicenseNo()), 
                    DriverInfo::getLicenseNo, queryDTO.getLicenseNo())
               .eq(StringUtils.hasText(queryDTO.getLicenseType()), 
                    DriverInfo::getLicenseType, queryDTO.getLicenseType())
               .eq(queryDTO.getDeptId() != null, 
                    DriverInfo::getDeptId, queryDTO.getDeptId())
               .eq(queryDTO.getStatus() != null, 
                    DriverInfo::getStatus, queryDTO.getStatus())
               .orderByDesc(DriverInfo::getCreateTime);
        
        // 分页查询
        Page<DriverInfo> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page = this.page(page, wrapper);
        
        // 转换为VO
        Page<DriverVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(this::convertToVO)
                .toList());
        
        return voPage;
    }
    
    @Override
    public DriverVO getDetailById(Long id) {
        DriverInfo driverInfo = this.getById(id);
        if (driverInfo == null) {
            throw new BusinessException("驾驶员不存在");
        }
        return convertToVO(driverInfo);
    }
    
    @Override
    public boolean addDriver(DriverInfo driverInfo) {
        // 校验手机号是否已存在
        if (StringUtils.hasText(driverInfo.getPhone())) {
            LambdaQueryWrapper<DriverInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DriverInfo::getPhone, driverInfo.getPhone());
            if (this.count(wrapper) > 0) {
                throw new BusinessException("手机号已存在");
            }
        }
        
        // 校验驾驶证号是否已存在
        if (StringUtils.hasText(driverInfo.getLicenseNo())) {
            LambdaQueryWrapper<DriverInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DriverInfo::getLicenseNo, driverInfo.getLicenseNo());
            if (this.count(wrapper) > 0) {
                throw new BusinessException("驾驶证号已存在");
            }
        }
        
        return this.save(driverInfo);
    }
    
    @Override
    public boolean updateDriver(DriverInfo driverInfo) {
        // 校验驾驶员是否存在
        DriverInfo existing = this.getById(driverInfo.getId());
        if (existing == null) {
            throw new BusinessException("驾驶员不存在");
        }
        
        // 如果修改了手机号，需要校验新手机号是否已存在
        if (StringUtils.hasText(driverInfo.getPhone()) && 
            !existing.getPhone().equals(driverInfo.getPhone())) {
            LambdaQueryWrapper<DriverInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DriverInfo::getPhone, driverInfo.getPhone())
                   .ne(DriverInfo::getId, driverInfo.getId());
            if (this.count(wrapper) > 0) {
                throw new BusinessException("手机号已存在");
            }
        }
        
        return this.updateById(driverInfo);
    }
    
    @Override
    public boolean deleteDriver(Long id) {
        DriverInfo driverInfo = this.getById(id);
        if (driverInfo == null) throw new BusinessException("驾驶员不存在");

        // Bug 修复 B8：驾驶员有进行中调度任务（待出车/行驶中）时不允许删除
        long activeDispatches = vehicleDispatchMapper.selectCount(
                new LambdaQueryWrapper<VehicleDispatch>()
                        .eq(VehicleDispatch::getDriverId, id)
                        .in(VehicleDispatch::getDispatchStatus,
                                Constants.DispatchStatus.PENDING,
                                Constants.DispatchStatus.IN_PROGRESS));
        if (activeDispatches > 0) {
            throw new BusinessException(
                    "删除失败：该驾驶员仍有 " + activeDispatches + " 条进行中的调度任务，请先完成或取消相关调度");
        }

        return this.baseMapper.deleteById(id) > 0;
    }
    
    /**
     * 实体转VO
     */
    private DriverVO convertToVO(DriverInfo driverInfo) {
        DriverVO vo = new DriverVO();
        BeanUtils.copyProperties(driverInfo, vo);
        
        // 设置状态描述
        switch (driverInfo.getStatus()) {
            case 1 -> vo.setStatusDesc("正常");
            case 2 -> vo.setStatusDesc("停用");
            default -> vo.setStatusDesc("未知");
        }
        
        // TODO: 查询部门名称
        
        return vo;
    }
}
