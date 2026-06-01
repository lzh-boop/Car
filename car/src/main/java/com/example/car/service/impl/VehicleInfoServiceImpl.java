package com.example.car.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.car.common.constant.Constants;
import com.example.car.common.exception.BusinessException;
import com.example.car.entity.VehicleApply;
import com.example.car.entity.VehicleDispatch;
import com.example.car.entity.VehicleInfo;
import com.example.car.entity.dto.VehicleQueryDTO;
import com.example.car.entity.vo.VehicleVO;
import com.example.car.entity.BeidouLocation;
import com.example.car.entity.BeidouTrackHistory;
import com.example.car.entity.FenceAlarm;
import com.example.car.entity.TripRecord;
import com.example.car.mapper.BeidouLocationMapper;
import com.example.car.mapper.BeidouTrackHistoryMapper;
import com.example.car.mapper.FenceAlarmMapper;
import com.example.car.mapper.TripRecordMapper;
import com.example.car.mapper.VehicleApplyMapper;
import com.example.car.mapper.VehicleDispatchMapper;
import com.example.car.mapper.VehicleInfoMapper;
import com.example.car.service.VehicleInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 车辆信息服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleInfoServiceImpl extends ServiceImpl<VehicleInfoMapper, VehicleInfo>
        implements VehicleInfoService {

    private final VehicleDispatchMapper vehicleDispatchMapper;
    private final VehicleApplyMapper vehicleApplyMapper;
    private final BeidouLocationMapper beidouLocationMapper;
    private final BeidouTrackHistoryMapper beidouTrackHistoryMapper;
    private final FenceAlarmMapper fenceAlarmMapper;
    private final TripRecordMapper tripRecordMapper;
    
    @Override
    public Page<VehicleVO> pageQuery(VehicleQueryDTO queryDTO) {
        // 根据汽车种类查询条件
        LambdaQueryWrapper<VehicleInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getVehicleType()), 
                    VehicleInfo::getVehicleType, queryDTO.getVehicleType())
               .eq(StringUtils.hasText(queryDTO.getVehicleNo()), 
                    VehicleInfo::getVehicleNo, queryDTO.getVehicleNo())
               .eq(queryDTO.getStatus() != null,
                    VehicleInfo::getStatus, queryDTO.getStatus())
               .orderByDesc(VehicleInfo::getCreateTime);
        
        // 分页查询
        Page<VehicleInfo> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page = this.page(page, wrapper);
        
        // 转换为VO
        Page<VehicleVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(this::convertToVO)
                .toList());
        
        return voPage;
    }
    
    @Override
    public VehicleVO getVehicleByVehicleNo(String vehicleNo) {
        VehicleInfo vehicleInfo = this.getOne(new LambdaQueryWrapper<VehicleInfo>().eq(VehicleInfo::getVehicleNo, vehicleNo));
        if (vehicleInfo == null) {
            throw new BusinessException("车辆不存在");
        }
        return convertToVO(vehicleInfo);
    }
    
    @Override
    public boolean addVehicle(VehicleInfo vehicleInfo) {
        // 校验车牌号不能为空
        if (vehicleInfo.getVehicleNo() == null || vehicleInfo.getVehicleNo().trim().isEmpty()) {
            throw new BusinessException("车牌号不能为空");
        }
        
        // 校验车牌号是否已存在
        LambdaQueryWrapper<VehicleInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VehicleInfo::getVehicleNo, vehicleInfo.getVehicleNo());
        if (this.count(wrapper) > 0) {
            throw new BusinessException("车牌号已存在");
        }
        
        return this.save(vehicleInfo);
    }
    
    @Override
    public boolean updateVehicle(VehicleInfo vehicleInfo) {
        // 校验车牌号不能为空
        if (vehicleInfo.getVehicleNo() == null || vehicleInfo.getVehicleNo().trim().isEmpty()) {
            throw new BusinessException("车牌号不能为空");
        }
        
        // 根据车牌号查询车辆是否存在
        LambdaQueryWrapper<VehicleInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VehicleInfo::getVehicleNo, vehicleInfo.getVehicleNo().trim());
        VehicleInfo existing = this.getOne(wrapper);
        if (existing == null) {
            throw new BusinessException("车牌号【" + vehicleInfo.getVehicleNo() + "】对应的车辆不存在");
        }
        
        // 更新字段（只更新非空字段）
        if (vehicleInfo.getVehicleType() != null) {
            existing.setVehicleType(vehicleInfo.getVehicleType());
        }
        if (vehicleInfo.getBrand() != null) {
            existing.setBrand(vehicleInfo.getBrand());
        }
        if (vehicleInfo.getModel() != null) {
            existing.setModel(vehicleInfo.getModel());
        }
        if (vehicleInfo.getColor() != null) {
            existing.setColor(vehicleInfo.getColor());
        }
        if (vehicleInfo.getVin() != null) {
            existing.setVin(vehicleInfo.getVin());
        }
        if (vehicleInfo.getEngineNo() != null) {
            existing.setEngineNo(vehicleInfo.getEngineNo());
        }
        if (vehicleInfo.getPurchaseDate() != null) {
            existing.setPurchaseDate(vehicleInfo.getPurchaseDate());
        }
        if (vehicleInfo.getPurchasePrice() != null) {
            existing.setPurchasePrice(vehicleInfo.getPurchasePrice());
        }
        if (vehicleInfo.getDeptId() != null) {
            existing.setDeptId(vehicleInfo.getDeptId());
        }
        if (vehicleInfo.getStatus() != null) {
            existing.setStatus(vehicleInfo.getStatus());
        }
        if (vehicleInfo.getSeats() != null) {
            existing.setSeats(vehicleInfo.getSeats());
        }
        if (vehicleInfo.getMileage() != null) {
            existing.setMileage(vehicleInfo.getMileage());
        }
        if (vehicleInfo.getRemark() != null) {
            existing.setRemark(vehicleInfo.getRemark());
        }
        if (vehicleInfo.getTerminalNo() != null) {
            existing.setTerminalNo(vehicleInfo.getTerminalNo());
        }
        
        // 车牌号和ID不允许修改，createTime和updateTime由系统管理
        // updateTime 会由 MyBatis Plus 自动更新
        
        return this.updateById(existing);
    }
    
    @Override
    public boolean deleteVehicle(String vehicleNo) {
        VehicleInfo vehicleInfo = this.getOne(
                new LambdaQueryWrapper<VehicleInfo>().eq(VehicleInfo::getVehicleNo, vehicleNo));
        if (vehicleInfo == null) throw new BusinessException("车辆不存在");

        // Bug 修复 B8：车辆在用时不允许删除
        if (Constants.VehicleStatus.IN_USE.equals(vehicleInfo.getStatus())) {
            throw new BusinessException("删除失败：车辆 [" + vehicleNo + "] 当前处于在用状态，请先完成还车");
        }

        // Bug 修复 B8：存在待出车或行驶中的调度单时不允许删除
        long activeDispatches = vehicleDispatchMapper.selectCount(
                new LambdaQueryWrapper<VehicleDispatch>()
                        .eq(VehicleDispatch::getVehicleNo, vehicleNo)
                        .in(VehicleDispatch::getDispatchStatus,
                                Constants.DispatchStatus.PENDING,
                                Constants.DispatchStatus.IN_PROGRESS));
        if (activeDispatches > 0) {
            throw new BusinessException(
                    "删除失败：车辆 [" + vehicleNo + "] 存在 " + activeDispatches + " 条进行中的调度记录，请先完成调度");
        }

        // Bug 修复 B8：存在待审批的用车申请时不允许删除
        long pendingApplies = vehicleApplyMapper.selectCount(
                new LambdaQueryWrapper<VehicleApply>()
                        .eq(VehicleApply::getVehicleNo, vehicleNo)
                        .eq(VehicleApply::getApplyStatus, Constants.ApplyStatus.PENDING));
        if (pendingApplies > 0) {
            throw new BusinessException(
                    "删除失败：车辆 [" + vehicleNo + "] 存在 " + pendingApplies + " 条待审批的用车申请，请先处理");
        }

        Long vehicleId = vehicleInfo.getId();

        // 删除关联的定位、轨迹、围栏报警、行程记录
        beidouLocationMapper.delete(
                new LambdaQueryWrapper<BeidouLocation>().eq(BeidouLocation::getVehicleId, vehicleId));
        beidouTrackHistoryMapper.delete(
                new LambdaQueryWrapper<BeidouTrackHistory>().eq(BeidouTrackHistory::getVehicleId, vehicleId));
        fenceAlarmMapper.delete(
                new LambdaQueryWrapper<FenceAlarm>().eq(FenceAlarm::getVehicleId, vehicleId));
        tripRecordMapper.delete(
                new LambdaQueryWrapper<TripRecord>().eq(TripRecord::getVehicleId, vehicleId));

        return this.baseMapper.delete(
                new LambdaQueryWrapper<VehicleInfo>().eq(VehicleInfo::getVehicleNo, vehicleNo)) > 0;
    }
    
    /**
     * 实体转VO
     */
    private VehicleVO convertToVO(VehicleInfo vehicleInfo) {
        VehicleVO vo = new VehicleVO();
        BeanUtils.copyProperties(vehicleInfo, vo);
        
        // 设置状态描述
        switch (vehicleInfo.getStatus()) {
            case 0 -> vo.setStatusDesc("空闲");
            case 1 -> vo.setStatusDesc("在用");
            case 2 -> vo.setStatusDesc("维修");
            case 3 -> vo.setStatusDesc("报废");
            default -> vo.setStatusDesc("未知");
        }
        
        // TODO: 从Redis获取在线状态
        vo.setOnline(false);
        
        return vo;
    }
}
