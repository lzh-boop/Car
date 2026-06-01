package com.example.car.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("apply_approve")
public class ApplyApprove {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long applyId;
    
    private Long approverId;
    
    private String approverName;
    
    private Integer approveLevel;
    
    private Integer approveResult;
    
    private String approveOpinion;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime approveTime;
}
