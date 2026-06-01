package com.example.car.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.car.common.constant.Constants;
import com.example.car.common.exception.BusinessException;
import com.example.car.entity.VehicleDispatch;
import com.example.car.entity.VehicleInfo;
import com.example.car.entity.VehicleReturn;
import com.example.car.entity.dto.VehicleDispatchQueryDTO;
import com.example.car.entity.vo.VehicleDispatchVO;
import com.example.car.mapper.VehicleDispatchMapper;
import com.example.car.mapper.VehicleInfoMapper;
import com.example.car.mapper.VehicleReturnMapper;
import com.example.car.service.VehicleDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 车辆调度服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleDispatchServiceImpl extends ServiceImpl<VehicleDispatchMapper, VehicleDispatch> 
        implements VehicleDispatchService {

    private final VehicleInfoMapper vehicleInfoMapper;
    private final VehicleReturnMapper vehicleReturnMapper;
    
    @Override
    public Page<VehicleDispatchVO> pageQuery(VehicleDispatchQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<VehicleDispatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getDispatchNo()), 
                    VehicleDispatch::getDispatchNo, queryDTO.getDispatchNo())
               .like(StringUtils.hasText(queryDTO.getVehicleNo()), 
                    VehicleDispatch::getVehicleNo, queryDTO.getVehicleNo())
               .like(StringUtils.hasText(queryDTO.getDriverName()), 
                    VehicleDispatch::getDriverName, queryDTO.getDriverName())
               .eq(queryDTO.getVehicleId() != null, 
                    VehicleDispatch::getVehicleId, queryDTO.getVehicleId())
               .eq(queryDTO.getDriverId() != null, 
                    VehicleDispatch::getDriverId, queryDTO.getDriverId())
               .eq(queryDTO.getDispatchStatus() != null, 
                    VehicleDispatch::getDispatchStatus, queryDTO.getDispatchStatus())
               .ge(queryDTO.getStartTime() != null, 
                    VehicleDispatch::getPlanStartTime, queryDTO.getStartTime())
               .le(queryDTO.getEndTime() != null, 
                    VehicleDispatch::getPlanEndTime, queryDTO.getEndTime())
               .orderByDesc(VehicleDispatch::getCreateTime);
        
        // 分页查询
        Page<VehicleDispatch> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page = this.page(page, wrapper);
        
        // 转换为VO
        Page<VehicleDispatchVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(this::convertToVO)
                .toList());
        
        return voPage;
    }
    
    @Override
    public VehicleDispatchVO getDetailById(Long id) {
        VehicleDispatch vehicleDispatch = this.getById(id);
        if (vehicleDispatch == null) {
            throw new BusinessException("调度单不存在");
        }
        return convertToVO(vehicleDispatch);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createDispatch(VehicleDispatch vehicleDispatch) {
        // 生成调度单号
        vehicleDispatch.setDispatchNo(generateDispatchNo());

        // 设置初始状态为待出车
        vehicleDispatch.setDispatchStatus(Constants.DispatchStatus.PENDING);

        // 若前端未传 vehicleId，通过 vehicleNo 补查
        if (vehicleDispatch.getVehicleId() == null && StringUtils.hasText(vehicleDispatch.getVehicleNo())) {
            VehicleInfo vehicle = vehicleInfoMapper.selectOne(
                    new LambdaQueryWrapper<VehicleInfo>()
                            .eq(VehicleInfo::getVehicleNo, vehicleDispatch.getVehicleNo()));
            if (vehicle != null) {
                vehicleDispatch.setVehicleId(vehicle.getId());
            }
        }

        // 校验时间
        if (vehicleDispatch.getPlanStartTime() != null &&
            vehicleDispatch.getPlanEndTime() != null) {
            if (vehicleDispatch.getPlanStartTime().isAfter(vehicleDispatch.getPlanEndTime())) {
                throw new BusinessException("计划结束时间不能早于开始时间");
            }
        }

        return this.save(vehicleDispatch);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDispatch(VehicleDispatch vehicleDispatch) {
        // 校验调度单是否存在
        VehicleDispatch existing = this.getById(vehicleDispatch.getId());
        if (existing == null) {
            throw new BusinessException("调度单不存在");
        }
        
        // 只有待出车状态才允许修改
        if (!Constants.DispatchStatus.PENDING.equals(existing.getDispatchStatus())) {
            throw new BusinessException("只有待出车状态的调度单才能修改");
        }
        
        // 校验时间
        if (vehicleDispatch.getPlanStartTime() != null && 
            vehicleDispatch.getPlanEndTime() != null) {
            if (vehicleDispatch.getPlanStartTime().isAfter(vehicleDispatch.getPlanEndTime())) {
                throw new BusinessException("计划结束时间不能早于开始时间");
            }
        }
        
        boolean result = this.updateById(vehicleDispatch);

        // 同步更新关联还车记录的驾驶员姓名
        if (StringUtils.hasText(vehicleDispatch.getDriverName())) {
            VehicleReturn pendingReturn = vehicleReturnMapper.selectOne(
                    new LambdaQueryWrapper<VehicleReturn>()
                            .eq(VehicleReturn::getDispatchId, vehicleDispatch.getId())
                            .eq(VehicleReturn::getReturnStatus, 0));
            if (pendingReturn != null) {
                pendingReturn.setDriverId(vehicleDispatch.getDriverId());
                pendingReturn.setDriverName(vehicleDispatch.getDriverName());
                vehicleReturnMapper.updateById(pendingReturn);
            }
        }

        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean startDispatch(Long id) {
        // 校验调度单是否存在
        VehicleDispatch vehicleDispatch = this.getById(id);
        if (vehicleDispatch == null) {
            throw new BusinessException("调度单不存在");
        }
        
        // 只有待出车状态才能开始
        if (!Constants.DispatchStatus.PENDING.equals(vehicleDispatch.getDispatchStatus())) {
            throw new BusinessException("只有待出车状态的调度单才能开始");
        }

        // 校验驾驶员姓名不能为空（driverId 为 null 但 driverName 有值时，表示申请人即驾驶员，允许出车）
        if (!org.springframework.util.StringUtils.hasText(vehicleDispatch.getDriverName())) {
            throw new BusinessException("请先为该调度单分配驾驶员再出车");
        }

        // 更新状态和实际开始时间
        vehicleDispatch.setDispatchStatus(Constants.DispatchStatus.IN_PROGRESS);
        vehicleDispatch.setActualStartTime(LocalDateTime.now());
        
        return this.updateById(vehicleDispatch);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeDispatch(Long id) {
        // 校验调度单是否存在
        VehicleDispatch vehicleDispatch = this.getById(id);
        if (vehicleDispatch == null) {
            throw new BusinessException("调度单不存在");
        }
        
        // 只有行驶中状态才能完成
        if (!Constants.DispatchStatus.IN_PROGRESS.equals(vehicleDispatch.getDispatchStatus())) {
            throw new BusinessException("只有行驶中状态的调度单才能完成");
        }
        
        // 更新状态和实际结束时间
        vehicleDispatch.setDispatchStatus(Constants.DispatchStatus.COMPLETED);
        vehicleDispatch.setActualEndTime(LocalDateTime.now());
        
        return this.updateById(vehicleDispatch);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelDispatch(Long id) {
        // 校验调度单是否存在
        VehicleDispatch vehicleDispatch = this.getById(id);
        if (vehicleDispatch == null) {
            throw new BusinessException("调度单不存在");
        }
        
        // 已完成或已取消的不能再次取消
        if (Constants.DispatchStatus.COMPLETED.equals(vehicleDispatch.getDispatchStatus()) ||
            Constants.DispatchStatus.CANCELLED.equals(vehicleDispatch.getDispatchStatus())) {
            throw new BusinessException("该调度单已完成或已取消");
        }
        
        // 更新状态为已取消
        vehicleDispatch.setDispatchStatus(Constants.DispatchStatus.CANCELLED);
        boolean result = this.updateById(vehicleDispatch);

        // 将车辆状态恢复为空闲
        if (vehicleDispatch.getVehicleNo() != null) {
            VehicleInfo vehicle = vehicleInfoMapper.selectOne(
                    new LambdaQueryWrapper<VehicleInfo>()
                            .eq(VehicleInfo::getVehicleNo, vehicleDispatch.getVehicleNo()));
            if (vehicle != null && Constants.VehicleStatus.IN_USE.equals(vehicle.getStatus())) {
                vehicle.setStatus(Constants.VehicleStatus.FREE);
                vehicleInfoMapper.updateById(vehicle);
            }
        }

        // 将关联的待还车记录标记为已取消（returnStatus=2）
        VehicleReturn pendingReturn = vehicleReturnMapper.selectOne(
                new LambdaQueryWrapper<VehicleReturn>()
                        .eq(VehicleReturn::getDispatchId, id)
                        .eq(VehicleReturn::getReturnStatus, 0));
        if (pendingReturn != null) {
            pendingReturn.setReturnStatus(2);
            vehicleReturnMapper.updateById(pendingReturn);
        }

        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteDispatch(Long id) {
        // 校验调度单是否存在
        VehicleDispatch vehicleDispatch = this.getById(id);
        if (vehicleDispatch == null) {
            throw new BusinessException("调度单不存在");
        }
        
        // 使用物理删除（从数据库中真正删除）
        return this.baseMapper.deleteById(id) > 0;
    }
    
    /**
     * 生成调度单号
     * 格式：DISPATCH + yyyyMMddHHmmss + 4位随机数
     */
    private String generateDispatchNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = (int) (Math.random() * 9000) + 1000;
        return "DISPATCH" + timestamp + random;
    }
    
    /**
     * 实体转VO
     */
    private VehicleDispatchVO convertToVO(VehicleDispatch vehicleDispatch) {
        VehicleDispatchVO vo = new VehicleDispatchVO();
        BeanUtils.copyProperties(vehicleDispatch, vo);

        // 填充车辆类型和品牌
        if (vehicleDispatch.getVehicleId() != null) {
            VehicleInfo vehicle = vehicleInfoMapper.selectById(vehicleDispatch.getVehicleId());
            if (vehicle != null) {
                vo.setVehicleType(vehicle.getVehicleType());
                vo.setBrand(vehicle.getBrand());
            }
        } else if (org.springframework.util.StringUtils.hasText(vehicleDispatch.getVehicleNo())) {
            VehicleInfo vehicle = vehicleInfoMapper.selectOne(
                    new LambdaQueryWrapper<VehicleInfo>()
                            .eq(VehicleInfo::getVehicleNo, vehicleDispatch.getVehicleNo()));
            if (vehicle != null) {
                vo.setVehicleType(vehicle.getVehicleType());
                vo.setBrand(vehicle.getBrand());
            }
        }

        // 设置状态描述
        Integer status = vehicleDispatch.getDispatchStatus();
        if (status == null) status = 0;
        switch (status) {
            case 0 -> vo.setDispatchStatusDesc("待出车");
            case 1 -> vo.setDispatchStatusDesc("行驶中");
            case 2 -> vo.setDispatchStatusDesc("已完成");
            case 3 -> vo.setDispatchStatusDesc("已取消");
            default -> vo.setDispatchStatusDesc("未知");
        }
        
        return vo;
    }
}
