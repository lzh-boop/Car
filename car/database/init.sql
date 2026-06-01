-- =============================================
-- 公务车北斗定位管理系统 - 数据库初始化脚本
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS vehicle_gps DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE vehicle_gps;

-- =============================================
-- 系统管理模块
-- =============================================

-- 用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(200) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) COMMENT '手机号',
    email VARCHAR(100) COMMENT '邮箱',
    dept_id BIGINT COMMENT '部门ID',
    avatar VARCHAR(200) COMMENT '头像',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    role VARCHAR(32) NOT NULL DEFAULT 'USER' COMMENT '用户角色: ADMIN=管理员, USER=普通用户',
    create_by BIGINT COMMENT '创建人',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_by BIGINT COMMENT '更新人',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_dept (dept_id),
    INDEX idx_phone (phone)
) COMMENT '用户表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 角色表
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    role_key VARCHAR(50) NOT NULL COMMENT '角色标识（允许重复）',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '角色表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 用户角色关联表
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (user_id, role_id)
) COMMENT '用户角色关联表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 部门表
DROP TABLE IF EXISTS sys_dept;
CREATE TABLE sys_dept (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '部门ID',
    parent_id BIGINT DEFAULT 0 COMMENT '父部门ID',
    ancestors VARCHAR(500) DEFAULT '' COMMENT '祖级列表',
    dept_name VARCHAR(100) NOT NULL COMMENT '部门名称',
    order_num INT DEFAULT 0 COMMENT '显示顺序',
    leader VARCHAR(50) COMMENT '负责人',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    status TINYINT DEFAULT 1 COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_parent (parent_id)
) COMMENT '部门表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- 车辆管理模块
-- =============================================

-- 车辆信息表
DROP TABLE IF EXISTS vehicle_info;
CREATE TABLE vehicle_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '车辆ID',
    vehicle_no VARCHAR(20) NOT NULL UNIQUE COMMENT '车牌号',
    vehicle_type VARCHAR(50) COMMENT '车辆类型',
    brand VARCHAR(50) COMMENT '品牌',
    model VARCHAR(50) COMMENT '型号',
    color VARCHAR(20) COMMENT '颜色',
    vin VARCHAR(50) COMMENT '车架号',
    engine_no VARCHAR(50) COMMENT '发动机号',
    purchase_date DATE COMMENT '购置日期',
    purchase_price DECIMAL(10,2) COMMENT '购置价格',
    dept_id BIGINT COMMENT '所属部门',
    status TINYINT DEFAULT 0 COMMENT '状态 0-空闲 1-在用 2-维修 3-报废',
    seats INT DEFAULT 5 COMMENT '座位数',
    mileage INT DEFAULT 0 COMMENT '里程(km)',
    remark VARCHAR(500) COMMENT '备注',
    terminal_no VARCHAR(50) COMMENT '北斗终端编号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_vehicle_no (vehicle_no),
    INDEX idx_dept (dept_id),
    INDEX idx_terminal (terminal_no)
) COMMENT '车辆信息表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 驾驶员信息表
DROP TABLE IF EXISTS driver_info;
CREATE TABLE driver_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '驾驶员ID',
    driver_name VARCHAR(50) NOT NULL COMMENT '姓名',
    phone VARCHAR(20) COMMENT '手机号',
    id_card VARCHAR(18) COMMENT '身份证号',
    license_no VARCHAR(50) COMMENT '驾驶证号',
    license_type VARCHAR(10) COMMENT '准驾车型',
    license_date DATE COMMENT '领证日期',
    dept_id BIGINT COMMENT '所属部门',
    status TINYINT DEFAULT 1 COMMENT '状态 1-正常 2-停用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_phone (phone),
    INDEX idx_license (license_no)
) COMMENT '驾驶员信息表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- 用车申请模块
-- =============================================

