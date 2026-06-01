package com.example.car.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.car.common.exception.BusinessException;
import com.example.car.common.util.AmapGeocodingUtil;
import com.example.car.entity.BeidouLocation;
import com.example.car.entity.VehicleInfo;
import com.example.car.entity.dto.BeidouLocationQueryDTO;
import com.example.car.entity.dto.DeviceReportDTO;
import com.example.car.entity.vo.BeidouLocationVO;
import com.example.car.mapper.BeidouLocationMapper;
import com.example.car.mapper.VehicleInfoMapper;
import com.example.car.service.BeidouLocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
/**
 * 北斗定位服务实现类
 * 接收北斗终端上报的经纬度数据，通过高德逆地理编码 API 自动解析为详细地址存库
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BeidouLocationServiceImpl extends ServiceImpl<BeidouLocationMapper, BeidouLocation>
        implements BeidouLocationService {

    /** 高德逆地理编码工具（Web服务Key调用） */
    private final AmapGeocodingUtil amapGeocodingUtil;

    private final VehicleInfoMapper vehicleInfoMapper;

    // ======================== 分页查询 ========================

    @Override
    public Page<BeidouLocationVO> pageQuery(BeidouLocationQueryDTO queryDTO) {
        LambdaQueryWrapper<BeidouLocation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(queryDTO.getTerminalNo()),
                        BeidouLocation::getTerminalNo, queryDTO.getTerminalNo())
               .eq(queryDTO.getVehicleId() != null,
                        BeidouLocation::getVehicleId, queryDTO.getVehicleId())
               .like(StringUtils.hasText(queryDTO.getVehicleNo()),
                        BeidouLocation::getVehicleNo, queryDTO.getVehicleNo())
               .ge(queryDTO.getStartTime() != null,
                        BeidouLocation::getLocationTime, queryDTO.getStartTime())
               .le(queryDTO.getEndTime() != null,
                        BeidouLocation::getLocationTime, queryDTO.getEndTime())
               .orderByDesc(BeidouLocation::getLocationTime);

        Page<BeidouLocation> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        page = this.page(page, wrapper);

        Page<BeidouLocationVO> voPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        voPage.setRecords(page.getRecords().stream().map(this::convertToVO).toList());
        return voPage;
    }

    // ======================== 获取最新定位 ========================

    @Override
    public BeidouLocationVO getLatestLocation(Long vehicleId) {
        LambdaQueryWrapper<BeidouLocation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BeidouLocation::getVehicleId, vehicleId)
               .orderByDesc(BeidouLocation::getLocationTime)
               .last("LIMIT 1");

        BeidouLocation beidouLocation = this.getOne(wrapper);
        if (beidouLocation == null) {
            throw new BusinessException("未找到车辆北斗定位信息");
        }
        return convertToVO(beidouLocation);
    }

    // ======================== 北斗数据上报（核心：自动逆地理编码） ========================

    /**
     * 保存北斗上报定位数据
     * 1. 先将数据入库（保证实时性，不阻塞上报响应）
     * 2. 异步调用高德逆地理编码解析详细地址，解析完毕后更新到数据库
     */
    @Override
    public boolean saveLocation(BeidouLocation beidouLocation) {
        log.info("【北斗定位上报】终端:{} 车辆:{} 经度:{} 纬度:{} 卫星数:{} 信号强度:{}",
                beidouLocation.getTerminalNo(), beidouLocation.getVehicleNo(),
                beidouLocation.getLongitude(), beidouLocation.getLatitude(),
                beidouLocation.getSatelliteCount(), beidouLocation.getSignalStrength());

        // 1. 先保存到数据库（address 此时为空）
        boolean saved = this.save(beidouLocation);

        if (saved && beidouLocation.getLongitude() != null && beidouLocation.getLatitude() != null) {
            // 2. 异步解析地址并回写数据库（不阻塞上报接口响应）
            asyncResolveAndUpdate(beidouLocation.getId(),
                    beidouLocation.getLongitude(), beidouLocation.getLatitude());
        }
        return saved;
    }

    /**
     * 异步：调用高德逆地理编码，将详细地址更新到数据库
     */
    @Async
    public void asyncResolveAndUpdate(Long id, BigDecimal longitude, BigDecimal latitude) {
        try {
            String address = amapGeocodingUtil.resolveAddress(longitude, latitude);
            if (address != null) {
                this.update(new LambdaUpdateWrapper<BeidouLocation>()
                        .eq(BeidouLocation::getId, id)
                        .set(BeidouLocation::getAddress, address));
                log.info("【北斗地址解析】id={} -> {}", id, address);
            }
        } catch (Exception e) {
            log.error("【北斗地址解析】异步回写失败 id={}: {}", id, e.getMessage());
        }
    }

    // ======================== 单独解析地址接口 ========================

    /**
     * 根据经纬度实时解析详细地址
     * 若传入了记录 ID，解析成功后同时回写数据库
     *
     * @param id        定位记录 ID（可为 null）
     * @param longitude 经度
     * @param latitude  纬度
     * @return 详细地址
     */
    @Override
    public String resolveAddress(Long id, Double longitude, Double latitude) {
        if (longitude == null || latitude == null) {
            throw new BusinessException("经纬度不能为空");
        }

        String address = amapGeocodingUtil.resolveAddress(
                BigDecimal.valueOf(longitude), BigDecimal.valueOf(latitude));

        if (address == null) {
            throw new BusinessException("地址解析失败，请检查坐标是否在中国大陆范围内");
        }

        // 如果传了记录 ID，同步更新数据库
        if (id != null) {
            this.update(new LambdaUpdateWrapper<BeidouLocation>()
                    .eq(BeidouLocation::getId, id)
                    .set(BeidouLocation::getAddress, address));
            log.info("【北斗地址回写】id={} -> {}", id, address);
        }

        return address;
    }

    // ======================== 硬件终端原始数据接收 ========================

    /**
     * 接收硬件设备（如 esp32_sim7670x）上报的原始 JSON，解析并存库
     *
     * 字段映射规则：
     *   device_id  -> terminalNo
     *   latitude   -> latitude  （已是十进制度，直接转 BigDecimal；若为空则跳过）
     *   longitude  -> longitude
     *   ns_indicator / ew_indicator：南纬(S)/西经(W)时将坐标取反
     *   altitude   -> altitude
     *   speed      -> speed（节，不做单位转换，原值存库）
     *   course     -> direction（取整）
     *   satellites -> satelliteCount
     *   date+time  -> locationTime（UTC，格式 DDMMYY + HHMMSS.sss）
     */
    @Override
    public boolean saveDeviceReport(DeviceReportDTO dto) {
        log.info("【北斗设备上报】原始数据: device_id={} lat={} lng={} ns={} ew={} alt={} speed={} course={} sats={} date={} time={}",
                dto.getDeviceId(), dto.getLatitude(), dto.getLongitude(),
                dto.getNsIndicator(), dto.getEwIndicator(),
                dto.getAltitude(), dto.getSpeed(), dto.getCourse(),
                dto.getSatellites(), dto.getDate(), dto.getTime());

        BeidouLocation loc = new BeidouLocation();

        // 终端编号
        String terminalNo = dto.getDeviceId();
        loc.setTerminalNo(terminalNo);

        // 根据终端编号自动关联车辆信息（vehicleId / vehicleNo）
        if (StringUtils.hasText(terminalNo)) {
            VehicleInfo vehicle = vehicleInfoMapper.selectOne(
                    new LambdaQueryWrapper<VehicleInfo>()
                            .eq(VehicleInfo::getTerminalNo, terminalNo)
                            .last("LIMIT 1")
            );
            if (vehicle != null) {
                loc.setVehicleId(vehicle.getId());
                loc.setVehicleNo(vehicle.getVehicleNo());
                log.info("【北斗设备上报】终端={} 关联车辆: id={} 车牌={}",
                        terminalNo, vehicle.getId(), vehicle.getVehicleNo());
            } else {
                log.warn("【北斗设备上报】终端={} 未找到对应车辆，定位数据将以终端号存库", terminalNo);
            }
        }

        // 经纬度：ESP32 上报的已是十进制度，直接解析
        BigDecimal lat = parseBigDecimal(dto.getLatitude());
        BigDecimal lng = parseBigDecimal(dto.getLongitude());

        if (lat != null && "S".equalsIgnoreCase(trim(dto.getNsIndicator()))) {
            lat = lat.negate();
        }
        if (lng != null && "W".equalsIgnoreCase(trim(dto.getEwIndicator()))) {
            lng = lng.negate();
        }
        loc.setLatitude(lat);
        loc.setLongitude(lng);
        log.info("【北斗设备上报】坐标(十进制度): lat={} lng={}", lat, lng);

        // 高度
        loc.setAltitude(parseBigDecimal(dto.getAltitude()));

        // 速度（节）
        loc.setSpeed(parseBigDecimal(dto.getSpeed()));

        // 方向角
        BigDecimal course = parseBigDecimal(dto.getCourse());
        if (course != null) {
            loc.setDirection(course.intValue());
        }

        // 卫星数量
        Integer sats = parseInt(dto.getSatellites());
        loc.setSatelliteCount(sats);

        // 定位时间：date=DDMMYY, time=HHMMSS.sss（UTC）
        LocalDateTime locationTime = parseUtcDateTime(dto.getDate(), dto.getTime());
        loc.setLocationTime(locationTime);

        log.info("【北斗设备上报】解析结果: 终端={} 纬度={} 经度={} 南北={} 东西={} 高度={}m 速度={}节 航向={}° 卫星数={} 定位时间={}",
                loc.getTerminalNo(),
                loc.getLatitude(), loc.getLongitude(),
                dto.getNsIndicator(), dto.getEwIndicator(),
                loc.getAltitude(), loc.getSpeed(), loc.getDirection(),
                loc.getSatelliteCount(), loc.getLocationTime());

        // GPS 未定位时（经纬度为空），只记录日志不存库，避免 NOT NULL 约束报错
        if (loc.getLatitude() == null || loc.getLongitude() == null) {
            log.warn("【北斗设备上报】终端={} GPS 尚未定位（经纬度为空），本次不存库，等待下次上报", terminalNo);
            return false;
        }

        return saveLocation(loc);
    }

    // ─── 解析工具方法 ───────────────────────────────────────────

    /**
     * NMEA DDMM.MMMM 格式 → 十进制度
     * 纬度：DDMM.MMMM，例 4721.2400 → 47 + 21.2400/60 = 47.354000
     * 经度：DDDMM.MMMM，例 12355.0800 → 123 + 55.0800/60 = 123.918000
     */
    private BigDecimal nmeaToDegree(String val) {
        if (val == null || val.isBlank()) return null;
        try {
            String trimmed = val.trim();
            BigDecimal raw = new BigDecimal(trimmed);
            // 整数部分包含度，小数点前两位是分
            int degrees = raw.intValue() / 100;
            BigDecimal minutes = raw.subtract(BigDecimal.valueOf(degrees * 100L));
            return BigDecimal.valueOf(degrees).add(minutes.divide(BigDecimal.valueOf(60), 8, java.math.RoundingMode.HALF_UP));
        } catch (Exception e) {
            log.warn("【北斗设备上报】NMEA坐标转换失败: '{}'", val);
            return null;
        }
    }

    private BigDecimal parseBigDecimal(String val) {
        if (val == null || val.isBlank()) return null;
        try {
            return new BigDecimal(val.trim());
        } catch (NumberFormatException e) {
            log.warn("【北斗设备上报】数值解析失败: '{}'", val);
            return null;
        }
    }

    private Integer parseInt(String val) {
        if (val == null || val.isBlank()) return null;
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String trim(String val) {
        return val == null ? "" : val.trim();
    }

    /**
     * 解析 NMEA 风格的 UTC 日期时间，自动兼容 date/time 字段互换的情况。
     * 正常：date=DDMMYY（如090426）, time=HHMMSS.sss（如051752.000）
     * ESP32 有时上报时字段互换：date=HHMMSS.sss, time=DDMMYY，自动识别并纠正。
     */
    private LocalDateTime parseUtcDateTime(String date, String time) {
        if (date == null || date.isBlank() || time == null || time.isBlank()) return null;
        try {
            String d = date.trim();
            String t = time.trim();

            // 自动识别：时间字段包含小数点（如 051752.000），日期字段不含小数点
            // 若 d 包含小数点而 t 不含，说明两者互换了，纠正之
            if (d.contains(".") && !t.contains(".")) {
                log.info("【北斗设备上报】检测到 date/time 字段互换，自动纠正: date='{}' time='{}'", d, t);
                String tmp = d;
                d = t;
                t = tmp;
            }

            // 去掉时间字段的小数秒部分（如 051752.000 → 051752）
            if (t.contains(".")) {
                t = t.substring(0, t.indexOf('.'));
            }

            // date: DDMMYY
            int day   = Integer.parseInt(d.substring(0, 2));
            int month = Integer.parseInt(d.substring(2, 4));
            int year  = 2000 + Integer.parseInt(d.substring(4, 6));

            // time: HHMMSS
            int hour   = Integer.parseInt(t.substring(0, 2));
            int minute = Integer.parseInt(t.substring(2, 4));
            int second = Integer.parseInt(t.substring(4, 6));

            // UTC 转北京时间（+8）
            return LocalDateTime.of(year, month, day, hour, minute, second)
                    .atOffset(ZoneOffset.UTC)
                    .atZoneSameInstant(java.time.ZoneId.of("Asia/Shanghai"))
                    .toLocalDateTime();
        } catch (Exception e) {
            log.warn("【北斗设备上报】日期时间解析失败: date='{}' time='{}'，使用当前时间代替", date, time);
            // 解析失败时用当前服务器时间，避免 NOT NULL 约束报错
            return LocalDateTime.now();
        }
    }

    // ======================== 工具方法 ========================

    private BeidouLocationVO convertToVO(BeidouLocation beidouLocation) {
        BeidouLocationVO vo = new BeidouLocationVO();
        BeanUtils.copyProperties(beidouLocation, vo);
        return vo;
    }
}
