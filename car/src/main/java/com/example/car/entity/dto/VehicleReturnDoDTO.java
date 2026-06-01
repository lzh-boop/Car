package com.example.car.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 执行还车请求 DTO
 *
 * 安全说明：只暴露驾驶员/调度员实际填写的字段。
 * 关键关联字段（vehicleId 车辆ID、vehicleNo 车牌号、dispatchId 调度ID、
 * dispatchNo 调度单号、driverId 驾驶员ID、driverName 驾驶员姓名、
 * returnStatus 还车状态）均从数据库已有记录加载，前端无法篡改关联关系。
 */
@Data
@Schema(description = "执行还车请求")
public class VehicleReturnDoDTO {

    @NotNull(message = "还车记录ID不能为空")
    @Schema(description = "还车记录ID")
    private Long id;

    @Schema(description = "还车实际里程（公里）")
    private Integer mileageAfter;

    /**
     * 油量状态：0-充足  1-偏少  2-需加油
     */
    @Schema(description = "油量状态（0-充足，1-偏少，2-需加油）")
    private Integer fuelLevel;

    /**
     * 车辆状况：0-正常  1-轻微损伤  2-需维修
     */
    @Schema(description = "车辆状况（0-正常，1-轻微损伤，2-需维修）")
    private Integer vehicleCondition;

    @Schema(description = "备注（损伤情况说明等）")
    private String remark;
}