-- 用车申请表
DROP TABLE IF EXISTS vehicle_apply;
CREATE TABLE vehicle_apply (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '申请ID',
    apply_no VARCHAR(50) NOT NULL UNIQUE COMMENT '申请单号',
    vehicle_no VARCHAR(20) COMMENT '车牌号',
    apply_user_id BIGINT NOT NULL COMMENT '申请人ID',
    apply_user_name VARCHAR(50) COMMENT '申请人姓名',
    dept_id BIGINT COMMENT '申请部门',
    dept_name VARCHAR(100) COMMENT '部门名称',
    purpose VARCHAR(500) COMMENT '用车事由',
    passenger_count INT COMMENT '乘车人数',
    passenger_names VARCHAR(500) COMMENT '乘车人员',
    start_location VARCHAR(200) COMMENT '出发地',
    end_location VARCHAR(200) COMMENT '目的地',
    plan_start_time DATETIME COMMENT '计划开始时间',
    plan_end_time DATETIME COMMENT '计划结束时间',
    apply_status TINYINT DEFAULT 0 COMMENT '状态 0-待审批 1-已通过 2-已拒绝 3-已取消',
    apply_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    remark VARCHAR(500) COMMENT '备注',
    INDEX idx_apply_no (apply_no),
    INDEX idx_apply_user (apply_user_id),
    INDEX idx_status (apply_status),
    INDEX idx_apply_time (apply_time)
) COMMENT '用车申请表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 审批记录表
DROP TABLE IF EXISTS apply_approve;
CREATE TABLE apply_approve (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '审批ID',
    apply_id BIGINT NOT NULL COMMENT '申请单ID',
    approver_id BIGINT NOT NULL COMMENT '审批人ID',
    approver_name VARCHAR(50) COMMENT '审批人姓名',
    approve_level INT COMMENT '审批级别',
    approve_result TINYINT COMMENT '审批结果 1-通过 2-拒绝',
    approve_opinion VARCHAR(500) COMMENT '审批意见',
    approve_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间',
    INDEX idx_apply (apply_id),
    INDEX idx_approver (approver_id)
) COMMENT '审批记录表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- 车辆调度模块
-- =============================================

-- 车辆调度表
DROP TABLE IF EXISTS vehicle_dispatch;
CREATE TABLE vehicle_dispatch (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '调度ID',
    apply_id BIGINT COMMENT '关联申请单',
    dispatch_no VARCHAR(50) NOT NULL UNIQUE COMMENT '调度单号',
    vehicle_id BIGINT NOT NULL COMMENT '车辆ID',
    vehicle_no VARCHAR(20) COMMENT '车牌号',
    driver_id BIGINT COMMENT '驾驶员ID',
    driver_name VARCHAR(50) COMMENT '驾驶员姓名',
    start_location VARCHAR(200) COMMENT '出发地',
    end_location VARCHAR(200) COMMENT '目的地',
    plan_start_time DATETIME COMMENT '计划开始时间',
    plan_end_time DATETIME COMMENT '计划结束时间',
    actual_start_time DATETIME COMMENT '实际开始时间',
    actual_end_time DATETIME COMMENT '实际结束时间',
    dispatch_status TINYINT DEFAULT 0 COMMENT '状态 0-待出车 1-行驶中 2-已完成 3-已取消',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_vehicle (vehicle_id),
    INDEX idx_driver (driver_id),
    INDEX idx_status (dispatch_status),
    INDEX idx_plan_time (plan_start_time)
) COMMENT '车辆调度表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 还车记录表
DROP TABLE IF EXISTS vehicle_return;
CREATE TABLE vehicle_return (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '还车记录ID',
    dispatch_id BIGINT COMMENT '调度单ID',
    dispatch_no VARCHAR(50) COMMENT '调度单号',
    vehicle_id BIGINT COMMENT '车辆ID',
    vehicle_no VARCHAR(20) COMMENT '车牌号',
    driver_id BIGINT COMMENT '驾驶员ID',
    driver_name VARCHAR(50) COMMENT '驾驶员姓名',
    end_location VARCHAR(200) COMMENT '还车地点',
    plan_end_time DATETIME COMMENT '计划结束时间',
    actual_end_time DATETIME COMMENT '实际还车时间',
    mileage_before INT COMMENT '还车前里程',
    mileage_after INT COMMENT '还车后里程',
    fuel_level TINYINT COMMENT '油量状态：0-充足 1-偏少 2-需加油',
    vehicle_condition TINYINT COMMENT '车辆状况：0-正常 1-轻微损伤 2-需维修',
    remark VARCHAR(500) COMMENT '备注',
    return_status TINYINT DEFAULT 0 COMMENT '还车状态：0-待还车 1-已还车',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_dispatch (dispatch_id),
    INDEX idx_vehicle (vehicle_id),
    INDEX idx_return_status (return_status)
) COMMENT '还车记录表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- GPS定位模块
-- =============================================

