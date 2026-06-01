package com.example.car.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "系统日志信息")
public class SysLogVO {
    
    @Schema(name = "日志ID", description = "日志ID")
    private Long id;
    
    @Schema(name = "用户ID", description = "操作用户ID")
    private Long userId;
    
    @Schema(name = "用户名", description = "操作用户名")
    private String username;
    
    @Schema(name = "操作内容", description = "操作内容")
    private String operation;
    
    @Schema(name = "请求方法", description = "请求方法")
    private String method;
    
    @Schema(name = "请求参数", description = "请求参数")
    private String params;
    
    @Schema(name = "返回结果", description = "返回结果")
    private String result;
    
    @Schema(name = "IP地址", description = "IP地址")
    private String ip;
    
    @Schema(name = "位置", description = "IP位置")
    private String location;
    
    @Schema(name = "执行时长", description = "执行时长（毫秒）")
    private Long executeTime;
    
    @Schema(name = "创建时间", description = "创建时间")
    private LocalDateTime createTime;
}
