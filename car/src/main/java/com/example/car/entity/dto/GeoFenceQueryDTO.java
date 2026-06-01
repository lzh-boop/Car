package com.example.car.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "电子围栏查询条件")
public class GeoFenceQueryDTO {
    
    @Schema(name = "围栏名称", description = "围栏名称（模糊查询）", example = "公司围栏")
    private String fenceName;
    
    @Schema(name = "围栏类型", description = "围栏类型：1-圆形，2-多边形，3-矩形", example = "1")
    private Integer fenceType;
    
    @Schema(name = "状态", description = "状态：0-禁用，1-启用", example = "1")
    private Integer status;
    
    @Schema(name = "页码", description = "页码", example = "1")
    private Integer pageNum = 1;
    
    @Schema(name = "每页数量", description = "每页数量", example = "10")
    private Integer pageSize = 10;
}