-- GPS定位数据表
DROP TABLE IF EXISTS gps_location;
CREATE TABLE gps_location (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '定位ID',
    terminal_no VARCHAR(50) NOT NULL COMMENT '终端编号',
    vehicle_id BIGINT NOT NULL COMMENT '车辆ID',
    vehicle_no VARCHAR(20) COMMENT '车牌号',
    longitude DECIMAL(10, 6) NOT NULL COMMENT '经度',
    latitude DECIMAL(10, 6) NOT NULL COMMENT '纬度',
    speed DECIMAL(5, 2) DEFAULT 0 COMMENT '速度 km/h',
    direction INT DEFAULT 0 COMMENT '方向 0-359度',
    altitude DECIMAL(8, 2) COMMENT '海拔',
    location_time DATETIME NOT NULL COMMENT '定位时间',
    receive_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '接收时间',
    address VARCHAR(200) COMMENT '地址',
    signal_strength INT COMMENT '信号强度（0-100）',
    satellite_count INT COMMENT '卫星数量',
    INDEX idx_terminal (terminal_no),
    INDEX idx_vehicle (vehicle_id),
    INDEX idx_location_time (location_time)
) COMMENT 'GPS定位数据表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 历史轨迹表
DROP TABLE IF EXISTS gps_track_history;
CREATE TABLE gps_track_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '轨迹ID',
    vehicle_id BIGINT NOT NULL COMMENT '车辆ID',
    vehicle_no VARCHAR(20) COMMENT '车牌号',
    track_date DATE NOT NULL COMMENT '轨迹日期',
    track_data JSON COMMENT '轨迹数据JSON',
    total_distance DECIMAL(10, 2) DEFAULT 0 COMMENT '总里程',
    point_count INT DEFAULT 0 COMMENT '轨迹点数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_vehicle_date (vehicle_id, track_date)
) COMMENT '历史轨迹表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 电子围栏表
DROP TABLE IF EXISTS geo_fence;
CREATE TABLE geo_fence (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '围栏ID',
    fence_name VARCHAR(100) NOT NULL COMMENT '围栏名称',
    fence_type TINYINT COMMENT '类型 1-圆形 2-多边形 3-矩形',
    center_point VARCHAR(100) COMMENT '中心点坐标（经度,纬度）',
    radius INT COMMENT '半径（米）',
    polygon_points TEXT COMMENT '多边形顶点坐标JSON',
    bind_vehicles TEXT COMMENT '绑定车辆ID，JSON数组',
    alarm_type TINYINT DEFAULT 3 COMMENT '报警类型 1-进入 2-离开 3-进出都报警',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status)
) COMMENT '电子围栏表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 围栏报警记录表
DROP TABLE IF EXISTS fence_alarm;
CREATE TABLE fence_alarm (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报警ID',
    fence_id BIGINT NOT NULL COMMENT '围栏ID',
    fence_name VARCHAR(100) COMMENT '围栏名称',
    vehicle_id BIGINT NOT NULL COMMENT '车辆ID',
    vehicle_no VARCHAR(20) COMMENT '车牌号',
    alarm_type TINYINT COMMENT '1-进入 2-离开',
    alarm_time DATETIME NOT NULL COMMENT '报警时间',
    longitude DECIMAL(10, 6) COMMENT '经度',
    latitude DECIMAL(10, 6) COMMENT '纬度',
    address VARCHAR(200) COMMENT '地址',
    is_handled TINYINT DEFAULT 0 COMMENT '是否处理 0-未处理 1-已处理',
    handler_id BIGINT COMMENT '处理人ID',
    handle_time DATETIME COMMENT '处理时间',
    handle_remark VARCHAR(500) COMMENT '处理说明',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_vehicle (vehicle_id),
    INDEX idx_alarm_time (alarm_time),
    INDEX idx_handled (is_handled)
) COMMENT '围栏报警记录表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- 行程管理模块
-- =============================================

