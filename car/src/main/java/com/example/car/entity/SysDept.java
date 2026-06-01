package com.example.car.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 部门实体类
 */
@Data
@TableName("sys_dept")
public class SysDept {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long parentId;
    
    private String ancestors;
    
    private String deptName;
    
    private Integer orderNum;
    
    private String leader;
    
    private String phone;
    
    private String email;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
