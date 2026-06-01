package com.example.car.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 北斗定位数据实体类
 */
@Data
@TableName("gps_location")
public class BeidouLocation {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 北斗终端编号 */
    private String terminalNo;

    private Long vehicleId;

    private String vehicleNo;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private BigDecimal speed;

    /** 方向角（0-359°，正北为0°，顺时针） */
    private Integer direction;

    private BigDecimal altitude;

    /** 北斗定位时间 */
    private LocalDateTime locationTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime receiveTime;

    private String address;

    /** 信号强度（0-100） */
    private Integer signalStrength;

    /** 卫星数量 */
    private Integer satelliteCount;
}