-- 行程记录表
DROP TABLE IF EXISTS trip_record;
CREATE TABLE trip_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '行程ID',
    dispatch_id BIGINT COMMENT '调度单ID',
    vehicle_id BIGINT NOT NULL COMMENT '车辆ID',
    vehicle_no VARCHAR(20) COMMENT '车牌号',
    driver_id BIGINT COMMENT '驾驶员ID',
    driver_name VARCHAR(50) COMMENT '驾驶员姓名',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    start_location VARCHAR(200) COMMENT '起点',
    start_longitude DECIMAL(10, 6) COMMENT '起点经度',
    start_latitude DECIMAL(10, 6) COMMENT '起点纬度',
    end_location VARCHAR(200) COMMENT '终点',
    end_longitude DECIMAL(10, 6) COMMENT '终点经度',
    end_latitude DECIMAL(10, 6) COMMENT '终点纬度',
    start_mileage DECIMAL(10, 2) COMMENT '起始里程',
    end_mileage DECIMAL(10, 2) COMMENT '结束里程',
    trip_distance DECIMAL(10, 2) DEFAULT 0 COMMENT '行程里程',
    fuel_consumption DECIMAL(8, 2) COMMENT '油耗（升）',
    trip_status TINYINT DEFAULT 1 COMMENT '1-进行中 2-已结束',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_vehicle (vehicle_id),
    INDEX idx_driver (driver_id),
    INDEX idx_start_time (start_time),
    INDEX idx_status (trip_status)
) COMMENT '行程记录表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- 车辆维护模块
-- =============================================

-- 车辆维护记录表
DROP TABLE IF EXISTS vehicle_maintenance;
CREATE TABLE vehicle_maintenance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '维护ID',
    vehicle_id BIGINT NOT NULL COMMENT '车辆ID',
    vehicle_no VARCHAR(20) COMMENT '车牌号',
    maintenance_type TINYINT COMMENT '1-保养 2-维修 3-年检 4-保险',
    maintenance_date DATE COMMENT '维护日期',
    maintenance_item VARCHAR(200) COMMENT '维护项目',
    maintenance_cost DECIMAL(10, 2) DEFAULT 0 COMMENT '费用',
    current_mileage DECIMAL(10, 2) COMMENT '当前里程',
    service_provider VARCHAR(100) COMMENT '服务商',
    next_maintenance_date DATE COMMENT '下次保养日期',
    next_maintenance_mileage DECIMAL(10, 2) COMMENT '下次保养里程',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_vehicle (vehicle_id),
    INDEX idx_date (maintenance_date)
) COMMENT '车辆维护记录表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- 系统管理模块
-- =============================================

-- 操作日志表
DROP TABLE IF EXISTS sys_log;
CREATE TABLE sys_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '用户名',
    operation VARCHAR(200) COMMENT '操作内容',
    method VARCHAR(200) COMMENT '方法名',
    params TEXT COMMENT '请求参数',
    result TEXT COMMENT '返回结果',
    ip VARCHAR(50) COMMENT 'IP地址',
    location VARCHAR(100) COMMENT '操作地点',
    execute_time BIGINT COMMENT '执行时长（毫秒）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user (user_id),
    INDEX idx_create_time (create_time)
) COMMENT '操作日志表' ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =============================================
-- 初始化数据
-- =============================================

-- 插入默认管理员（密码：admin123，使用BCrypt加密）
INSERT INTO sys_user (username, password, real_name, phone, email, dept_id, status, role)
VALUES ('admin', '$2a$10$1yuaRmHaSVhYUi/wh1pDDeg3tGydZPZjCvGblCWCZJXBw6SC6yBKS', '系统管理员', '13800138000', 'admin@example.com', 1, 1, 'ADMIN');

-- 插入默认角色
INSERT INTO sys_role (role_name, role_key, sort, status)
VALUES 
('超级管理员', 'admin', 1, 1),
('调度员', 'dispatcher', 2, 1),
('驾驶员', 'driver', 3, 1),
('普通用户', 'user', 4, 1);

-- 绑定管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 插入默认部门
INSERT INTO sys_dept (parent_id, dept_name, order_num, leader, phone, status)
VALUES 
(0, '某某单位', 0, '张三', '13800138000', 1),
(1, '办公室', 1, '李四', '13800138001', 1),
(1, '业务部', 2, '王五', '13800138002', 1),
(1, '后勤部', 3, '赵六', '13800138003', 1);

-- 更新部门祖级列表
UPDATE sys_dept SET ancestors = '0' WHERE parent_id = 0;
UPDATE sys_dept SET ancestors = '0,1' WHERE parent_id = 1;

