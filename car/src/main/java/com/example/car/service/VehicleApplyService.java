package com.example.car.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.car.entity.VehicleApply;
import com.example.car.entity.dto.VehicleApplyCreateDTO;
import com.example.car.entity.dto.VehicleApplyQueryDTO;
import com.example.car.entity.dto.VehicleApplyUpdateDTO;
import com.example.car.entity.vo.VehicleApplyVO;

public interface VehicleApplyService extends IService<VehicleApply> {

    Page<VehicleApplyVO> pageQuery(VehicleApplyQueryDTO queryDTO);

    VehicleApplyVO getDetailById(Long id);

    /** Create a new application. Applicant info is resolved from the JWT, not from the DTO. */
    boolean createApply(VehicleApplyCreateDTO dto);

    /**
     * Update an existing application.
     * Security: only the owner (or ADMIN) may update; status check enforced in impl.
     */
    boolean updateApply(VehicleApplyUpdateDTO dto);

    /**
     * Cancel an application.
     * Security: only the owner (or ADMIN) may cancel.
     */
    boolean cancelApply(Long id);

    /**
     * Delete an application.
     * Security: only the owner (or ADMIN) may delete.
     */
    boolean deleteApply(Long id);

    /** Approve or reject an application (ADMIN / approver only). */
    boolean approveApply(Long id, Integer status, String remark);
}
