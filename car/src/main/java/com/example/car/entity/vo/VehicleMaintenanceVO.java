package com.example.car.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "车辆维护信息")
public class VehicleMaintenanceVO {
    
    @Schema(name = "维护ID", description = "维护记录ID")
    private Long id;
    
    @Schema(name = "车辆ID", description = "车辆ID")
    private Long vehicleId;
    
    @Schema(name = "车牌号", description = "车牌号")
    private String vehicleNo;
    
    @Schema(name = "维护类型", description = "维护类型：1-保养，2-维修，3-年检，4-保险")
    private Integer maintenanceType;
    
    @Schema(name = "维护类型描述", description = "维护类型描述")
    private String maintenanceTypeDesc;
    
    @Schema(name = "维护日期", description = "维护日期")
    private LocalDate maintenanceDate;
    
    @Schema(name = "维护项目", description = "维护项目")
    private String maintenanceItem;
    
    @Schema(name = "维护费用", description = "维护费用（单位：元）")
    private BigDecimal maintenanceCost;
    
    @Schema(name = "当前里程", description = "当前里程（单位：公里）")
    private BigDecimal currentMileage;
    
    @Schema(name = "服务商", description = "服务商")
    private String serviceProvider;
    
    @Schema(name = "下次维护日期", description = "下次维护日期")
    private LocalDate nextMaintenanceDate;
    
    @Schema(name = "下次维护里程", description = "下次维护里程（单位：公里）")
    private BigDecimal nextMaintenanceMileage;
    
    @Schema(name = "备注", description = "备注")
    private String remark;
    
    @Schema(name = "创建时间", description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(name = "更新时间", description = "更新时间")
    private LocalDateTime updateTime;
}