-- 插入测试车辆数据
INSERT INTO vehicle_info (vehicle_no, vehicle_type, brand, model, color, vin, purchase_date, purchase_price, dept_id, status, terminal_no)
VALUES 
('京A12345', '轿车', '大众', '帕萨特', '黑色', 'LSVAA2183E2123456', '2023-01-15', 180000.00, 2, 1, 'BD001'),
('京A23456', 'SUV', '丰田', '汉兰达', '白色', 'JTMAZ1FJ0F2123457', '2023-03-20', 280000.00, 3, 1, 'BD002'),
('京A34567', '商务车', '别克', 'GL8', '银色', 'LSGGH54U8ES123458', '2023-05-10', 250000.00, 4, 1, 'BD003');

-- 插入测试驾驶员数据
INSERT INTO driver_info (driver_name, phone, id_card, license_no, license_type, license_date, dept_id, status)
VALUES 
('张驾驶', '13900139001', '110101199001011234', 'J110101199001011234', 'C1', '2010-06-15', 4, 1),
('李驾驶', '13900139002', '110101199002021234', 'J110101199002021234', 'C1', '2012-08-20', 4, 1),
('王驾驶', '13900139003', '110101199003031234', 'J110101199003031234', 'B1', '2008-04-10', 4, 1);

-- 插入用车申请测试数据
INSERT INTO vehicle_apply (apply_no, apply_user_id, apply_user_name, dept_id, dept_name, purpose, passenger_count, passenger_names, start_location, end_location, plan_start_time, plan_end_time, apply_status, apply_time, remark)
VALUES 
('AP202401001', 1, '系统管理员', 2, '办公室', '参加市政府会议', 3, '张三,李四,王五', '某某单位', '市政府大楼', '2024-01-15 09:00:00', '2024-01-15 17:00:00', 1, '2024-01-14 10:00:00', '需准时到达'),
('AP202401002', 1, '系统管理员', 3, '业务部', '外出考察学习', 5, '赵六,孙七,周八,吴九,郑十', '某某单位', '友好单位', '2024-01-18 08:00:00', '2024-01-18 18:00:00', 1, '2024-01-16 14:30:00', '往返行程'),
('AP202401003', 1, '系统管理员', 4, '后勤部', '接送贵宾', 2, '领导,秘书', '机场', '某某单位', '2024-01-20 14:00:00', '2024-01-20 16:00:00', 2, '2024-01-19 09:00:00', '航班延误可能性大'),
('AP202401004', 1, '系统管理员', 2, '办公室', '紧急公务', 1, '张三', '某某单位', '市医院', '2024-01-22 10:00:00', '2024-01-22 12:00:00', 1, '2024-01-21 16:00:00', NULL);

-- 插入审批记录测试数据
INSERT INTO apply_approve (apply_id, approver_id, approver_name, approve_level, approve_result, approve_opinion, approve_time)
VALUES 
(1, 1, '系统管理员', 1, 1, '同意', '2024-01-14 11:00:00'),
(2, 1, '系统管理员', 1, 1, '批准，注意安全', '2024-01-16 15:00:00'),
(3, 1, '系统管理员', 1, 2, '该时段车辆紧张，建议改期', '2024-01-19 10:00:00'),
(4, 1, '系统管理员', 1, 1, '同意，优先安排', '2024-01-21 17:00:00');

-- 插入车辆调度测试数据
INSERT INTO vehicle_dispatch (apply_id, dispatch_no, vehicle_id, vehicle_no, driver_id, driver_name, start_location, end_location, plan_start_time, plan_end_time, actual_start_time, actual_end_time, dispatch_status, create_time)
VALUES 
(1, 'DS202401001', 1, '京A12345', 1, '张驾驶', '某某单位', '市政府大楼', '2024-01-15 09:00:00', '2024-01-15 17:00:00', '2024-01-15 08:55:00', '2024-01-15 17:10:00', 2, '2024-01-14 11:30:00'),
(2, 'DS202401002', 2, '京A23456', 2, '李驾驶', '某某单位', '友好单位', '2024-01-18 08:00:00', '2024-01-18 18:00:00', '2024-01-18 07:50:00', '2024-01-18 18:20:00', 2, '2024-01-16 15:30:00'),
(4, 'DS202401003', 3, '京A34567', 3, '王驾驶', '某某单位', '市医院', '2024-01-22 10:00:00', '2024-01-22 12:00:00', '2024-01-22 09:55:00', NULL, 1, '2024-01-21 17:30:00'),
(NULL, 'DS202401004', 1, '京A12345', 1, '张驾驶', '某某单位', '开发区管委会', '2024-01-25 14:00:00', '2024-01-25 17:00:00', NULL, NULL, 0, '2024-01-24 10:00:00');

