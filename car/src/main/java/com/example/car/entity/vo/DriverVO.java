package com.example.car.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDate;

/**
 * 驾驶员信息VO
 */
@Data
@Schema(description = "驾驶员信息")
public class DriverVO {
    
    @Schema(name = "驾驶员ID", description = "驾驶员ID")
    private Long id;
    
    @Schema(name = "驾驶员姓名", description = "驾驶员姓名")
    private String driverName;
    
    @Schema(name = "手机号", description = "手机号")
    private String phone;
    
    @Schema(name = "身份证号", description = "身份证号")
    private String idCard;
    
    @Schema(name = "驾驶证号", description = "驾驶证号")
    private String licenseNo;
    
    @Schema(name = "准驾车型", description = "准驾车型")
    private String licenseType;
    
    @Schema(name = "领证日期", description = "领证日期")
    private LocalDate licenseDate;
    
    @Schema(name = "所属部门ID", description = "所属部门ID")
    private Long deptId;
    
    @Schema(name = "部门名称", description = "部门名称")
    private String deptName;
    
    @Schema(name = "状态", description = "状态：1-正常，2-停用")
    private Integer status;
    
    @Schema(name = "状态描述", description = "状态描述")
    private String statusDesc;
}
