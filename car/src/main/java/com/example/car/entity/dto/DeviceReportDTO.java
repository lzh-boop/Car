package com.example.car.entity.dto;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 北斗硬件终端上报的原始 JSON 格式
 * 对应设备（如 esp32_sim7670x）通过 HTTP POST 推送的数据结构
 */
@Data
@Schema(description = "北斗终端设备原始上报数据")
public class DeviceReportDTO {

    @Schema(description = "设备ID / 终端编号", example = "esp32_sim7670x")
    @JsonProperty("device_id")
    private String deviceId;

    @Schema(description = "纬度（十进制度，如 39.9042）", example = "39.9042")
    @JsonProperty("latitude")
    private String latitude;

    @Schema(description = "经度（十进制度，如 116.4074）", example = "116.4074")
    @JsonProperty("longitude")
    private String longitude;

    @Schema(description = "南北半球指示：N=北纬 / S=南纬", example = "N")
    @JsonProperty("ns_indicator")
    @JsonAlias({"ns", "ns_indicator"})
    private String nsIndicator;

    @Schema(description = "东西半球指示：E=东经 / W=西经", example = "E")
    @JsonProperty("ew_indicator")
    @JsonAlias({"ew", "ew_indicator"})
    private String ewIndicator;

    @Schema(description = "海拔高度（米）", example = "50.0")
    @JsonProperty("altitude")
    private String altitude;

    @Schema(description = "速度（节，1节≈1.852km/h）", example = "12.5")
    @JsonProperty("speed")
    private String speed;

    @Schema(description = "航向角/方向（0-359°，正北为0）", example = "270.0")
    @JsonProperty("course")
    private String course;

    @Schema(description = "参与定位的卫星数量", example = "10")
    @JsonProperty("satellites")
    private String satellites;

    @Schema(description = "UTC 日期（DDMMYY 格式）", example = "070326")
    @JsonProperty("date")
    private String date;

    @Schema(description = "UTC 时间（HHMMSS.sss 格式）", example = "033001.000")
    @JsonProperty("time")
    private String time;
}
