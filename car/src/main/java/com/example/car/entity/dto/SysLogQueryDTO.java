package com.example.car.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "系统日志查询条件")
public class SysLogQueryDTO {
    
    @Schema(name = "用户ID", description = "用户ID", example = "1")
    private Long userId;
    
    @Schema(name = "用户名", description = "用户名（模糊查询）", example = "admin")
    private String username;
    
    @Schema(name = "操作内容", description = "操作内容（模糊查询）", example = "登录系统")
    private String operation;
    
    @Schema(name = "开始时间", description = "开始时间", example = "2024-01-01 00:00:00")
    private LocalDateTime startTime;
    
    @Schema(name = "结束时间", description = "结束时间", example = "2024-01-31 23:59:59")
    private LocalDateTime endTime;
    
    @Schema(name = "页码", description = "页码", example = "1")
    private Integer pageNum = 1;
    
    @Schema(name = "每页数量", description = "每页数量", example = "10")
    private Integer pageSize = 10;
}
