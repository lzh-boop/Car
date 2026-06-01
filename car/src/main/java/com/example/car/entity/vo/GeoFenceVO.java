package com.example.car.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "电子围栏信息")
public class GeoFenceVO {
    
    @Schema(name = "围栏ID", description = "围栏ID")
    private Long id;
    
    @Schema(name = "围栏名称", description = "围栏名称")
    private String fenceName;
    
    @Schema(name = "围栏类型", description = "围栏类型：1-圆形，2-多边形，3-矩形")
    private Integer fenceType;
    
    @Schema(name = "围栏类型描述", description = "围栏类型描述")
    private String fenceTypeDesc;
    
    @Schema(name = "中心点", description = "中心点坐标")
    private String centerPoint;
    
    @Schema(name = "半径", description = "半径（米）")
    private Integer radius;
    
    @Schema(name = "多边形坐标", description = "多边形坐标点")
    private String polygonPoints;
    
    @Schema(name = "绑定车辆", description = "绑定车辆列表")
    private String bindVehicles;
    
    @Schema(name = "报警类型", description = "报警类型：1-进入，2-离开，3-进出")
    private Integer alarmType;
    
    @Schema(name = "报警类型描述", description = "报警类型描述")
    private String alarmTypeDesc;
    
    @Schema(name = "状态", description = "状态：0-禁用，1-启用")
    private Integer status;
    
    @Schema(name = "状态描述", description = "状态描述")
    private String statusDesc;
    
    @Schema(name = "创建时间", description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(name = "更新时间", description = "更新时间")
    private LocalDateTime updateTime;
}
