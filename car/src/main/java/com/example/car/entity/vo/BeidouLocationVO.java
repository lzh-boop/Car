package com.example.car.entity.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "北斗定位信息")
public class BeidouLocationVO {

    @Schema(description = "定位记录ID")
    private Long id;

    @Schema(description = "北斗终端编号")
    private String terminalNo;

    @Schema(description = "车辆ID")
    private Long vehicleId;

    @Schema(description = "车牌号")
    private String vehicleNo;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "速度（km/h）")
    private BigDecimal speed;

    @Schema(description = "方向角（0-359°，正北为0°）")
    private Integer direction;

    @Schema(description = "海拔高度（米）")
    private BigDecimal altitude;

    @Schema(description = "北斗定位时间")
    private LocalDateTime locationTime;

    @Schema(description = "服务器接收时间")
    private LocalDateTime receiveTime;

    @Schema(description = "位置地址")
    private String address;

    @Schema(description = "信号强度（0-100）")
    private Integer signalStrength;

    @Schema(description = "卫星数量")
    private Integer satelliteCount;
}
