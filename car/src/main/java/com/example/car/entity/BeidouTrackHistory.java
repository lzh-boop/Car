package com.example.car.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("gps_track_history")
public class BeidouTrackHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long vehicleId;

    private String vehicleNo;

    private LocalDate trackDate;

    private String trackData;

    private BigDecimal totalDistance;

    private Integer pointCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
