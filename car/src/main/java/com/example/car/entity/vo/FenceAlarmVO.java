package com.example.car.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "围栏报警信息")
public class FenceAlarmVO {
    
    @Schema(name = "报警ID", description = "报警记录ID")
    private Long id;
    
    @Schema(name = "围栏ID", description = "围栏ID")
    private Long fenceId;
    
    @Schema(name = "围栏名称", description = "围栏名称")
    private String fenceName;
    
    @Schema(name = "车辆ID", description = "车辆ID")
    private Long vehicleId;
    
    @Schema(name = "车牌号", description = "车牌号")
    private String vehicleNo;
    
    @Schema(name = "报警类型", description = "报警类型：1-进入，2-离开")
    private Integer alarmType;
    
    @Schema(name = "报警类型描述", description = "报警类型描述")
    private String alarmTypeDesc;
    
    @Schema(name = "报警时间", description = "报警时间")
    private LocalDateTime alarmTime;
    
    @Schema(name = "经度", description = "经度")
    private BigDecimal longitude;
    
    @Schema(name = "纬度", description = "纬度")
    private BigDecimal latitude;
    
    @Schema(name = "地址", description = "报警位置地址")
    private String address;
    
    @Schema(name = "是否处理", description = "是否处理：0-未处理，1-已处理")
    private Integer isHandled;
    
    @Schema(name = "处理状态描述", description = "处理状态描述")
    private String isHandledDesc;
    
    @Schema(name = "处理人ID", description = "处理人ID")
    private Long handlerId;
    
    @Schema(name = "处理时间", description = "处理时间")
    private LocalDateTime handleTime;
    
    @Schema(name = "处理备注", description = "处理备注")
    private String handleRemark;
    
    @Schema(name = "创建时间", description = "创建时间")
    private LocalDateTime createTime;
}
