package com.example.car.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.car.common.exception.BusinessException;
import com.example.car.entity.VehicleInfo;
import com.example.car.entity.VehicleMaintenance;
import com.example.car.entity.dto.VehicleMaintenanceQueryDTO;
import com.example.car.entity.vo.VehicleMaintenanceVO;
import com.example.car.mapper.VehicleInfoMapper;
import com.example.car.mapper.VehicleMaintenanceMapper;
import com.example.car.service.VehicleMaintenanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleMaintenanceServiceImpl extends ServiceImpl<VehicleMaintenanceMapper, VehicleMaintenance>
        implements VehicleMaintenanceService {

    private final VehicleInfoMapper vehicleInfoMapper;

    @Override
    public Page<VehicleMaintenanceVO> pageQuery(VehicleMaintenanceQueryDTO queryDTO) {
        LambdaQueryWrapper<VehicleMaintenance> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getVehicleNo()),
                    VehicleMaintenance::getVehicleNo, queryDTO.getVehicleNo())
               .eq(queryDTO.getMaintenanceType() != null,
                    VehicleMaintenance::getMaintenanceType, queryDTO.getMaintenanceType())
               .orderByDesc(VehicleMaintenance::getMaintenanceDate);

        Page<VehicleMaintenance> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page = this.page(page, wrapper);

        Page<VehicleMaintenanceVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(this::convertToVO)
                .toList());

        return voPage;
    }

    @Override
    public VehicleMaintenanceVO getMaintenanceByVehicleNo(String vehicleNo) {
        String trimmedVehicleNo = vehicleNo != null ? vehicleNo.trim() : "";

        LambdaQueryWrapper<VehicleMaintenance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VehicleMaintenance::getVehicleNo, trimmedVehicleNo)
               .orderByDesc(VehicleMaintenance::getMaintenanceDate)
               .last("LIMIT 1");
        VehicleMaintenance vehicleMaintenance = this.getOne(wrapper, false);
        if (vehicleMaintenance == null) {
            throw new BusinessException("该车牌号【" + trimmedVehicleNo + "】暂无维护记录");
        }
        return convertToVO(vehicleMaintenance);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addMaintenance(VehicleMaintenance vehicleMaintenance) {
        // 若前端只传了 vehicleNo 未传 vehicleId，自动根据车牌号补查 vehicleId
        if (vehicleMaintenance.getVehicleId() == null
                && StringUtils.hasText(vehicleMaintenance.getVehicleNo())) {
            VehicleInfo vehicle = vehicleInfoMapper.selectOne(
                    new LambdaQueryWrapper<VehicleInfo>()
                            .eq(VehicleInfo::getVehicleNo, vehicleMaintenance.getVehicleNo().trim()));
            if (vehicle == null) {
                throw new BusinessException("车牌号【" + vehicleMaintenance.getVehicleNo() + "】对应的车辆不存在");
            }
            vehicleMaintenance.setVehicleId(vehicle.getId());
        }
        if (vehicleMaintenance.getVehicleId() == null) {
            throw new BusinessException("车辆ID不能为空，请先选择车辆");
        }
        return this.save(vehicleMaintenance);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMaintenance(VehicleMaintenance vehicleMaintenance) {
        if (vehicleMaintenance.getVehicleNo() == null || vehicleMaintenance.getVehicleNo().trim().isEmpty()) {
            throw new BusinessException("车牌号不能为空");
        }

        LambdaQueryWrapper<VehicleMaintenance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VehicleMaintenance::getVehicleNo, vehicleMaintenance.getVehicleNo().trim())
               .orderByDesc(VehicleMaintenance::getMaintenanceDate)
               .last("LIMIT 1");
        VehicleMaintenance existing = this.getOne(wrapper, false);

        if (existing == null) {
            throw new BusinessException("车牌号【" + vehicleMaintenance.getVehicleNo() + "】暂无维护记录");
        }

        if (vehicleMaintenance.getMaintenanceType() != null)     existing.setMaintenanceType(vehicleMaintenance.getMaintenanceType());
        if (vehicleMaintenance.getMaintenanceDate() != null)     existing.setMaintenanceDate(vehicleMaintenance.getMaintenanceDate());
        if (vehicleMaintenance.getMaintenanceItem() != null)     existing.setMaintenanceItem(vehicleMaintenance.getMaintenanceItem());
        if (vehicleMaintenance.getMaintenanceCost() != null)     existing.setMaintenanceCost(vehicleMaintenance.getMaintenanceCost());
        if (vehicleMaintenance.getCurrentMileage() != null)      existing.setCurrentMileage(vehicleMaintenance.getCurrentMileage());
        if (vehicleMaintenance.getServiceProvider() != null)     existing.setServiceProvider(vehicleMaintenance.getServiceProvider());
        if (vehicleMaintenance.getNextMaintenanceDate() != null) existing.setNextMaintenanceDate(vehicleMaintenance.getNextMaintenanceDate());
        if (vehicleMaintenance.getNextMaintenanceMileage() != null) existing.setNextMaintenanceMileage(vehicleMaintenance.getNextMaintenanceMileage());
        if (vehicleMaintenance.getRemark() != null)              existing.setRemark(vehicleMaintenance.getRemark());

        return this.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMaintenance(String vehicleNo) {
        VehicleMaintenance vehicleMaintenance = this.getOne(
                new LambdaQueryWrapper<VehicleMaintenance>().eq(VehicleMaintenance::getVehicleNo, vehicleNo));
        if (vehicleMaintenance == null) {
            throw new BusinessException("维护记录不存在");
        }
        return this.baseMapper.delete(
                new LambdaQueryWrapper<VehicleMaintenance>().eq(VehicleMaintenance::getVehicleNo, vehicleNo)) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMaintenanceById(Long id) {
        VehicleMaintenance record = this.getById(id);
        if (record == null) {
            throw new BusinessException("维护记录不存在，id=" + id);
        }
        return this.removeById(id);
    }

    private VehicleMaintenanceVO convertToVO(VehicleMaintenance vehicleMaintenance) {
        VehicleMaintenanceVO vo = new VehicleMaintenanceVO();
        BeanUtils.copyProperties(vehicleMaintenance, vo);

        if (vehicleMaintenance.getMaintenanceType() != null) {
            switch (vehicleMaintenance.getMaintenanceType()) {
                case 1 -> vo.setMaintenanceTypeDesc("保养");
                case 2 -> vo.setMaintenanceTypeDesc("维修");
                case 3 -> vo.setMaintenanceTypeDesc("年检");
                case 4 -> vo.setMaintenanceTypeDesc("保险");
                default -> vo.setMaintenanceTypeDesc("未知");
            }
        }

        return vo;
    }
}