-- 插入GPS定位测试数据（最新定位）
INSERT INTO gps_location (terminal_no, vehicle_id, vehicle_no, longitude, latitude, speed, direction, altitude, location_time, address)
VALUES 
('BD001', 1, '京A12345', 116.397428, 39.904200, 0.00, 0, 50.5, '2024-01-22 08:30:00', '北京市东城区某某路123号'),
('BD002', 2, '京A23456', 116.407526, 39.904200, 35.50, 90, 52.0, '2024-01-22 08:30:00', '北京市朝阳区某某大街456号'),
('BD003', 3, '京A34567', 116.380000, 39.910000, 45.00, 180, 48.0, '2024-01-22 10:15:00', '北京市西城区某某医院附近');

-- 插入历史轨迹测试数据
INSERT INTO gps_track_history (vehicle_id, vehicle_no, track_date, track_data, total_distance, point_count, create_time)
VALUES 
(1, '京A12345', '2024-01-15', '[{"lng":116.397428,"lat":39.904200,"time":"2024-01-15 08:55:00"},{"lng":116.407428,"lat":39.914200,"time":"2024-01-15 09:15:00"},{"lng":116.417428,"lat":39.924200,"time":"2024-01-15 09:35:00"}]', 45.80, 120, '2024-01-15 18:00:00'),
(2, '京A23456', '2024-01-18', '[{"lng":116.397428,"lat":39.904200,"time":"2024-01-18 07:50:00"},{"lng":116.507428,"lat":40.004200,"time":"2024-01-18 09:30:00"},{"lng":116.607428,"lat":40.104200,"time":"2024-01-18 11:00:00"}]', 182.50, 350, '2024-01-18 19:00:00'),
(1, '京A12345', '2024-01-20', '[{"lng":116.397428,"lat":39.904200,"time":"2024-01-20 10:00:00"},{"lng":116.417428,"lat":39.924200,"time":"2024-01-20 10:30:00"}]', 28.30, 80, '2024-01-20 18:00:00');

-- 插入电子围栏测试数据
INSERT INTO geo_fence (fence_name, fence_type, center_point, radius, polygon_points, bind_vehicles, alarm_type, status, create_time)
VALUES 
('单位办公区围栏', 1, '116.397428,39.904200', 500, NULL, '[1,2,3]', 2, 1, '2024-01-10 09:00:00'),
('市区限行区域', 2, NULL, NULL, '[{"lng":116.380000,"lat":39.900000},{"lng":116.420000,"lat":39.900000},{"lng":116.420000,"lat":39.920000},{"lng":116.380000,"lat":39.920000}]', '[1,2]', 1, 1, '2024-01-10 10:00:00'),
('高速服务区', 1, '116.500000,40.000000', 1000, NULL, '[2]', 3, 1, '2024-01-10 11:00:00');

-- 插入围栏报警测试数据
INSERT INTO fence_alarm (fence_id, fence_name, vehicle_id, vehicle_no, alarm_type, alarm_time, longitude, latitude, address, is_handled, handler_id, handle_time, handle_remark, create_time)
VALUES 
(1, '单位办公区围栏', 1, '京A12345', 2, '2024-01-15 08:55:00', 116.397428, 39.904200, '北京市东城区某某路123号', 1, 1, '2024-01-15 09:00:00', '正常出车', '2024-01-15 08:55:00'),
(1, '单位办公区围栏', 1, '京A12345', 1, '2024-01-15 17:10:00', 116.397428, 39.904200, '北京市东城区某某路123号', 1, 1, '2024-01-15 17:15:00', '正常返回', '2024-01-15 17:10:00'),
(2, '市区限行区域', 2, '京A23456', 1, '2024-01-18 09:30:00', 116.400000, 39.910000, '北京市东城区限行区域', 1, 1, '2024-01-18 10:00:00', '执行公务，已报备', '2024-01-18 09:30:00'),
(3, '高速服务区', 2, '京A23456', 1, '2024-01-18 10:00:00', 116.500000, 40.000000, '京藏高速某服务区', 0, NULL, NULL, NULL, '2024-01-18 10:00:00');

