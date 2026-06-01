package com.example.car.controller.apply;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.car.common.result.Result;
import com.example.car.entity.dto.VehicleApplyCreateDTO;
import com.example.car.entity.dto.VehicleApplyQueryDTO;
import com.example.car.entity.dto.VehicleApplyUpdateDTO;
import com.example.car.entity.vo.VehicleApplyVO;
import com.example.car.service.VehicleApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用车申请控制器
 *
 * 安全改进：
 *  1. 新增/修改使用专用 DTO，服务端控制字段（applyNo、applyUserId、applyStatus、
 *     applyTime、deptId）不再由前端传入，防止越权赋值。
 *  2. 修改/取消/删除在 Service 层校验申请人归属，普通用户只能操作自己的申请，
 *     管理员（ADMIN）可操作所有申请。
 *  3. 审批接口要求 ADMIN 权限，普通用户无法绕过。
 */
@Tag(name = "用车申请", description = "用车申请管理相关接口")
@RestController
@RequestMapping("/api/apply")
@RequiredArgsConstructor
public class VehicleApplyController {

    private final VehicleApplyService vehicleApplyService;

    /**
     * 普通用户查询自己的申请列表（Service 层自动过滤当前用户数据）
     */
    @Operation(summary = "分页查询申请列表", description = "根据条件分页查询用车申请")
    @GetMapping("/list")
    public Result<Page<VehicleApplyVO>> pageQuery(VehicleApplyQueryDTO queryDTO) {
        return Result.success(vehicleApplyService.pageQuery(queryDTO));
    }

    /**
     * 管理员查询所有申请列表（用于审批管理页面，仅 ADMIN 可访问）
     */
    @Operation(summary = "管理员查询全部申请列表", description = "管理员查看所有用车申请，用于审批管理，需要 ADMIN 角色")
    @GetMapping("/admin/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<VehicleApplyVO>> adminPageQuery(VehicleApplyQueryDTO queryDTO) {
        return Result.success(vehicleApplyService.pageQuery(queryDTO));
    }

    @Operation(summary = "查询申请详情", description = "根据ID查询用车申请详细信息")
    @GetMapping("/detail/{id}")
    public Result<VehicleApplyVO> getDetail(
            @Parameter(description = "申请ID") @PathVariable Long id) {
        return Result.success(vehicleApplyService.getDetailById(id));
    }

    /**
     * 创建用车申请
     * 申请人信息从 JWT Token 中解析获取，前端无法指定 applyUserId
     */
    @Operation(summary = "创建用车申请", description = "提交新的用车申请，申请人信息自动从登录 Token 中获取")
    @PostMapping("/create")
    public Result<Void> create(@Validated @RequestBody VehicleApplyCreateDTO dto) {
        boolean ok = vehicleApplyService.createApply(dto);
        return ok ? Result.success() : Result.error("创建申请失败");
    }

    /**
     * 修改待审批申请
     * Service 层校验归属：仅申请人本人或 ADMIN 可操作
     */
    @Operation(summary = "更新申请信息", description = "修改待审批状态的用车申请（仅申请人本人或管理员）")
    @PutMapping("/update")
    public Result<Void> update(@Validated @RequestBody VehicleApplyUpdateDTO dto) {
        boolean ok = vehicleApplyService.updateApply(dto);
        return ok ? Result.success() : Result.error("更新申请失败");
    }

    /**
     * 取消申请
     * Service 层校验归属：仅申请人本人或 ADMIN 可操作
     */
    @Operation(summary = "取消申请", description = "将申请状态改为已取消（仅申请人本人或管理员）")
    @PutMapping("/cancel/{id}")
    public Result<Void> cancel(
            @Parameter(description = "申请ID") @PathVariable Long id) {
        boolean ok = vehicleApplyService.cancelApply(id);
        return ok ? Result.success() : Result.error("取消申请失败");
    }

    /**
     * 删除申请
     * Service 层校验归属：仅申请人本人或 ADMIN 可操作
     */
    @Operation(summary = "删除申请", description = "删除指定用车申请（仅申请人本人或管理员）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(description = "申请ID") @PathVariable Long id) {
        boolean ok = vehicleApplyService.deleteApply(id);
        return ok ? Result.success() : Result.error("删除申请失败");
    }

    /**
     * 审批用车申请（仅管理员）
     * handlerId 从 Security Context 中解析，不再信任前端参数
     */
    @Operation(summary = "审批申请（仅管理员）", description = "审批通过或拒绝用车申请，需要 ADMIN 角色")
    @PostMapping("/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> approve(@RequestBody Map<String, Object> params) {
        Long    id     = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        String  remark = params.get("remark") != null ? params.get("remark").toString() : "";
        boolean ok = vehicleApplyService.approveApply(id, status, remark);
        return ok ? Result.success() : Result.error("审批失败");
    }
}
