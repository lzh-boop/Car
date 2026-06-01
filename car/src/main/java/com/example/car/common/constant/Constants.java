package com.example.car.common.constant;

/**
 * 系统常量类
 */
public class Constants {
    
    /**
     * 用户状态
     */
    public static class UserStatus {
        public static final Integer DISABLED = 0;  // 禁用
        public static final Integer ENABLED = 1;   // 启用
    }
    
    /**
     * 车辆状态
     */
    public static class VehicleStatus {
        public static final Integer FREE = 0;        // 空闲
        public static final Integer IN_USE = 1;      // 在用
        public static final Integer MAINTENANCE = 2; // 维修
        public static final Integer SCRAPPED = 3;    // 报废
    }
    
    /**
     * 申请状态
     */
    public static class ApplyStatus {
        public static final Integer PENDING = 0;   // 待审批
        public static final Integer APPROVED = 1;  // 已通过
        public static final Integer REJECTED = 2;  // 已拒绝
        public static final Integer CANCELLED = 3; // 已取消
    }
    
    /**
     * 调度状态
     */
    public static class DispatchStatus {
        public static final Integer PENDING = 0;      // 待出车
        public static final Integer IN_PROGRESS = 1;  // 行驶中
        public static final Integer COMPLETED = 2;    // 已完成
        public static final Integer CANCELLED = 3;    // 已取消
    }
    
    /**
     * 行程状态
     */
    public static class TripStatus {
        public static final Integer RUNNING = 1;   // 进行中
        public static final Integer ENDED = 2;     // 已结束
    }
    
    /**
     * 围栏类型
     */
    public static class FenceType {
        public static final Integer CIRCLE = 1;    // 圆形
        public static final Integer POLYGON = 2;   // 多边形
        public static final Integer RECTANGLE = 3; // 矩形
    }
    
    /**
     * 围栏报警类型
     */
    public static class AlarmType {
        public static final Integer ENTER = 1;  // 进入
        public static final Integer LEAVE = 2;  // 离开
        public static final Integer BOTH = 3;   // 进出都报警
    }
    
    /**
     * Redis Key前缀
     */
    public static class RedisKey {
        public static final String VEHICLE_LOCATION = "vehicle:location:";     // 车辆实时位置
        public static final String VEHICLE_ONLINE = "vehicle:online:";         // 车辆在线状态
        public static final String USER_TOKEN = "user:token:";                 // 用户Token
        public static final String CAPTCHA = "captcha:";                       // 验证码
    }
    
    /**
     * JWT相关
     */
    public static class Jwt {
        public static final String TOKEN_HEADER = "Authorization";
        public static final String TOKEN_PREFIX = "Bearer ";
        public static final String CLAIM_USER_ID = "userId";
        public static final String CLAIM_USERNAME = "username";
        public static final String CLAIM_ROLES = "roles";
    }
}
