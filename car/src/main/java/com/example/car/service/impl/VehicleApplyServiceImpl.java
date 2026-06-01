package com.example.car.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.car.common.constant.Constants;
import com.example.car.common.exception.BusinessException;
import com.example.car.entity.SysUser;
import com.example.car.entity.VehicleApply;
import com.example.car.entity.VehicleDispatch;
import com.example.car.entity.VehicleReturn;
import com.example.car.entity.dto.VehicleApplyCreateDTO;
import com.example.car.entity.dto.VehicleApplyQueryDTO;
import com.example.car.entity.dto.VehicleApplyUpdateDTO;
import com.example.car.entity.vo.VehicleApplyVO;
import com.example.car.entity.VehicleInfo;
import com.example.car.mapper.SysUserMapper;
import com.example.car.mapper.VehicleApplyMapper;
import com.example.car.mapper.VehicleDispatchMapper;
import com.example.car.mapper.VehicleInfoMapper;
import com.example.car.mapper.VehicleReturnMapper;
import com.example.car.service.VehicleApplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleApplyServiceImpl extends ServiceImpl<VehicleApplyMapper, VehicleApply>
        implements VehicleApplyService {

    private final SysUserMapper sysUserMapper;
    private final VehicleInfoMapper vehicleInfoMapper;
    private final VehicleDispatchMapper vehicleDispatchMapper;
    private final VehicleReturnMapper vehicleReturnMapper;

    // ------------------------------------------------------------------ query

    @Override
    public Page<VehicleApplyVO> pageQuery(VehicleApplyQueryDTO queryDTO) {
        LambdaQueryWrapper<VehicleApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getApplyNo()),
                        VehicleApply::getApplyNo, queryDTO.getApplyNo())
               .like(StringUtils.hasText(queryDTO.getApplyUserName()),
                        VehicleApply::getApplyUserName, queryDTO.getApplyUserName())
               .eq(StringUtils.hasText(queryDTO.getVehicleNo()),
                        VehicleApply::getVehicleNo, queryDTO.getVehicleNo())
               .eq(queryDTO.getApplyUserId() != null,
                        VehicleApply::getApplyUserId, queryDTO.getApplyUserId())
               .eq(queryDTO.getDeptId() != null,
                        VehicleApply::getDeptId, queryDTO.getDeptId())
               .eq(queryDTO.getApplyStatus() != null,
                        VehicleApply::getApplyStatus, queryDTO.getApplyStatus())
               .ge(queryDTO.getStartTime() != null,
                        VehicleApply::getApplyTime, queryDTO.getStartTime())
               .le(queryDTO.getEndTime() != null,
                        VehicleApply::getApplyTime, queryDTO.getEndTime())
               .orderByDesc(VehicleApply::getApplyTime);

        Page<VehicleApply> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page = this.page(page, wrapper);

        Page<VehicleApplyVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::convertToVO).toList());
        return voPage;
    }

    @Override
    public VehicleApplyVO getDetailById(Long id) {
        VehicleApply apply = this.getById(id);
        if (apply == null) throw new BusinessException("用车申请不存在");
        return convertToVO(apply);
    }

    // ----------------------------------------------------------------- create

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean createApply(VehicleApplyCreateDTO dto) {
        SysUser currentUser = currentUser();

        VehicleApply apply = new VehicleApply();
        // 只复制用户可填写的字段，服务端控制字段不从客户端读取
        apply.setVehicleNo(dto.getVehicleNo());
        apply.setPurpose(dto.getPurpose());
        apply.setPlanStartTime(dto.getPlanStartTime());
        apply.setPlanEndTime(dto.getPlanEndTime());
        apply.setStartLocation(dto.getStartLocation());
        apply.setEndLocation(dto.getEndLocation());
        apply.setPassengerCount(dto.getPassengerCount());
        apply.setPassengerNames(dto.getPassengerNames());
        apply.setRemark(dto.getRemark());

        // 服务端控制字段，由系统自动填充
        apply.setApplyUserId(currentUser.getId());
        apply.setApplyUserName(
                StringUtils.hasText(currentUser.getRealName())
                        ? currentUser.getRealName()
                        : currentUser.getUsername());
        apply.setDeptId(currentUser.getDeptId());
        apply.setApplyNo(generateApplyNo());
        apply.setApplyStatus(Constants.ApplyStatus.PENDING);
        apply.setApplyTime(LocalDateTime.now());

        if (apply.getPlanStartTime() != null && apply.getPlanEndTime() != null
                && apply.getPlanStartTime().isAfter(apply.getPlanEndTime())) {
            throw new BusinessException("计划结束时间不能早于开始时间");
        }

        return this.save(apply);
    }

    // ----------------------------------------------------------------- update

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateApply(VehicleApplyUpdateDTO dto) {
        VehicleApply existing = requireOwnership(dto.getId());

        if (!Constants.ApplyStatus.PENDING.equals(existing.getApplyStatus())) {
            throw new BusinessException("只有待审批状态的申请才能修改");
        }

        // 只更新用户可编辑的字段子集
        VehicleApply patch = new VehicleApply();
        patch.setId(existing.getId());
        patch.setPurpose(dto.getPurpose());
        patch.setPlanStartTime(dto.getPlanStartTime());
        patch.setPlanEndTime(dto.getPlanEndTime());
        patch.setStartLocation(dto.getStartLocation());
        patch.setEndLocation(dto.getEndLocation());
        patch.setPassengerCount(dto.getPassengerCount());
        patch.setPassengerNames(dto.getPassengerNames());
        patch.setRemark(dto.getRemark());

        if (patch.getPlanStartTime() != null && patch.getPlanEndTime() != null
                && patch.getPlanStartTime().isAfter(patch.getPlanEndTime())) {
            throw new BusinessException("计划结束时间不能早于开始时间");
        }

        return this.updateById(patch);
    }

    // ----------------------------------------------------------------- cancel

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelApply(Long id) {
        VehicleApply apply = requireOwnership(id);

        if (Constants.ApplyStatus.CANCELLED.equals(apply.getApplyStatus())
                || Constants.ApplyStatus.REJECTED.equals(apply.getApplyStatus())) {
            throw new BusinessException("申请已取消或已拒绝，无法再次取消");
        }

        apply.setApplyStatus(Constants.ApplyStatus.CANCELLED);
        boolean result = this.updateById(apply);

        // 若车辆已分配，恢复为空闲状态
        if (StringUtils.hasText(apply.getVehicleNo())) {
            VehicleInfo vehicle = vehicleInfoMapper.selectOne(
                    new LambdaQueryWrapper<VehicleInfo>()
                            .eq(VehicleInfo::getVehicleNo, apply.getVehicleNo()));
            if (vehicle != null && Constants.VehicleStatus.IN_USE.equals(vehicle.getStatus())) {
                vehicle.setStatus(Constants.VehicleStatus.FREE);
                vehicleInfoMapper.updateById(vehicle);
            }
        }

        // 取消关联的待出车调度单
        VehicleDispatch dispatch = vehicleDispatchMapper.selectOne(
                new LambdaQueryWrapper<VehicleDispatch>()
                        .eq(VehicleDispatch::getApplyId, id)
                        .eq(VehicleDispatch::getDispatchStatus, Constants.DispatchStatus.PENDING));
        if (dispatch != null) {
            dispatch.setDispatchStatus(Constants.DispatchStatus.CANCELLED);
            vehicleDispatchMapper.updateById(dispatch);
            vehicleReturnMapper.delete(new LambdaQueryWrapper<VehicleReturn>()
                    .eq(VehicleReturn::getDispatchId, dispatch.getId())
                    .eq(VehicleReturn::getReturnStatus, 0));
        }

        return result;
    }

    // ----------------------------------------------------------------- delete

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteApply(Long id) {
        VehicleApply apply = requireOwnership(id);

        // 若申请已通过，删除前先释放车辆
        if (Constants.ApplyStatus.APPROVED.equals(apply.getApplyStatus())
                && StringUtils.hasText(apply.getVehicleNo())) {
            VehicleInfo vehicle = vehicleInfoMapper.selectOne(
                    new LambdaQueryWrapper<VehicleInfo>()
                            .eq(VehicleInfo::getVehicleNo, apply.getVehicleNo()));
            if (vehicle != null && Constants.VehicleStatus.IN_USE.equals(vehicle.getStatus())) {
                vehicle.setStatus(Constants.VehicleStatus.FREE);
                vehicleInfoMapper.updateById(vehicle);
            }
        }

        // 删除关联的待出车调度单及其还车记录
        VehicleDispatch pendingDispatch = vehicleDispatchMapper.selectOne(
                new LambdaQueryWrapper<VehicleDispatch>()
                        .eq(VehicleDispatch::getApplyId, id)
                        .eq(VehicleDispatch::getDispatchStatus, Constants.DispatchStatus.PENDING));
        if (pendingDispatch != null) {
            vehicleReturnMapper.delete(new LambdaQueryWrapper<VehicleReturn>()
                    .eq(VehicleReturn::getDispatchId, pendingDispatch.getId())
                    .eq(VehicleReturn::getReturnStatus, 0));
            vehicleDispatchMapper.deleteById(pendingDispatch.getId());
        }

        return this.baseMapper.deleteById(id) > 0;
    }

    // ---------------------------------------------------------------- approve

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approveApply(Long id, Integer status, String remark) {
        VehicleApply apply = this.getById(id);
        if (apply == null) throw new BusinessException("用车申请不存在");
        if (!Constants.ApplyStatus.PENDING.equals(apply.getApplyStatus())) {
            throw new BusinessException("该申请已处理，无法重复审批");
        }

        apply.setApplyStatus(status);
        apply.setRemark(remark);
        boolean result = this.updateById(apply);

        if (StringUtils.hasText(apply.getVehicleNo())) {
            VehicleInfo vehicle = vehicleInfoMapper.selectOne(
                    new LambdaQueryWrapper<VehicleInfo>()
                            .eq(VehicleInfo::getVehicleNo, apply.getVehicleNo()));
            if (vehicle != null) {
                if (Constants.ApplyStatus.APPROVED.equals(status)) {
                    // 审批通过：将车辆状态设为在用，并创建调度单和还车记录
                    vehicle.setStatus(Constants.VehicleStatus.IN_USE);
                    vehicleInfoMapper.updateById(vehicle);

                    VehicleDispatch dispatch = new VehicleDispatch();
                    dispatch.setApplyId(apply.getId());
                    dispatch.setVehicleNo(apply.getVehicleNo());
                    dispatch.setVehicleId(vehicle.getId());
                    dispatch.setStartLocation(apply.getStartLocation());
                    dispatch.setEndLocation(apply.getEndLocation());
                    dispatch.setPlanStartTime(apply.getPlanStartTime());
                    dispatch.setPlanEndTime(apply.getPlanEndTime());
                    dispatch.setDispatchStatus(Constants.DispatchStatus.PENDING);
                    dispatch.setDispatchNo(generateDispatchNo());
                    dispatch.setDriverId(null);
                    dispatch.setDriverName(apply.getApplyUserName());
                    vehicleDispatchMapper.insert(dispatch);

                    VehicleReturn vehicleReturn = new VehicleReturn();
                    vehicleReturn.setDispatchId(dispatch.getId());
                    vehicleReturn.setDispatchNo(dispatch.getDispatchNo());
                    vehicleReturn.setVehicleId(vehicle.getId());
                    vehicleReturn.setVehicleNo(apply.getVehicleNo());
                    vehicleReturn.setDriverName(apply.getApplyUserName());
                    vehicleReturn.setEndLocation(apply.getEndLocation());
                    vehicleReturn.setPlanEndTime(apply.getPlanEndTime());
                    vehicleReturn.setReturnStatus(0);
                    vehicleReturn.setMileageBefore(vehicle.getMileage() != null ? vehicle.getMileage() : 0);
                    vehicleReturn.setMileageAfter(vehicle.getMileage() != null ? vehicle.getMileage() : 0);
                    vehicleReturn.setFuelLevel(0);
                    vehicleReturn.setVehicleCondition(0);
                    vehicleReturnMapper.insert(vehicleReturn);
                } else {
                    // 审批拒绝：恢复车辆为空闲状态
                    vehicle.setStatus(Constants.VehicleStatus.FREE);
                    vehicleInfoMapper.updateById(vehicle);
                }
            }
        }

        return result;
    }

    // --------------------------------------------------------- private helpers

    /**
     * 获取当前登录用户，不存在时抛出异常
     */
    private SysUser currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) throw new BusinessException("当前登录用户不存在");
        return user;
    }

    /**
     * 加载申请记录并校验归属权。
     * ADMIN 用户可操作任意申请；普通用户只能操作自己提交的申请。
     *
     * @throws BusinessException 申请不存在，或当前用户无权操作
     */
    private VehicleApply requireOwnership(Long applyId) {
        VehicleApply apply = this.getById(applyId);
        if (apply == null) throw new BusinessException("用车申请不存在");

        SysUser currentUser = currentUser();

        // ADMIN 可管理所有申请
        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
        if (!isAdmin && !currentUser.getId().equals(apply.getApplyUserId())) {
            log.warn("[申请] 归属校验失败：用户={} 尝试操作申请={}",
                    currentUser.getUsername(), applyId);
            throw new BusinessException("只能操作自己提交的申请");
        }

        return apply;
    }

    private String generateApplyNo() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int rand = (int) (Math.random() * 9000) + 1000;
        return "APPLY" + ts + rand;
    }

    private String generateDispatchNo() {
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int rand = (int) (Math.random() * 9000) + 1000;
        return "DISPATCH" + ts + rand;
    }

    private VehicleApplyVO convertToVO(VehicleApply a) {
        VehicleApplyVO vo = new VehicleApplyVO();
        BeanUtils.copyProperties(a, vo);
        Integer status = a.getApplyStatus();
        if (status == null) status = 0;
        vo.setApplyStatusDesc(switch (status) {
            case 0 -> "待审批";
            case 1 -> "已通过";
            case 2 -> "已拒绝";
            case 3 -> "已取消";
            default -> "未知";
        });
        return vo;
    }
}
