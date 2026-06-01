package com.example.car.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.car.entity.BeidouLocation;
import com.example.car.entity.dto.BeidouLocationQueryDTO;
import com.example.car.entity.dto.DeviceReportDTO;
import com.example.car.entity.vo.BeidouLocationVO;

public interface BeidouLocationService extends IService<BeidouLocation> {

    Page<BeidouLocationVO> pageQuery(BeidouLocationQueryDTO queryDTO);

    BeidouLocationVO getLatestLocation(Long vehicleId);

    /**
     * 北斗终端上报定位数据（自动调用高德逆地理编码填充详细地址）
     */
    boolean saveLocation(BeidouLocation beidouLocation);

    /**
     * 接收硬件终端原始上报 JSON，解析后存库
     * 支持 esp32_sim7670x 等设备格式：
     * { "device_id","latitude","longitude","ns_indicator","ew_indicator",
     *   "altitude","speed","course","satellites","date","time" }
     */
    boolean saveDeviceReport(DeviceReportDTO dto);

    /**
     * 根据经纬度调用高德逆地理编码，返回详细地址
     * 同时将解析结果回写到数据库（按记录ID更新）
     *
     * @param id        定位记录 ID（可为 null，为 null 时只返回地址不更新库）
     * @param longitude 经度
     * @param latitude  纬度
     * @return 详细地址字符串
     */
    String resolveAddress(Long id, Double longitude, Double latitude);
}