-- 插入行程记录测试数据
INSERT INTO trip_record (dispatch_id, vehicle_id, vehicle_no, driver_id, driver_name, start_time, end_time, start_location, start_longitude, start_latitude, end_location, end_longitude, end_latitude, start_mileage, end_mileage, trip_distance, fuel_consumption, trip_status, create_time)
VALUES 
(1, 1, '京A12345', 1, '张驾驶', '2024-01-15 08:55:00', '2024-01-15 17:10:00', '某某单位', 116.397428, 39.904200, '市政府大楼', 116.417428, 39.924200, 15230.50, 15276.30, 45.80, 5.2, 2, '2024-01-15 08:55:00'),
(2, 2, '京A23456', 2, '李驾驶', '2024-01-18 07:50:00', '2024-01-18 18:20:00', '某某单位', 116.397428, 39.904200, '友好单位', 116.607428, 40.104200, 8560.20, 8742.70, 182.50, 18.5, 2, '2024-01-18 07:50:00'),
(3, 3, '京A34567', 3, '王驾驶', '2024-01-22 09:55:00', NULL, '某某单位', 116.397428, 39.904200, '市医院', 116.380000, 39.910000, 12450.00, NULL, NULL, NULL, 1, '2024-01-22 09:55:00');

-- 插入车辆维护记录测试数据
INSERT INTO vehicle_maintenance (vehicle_id, vehicle_no, maintenance_type, maintenance_date, maintenance_item, maintenance_cost, current_mileage, service_provider, next_maintenance_date, next_maintenance_mileage, remark, create_time)
VALUES 
(1, '京A12345', 1, '2023-12-15', '定期保养：更换机油、机滤、空滤', 680.00, 15000.00, '4S店-大众汽车', '2024-06-15', 20000.00, '保养情况良好', '2023-12-15 16:00:00'),
(2, '京A23456', 3, '2024-01-05', '年检', 350.00, 8500.00, '车管所指定检测站', '2025-01-05', NULL, '年检通过', '2024-01-05 14:00:00'),
(1, '京A12345', 2, '2024-01-10', '维修：更换前刹车片', 1200.00, 15200.00, '4S店-大众汽车', NULL, NULL, '刹车片磨损严重', '2024-01-10 10:00:00'),
(3, '京A34567', 4, '2023-11-20', '保险续保：交强险+商业险', 5800.00, 12000.00, '中国平安保险', '2024-11-20', NULL, '全险', '2023-11-20 09:00:00');

-- 插入系统操作日志测试数据
INSERT INTO sys_log (user_id, username, operation, method, params, result, ip, location, execute_time, create_time)
VALUES 
(1, 'admin', '用户登录', 'com.example.car.controller.system.AuthController.login()', '{"username":"admin"}', '{"code":200,"message":"登录成功"}', '192.168.1.100', '内网IP', 125, '2024-01-15 08:30:00'),
(1, 'admin', '查询车辆列表', 'com.example.car.controller.vehicle.VehicleController.pageQuery()', '{"pageNum":1,"pageSize":10}', '{"code":200,"data":{...}}', '192.168.1.100', '内网IP', 85, '2024-01-15 08:35:00'),
(1, 'admin', '新增用车申请', 'com.example.car.controller.apply.VehicleApplyController.add()', '{"purpose":"参加会议",...}', '{"code":200,"message":"操作成功"}', '192.168.1.100', '内网IP', 156, '2024-01-15 09:00:00'),
(1, 'admin', '审批用车申请', 'com.example.car.controller.apply.VehicleApplyController.approve()', '{"applyId":1,"result":1}', '{"code":200,"message":"审批成功"}', '192.168.1.100', '内网IP', 98, '2024-01-15 09:15:00'),
(1, 'admin', '创建车辆调度', 'com.example.car.controller.dispatch.DispatchController.add()', '{"vehicleId":1,"driverId":1,...}', '{"code":200,"message":"操作成功"}', '192.168.1.100', '内网IP', 112, '2024-01-15 09:30:00');

COMMIT;

-- =============================================
-- 数据库初始化完成
-- =============================================
