package com.example.car.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.car.common.constant.Constants;
import com.example.car.common.exception.BusinessException;
import com.example.car.entity.VehicleDispatch;
import com.example.car.entity.VehicleInfo;
import com.example.car.entity.VehicleReturn;
import com.example.car.entity.dto.VehicleReturnDoDTO;
import com.example.car.entity.dto.VehicleReturnQueryDTO;
import com.example.car.entity.vo.VehicleReturnVO;
import com.example.car.mapper.VehicleDispatchMapper;
import com.example.car.mapper.VehicleInfoMapper;
import com.example.car.mapper.VehicleReturnMapper;
import com.example.car.service.VehicleReturnService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VehicleReturnServiceImpl extends ServiceImpl<VehicleReturnMapper, VehicleReturn>
        implements VehicleReturnService {

    private final VehicleInfoMapper vehicleInfoMapper;
    private final VehicleDispatchMapper vehicleDispatchMapper;

    @Override
    public Page<VehicleReturnVO> pageQuery(VehicleReturnQueryDTO queryDTO) {
        LambdaQueryWrapper<VehicleReturn> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getVehicleNo()),
                        VehicleReturn::getVehicleNo, queryDTO.getVehicleNo())
               .like(StringUtils.hasText(queryDTO.getDriverName()),
                        VehicleReturn::getDriverName, queryDTO.getDriverName())
               .eq(queryDTO.getReturnStatus() != null,
                        VehicleReturn::getReturnStatus, queryDTO.getReturnStatus())
               .orderByDesc(VehicleReturn::getCreateTime);

        Page<VehicleReturn> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page = this.page(page, wrapper);

        Page<VehicleReturnVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::convertToVO).toList());
        return voPage;
    }

    @Override
    public VehicleReturnVO getDetailById(Long id) {
        VehicleReturn record = this.getById(id);
        if (record == null) throw new BusinessException("还车记录不存在");
        return convertToVO(record);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean doReturn(VehicleReturnDoDTO dto) {
        // 从数据库加载记录，不信任客户端传入的车辆/调度关联字段
        VehicleReturn existing = this.getById(dto.getId());
        if (existing == null) throw new BusinessException("还车记录不存在");
        if (Integer.valueOf(1).equals(existing.getReturnStatus())) {
            throw new BusinessException("该车辆已完成还车，请勿重复操作");
        }

        // 只更新驾驶员可填写的字段
        VehicleReturn patch = new VehicleReturn();
        patch.setId(existing.getId());
        patch.setMileageAfter(dto.getMileageAfter());
        patch.setFuelLevel(dto.getFuelLevel());
        patch.setVehicleCondition(dto.getVehicleCondition());
        patch.setRemark(dto.getRemark());
        // 服务端控制字段
        patch.setActualEndTime(LocalDateTime.now());
        patch.setReturnStatus(1);

        boolean result = this.updateById(patch);

        // 将车辆状态恢复为空闲，并更新里程
        if (StringUtils.hasText(existing.getVehicleNo())) {
            VehicleInfo vehicle = vehicleInfoMapper.selectOne(
                    new LambdaQueryWrapper<VehicleInfo>()
                            .eq(VehicleInfo::getVehicleNo, existing.getVehicleNo()));
            if (vehicle != null) {
                vehicle.setStatus(Constants.VehicleStatus.FREE);
                if (dto.getMileageAfter() != null && dto.getMileageAfter() > 0) {
                    vehicle.setMileage(dto.getMileageAfter());
                }
                vehicleInfoMapper.updateById(vehicle);
            }
        }

        // 将关联调度单标记为已完成
        if (existing.getDispatchId() != null) {
            VehicleDispatch dispatch = vehicleDispatchMapper.selectById(existing.getDispatchId());
            if (dispatch != null && !Constants.DispatchStatus.COMPLETED.equals(dispatch.getDispatchStatus())) {
                dispatch.setDispatchStatus(Constants.DispatchStatus.COMPLETED);
                dispatch.setActualEndTime(LocalDateTime.now());
                vehicleDispatchMapper.updateById(dispatch);
            }
        }

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteReturn(Long id) {
        VehicleReturn record = this.getById(id);
        if (record == null) throw new BusinessException("还车记录不存在");
        return this.baseMapper.deleteById(id) > 0;
    }

    private VehicleReturnVO convertToVO(VehicleReturn r) {
        VehicleReturnVO vo = new VehicleReturnVO();
        BeanUtils.copyProperties(r, vo);

        if (r.getVehicleId() != null) {
            VehicleInfo vehicle = vehicleInfoMapper.selectById(r.getVehicleId());
            if (vehicle != null) {
                vo.setVehicleType(vehicle.getVehicleType());
                vo.setBrand(vehicle.getBrand());
            }
        }

        // 油量状态描述
        vo.setFuelLevelDesc(switch (r.getFuelLevel() == null ? 0 : r.getFuelLevel()) {
            case 1  -> "偏少";
            case 2  -> "需加油";
            default -> "充足";
        });

        // 车辆状况描述
        vo.setVehicleConditionDesc(switch (r.getVehicleCondition() == null ? 0 : r.getVehicleCondition()) {
            case 1  -> "轻微损伤";
            case 2  -> "需维修";
            default -> "正常";
        });

        // 还车状态描述
        vo.setReturnStatusDesc(switch (r.getReturnStatus() == null ? 0 : r.getReturnStatus()) {
            case 1  -> "已还车";
            case 2  -> "已取消";
            default -> "待还车";
        });

        return vo;
    }
}
