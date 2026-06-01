package com.example.car.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_log")
public class SysLog {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String username;
    
    private String operation;
    
    private String method;
    
    private String params;
    
    private String result;
    
    private String ip;
    
    private String location;
    
    private Long executeTime;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
