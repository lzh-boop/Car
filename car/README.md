# 公务车北斗定位管理系统

## 项目介绍

这是一个基于 **Spring Boot 3** 和 **Vue 3** 开发的公务车北斗定位管理系统，实现了车辆实时定位监控、用车申请审批、车辆调度管理、电子围栏报警等功能。

## 技术栈

### 后端
- Spring Boot 3.x
- Spring Security（安全认证）
- MyBatis-Plus（ORM框架）
- MySQL 8.0（数据库）
- Redis 7.0（缓存）
- WebSocket（实时通信）
- JWT（令牌认证）
- Swagger（API文档）

### 前端
- Vue 3
- Element Plus
- Axios
- Vue Router
- Pinia
- 高德地图/百度地图 API

## 核心功能

### 1. 用户权限管理
- 用户登录/登出
- 角色权限管理
- 部门管理
- 菜单权限控制

### 2. 车辆管理
- 车辆档案管理
- 驾驶员管理
- 车辆保险信息
- 维护保养记录

### 3. 用车申请与审批
- 用车申请单创建
- 多级审批流程
- 申请单查询统计

### 4. 车辆调度
- 车辆派遣
- 驾驶员分配
- 调度计划管理
- 冲突检测

### 5. 实时定位监控（核心功能）
- 实时位置展示
- 历史轨迹回放
- 电子围栏报警
- 停车点统计
- 里程统计
- 异常驾驶行为报警

### 6. 统计分析
- 车辆使用率统计
- 里程统计报表
- 油耗统计分析
- 驾驶员考核报表

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 7.0+
- Node.js 16+

### 数据库初始化

1. 创建数据库
```bash
mysql -u root -p
```

2. 执行初始化脚本
```bash
mysql -u root -p < database/init.sql
```

默认管理员账号：
- 用户名：admin
- 密码：admin123

### 后端启动

1. 修改配置文件 `src/main/resources/application.yml`
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/vehicle_gps
    username: your_username
    password: your_password
  redis:
    host: localhost
    port: 6379
```

2. 启动后端服务
```bash
mvn clean install
mvn spring-boot:run
```

3. 访问 Swagger 文档
```
http://localhost:8080/swagger-ui.html
```

### 前端启动

1. 进入前端目录
```bash
cd vehicle-gps-frontend
```

2. 安装依赖
```bash
npm install
```

3. 启动开发服务器
```bash
npm run dev
```

4. 访问前端页面
```
http://localhost:5173
```

## 项目结构

```
car/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/car/
│   │   │       ├── common/           # 公共类
│   │   │       │   ├── exception/    # 异常处理
│   │   │       │   ├── result/       # 统一返回结果
│   │   │       │   └── util/         # 工具类
│   │   │       ├── config/           # 配置类
│   │   │       ├── controller/       # 控制器
│   │   │       ├── service/          # 服务层
│   │   │       ├── mapper/           # Mapper接口
│   │   │       ├── entity/           # 实体类
│   │   │       └── CarApplication.java
│   │   └── resources/
│   │       ├── application.yml       # 配置文件
│   │       └── mapper/              # Mapper XML
│   └── test/
├── database/
│   └── init.sql                     # 数据库初始化脚本
├── 开发流程文档.md                   # 详细开发流程
├── pom.xml
└── README.md
```

## API文档

启动项目后，访问 Swagger 文档查看完整的 API 接口：
```
http://localhost:8080/swagger-ui.html
```

## 开发指南

详细的开发流程、技术架构、数据库设计等内容，请参考：
- [开发流程文档.md](./开发流程文档.md)

## 部署说明

### Docker 部署（推荐）

1. 构建镜像
```bash
docker build -t vehicle-gps:latest .
```

2. 启动容器
```bash
docker-compose up -d
```

### 传统部署

1. 后端打包
```bash
mvn clean package -DskipTests
```

2. 运行 JAR 包
```bash
java -jar target/car-0.0.1-SNAPSHOT.jar
```

## 注意事项

1. **数据库密码**：生产环境请修改默认密码
2. **JWT密钥**：请修改 `application.yml` 中的 JWT 密钥
3. **地图API**：需要申请高德地图或百度地图的开发者密钥
4. **北斗终端对接**：根据实际硬件厂商提供的协议进行对接

## 性能优化建议

1. Redis 缓存实时位置数据，减少数据库压力
2. GPS轨迹数据按日期分表存储
3. 历史数据定期归档
4. 使用连接池优化数据库连接
5. 前端地图标记聚合显示

## 安全建议

1. 使用 HTTPS 加密传输
2. 定期更新依赖版本
3. 实施IP白名单限制
4. 敏感数据加密存储
5. 定期备份数据库

## 常见问题

### 1. 数据库连接失败
检查 MySQL 是否启动，配置文件中的数据库地址、用户名、密码是否正确。

### 2. Redis 连接失败
检查 Redis 是否启动，配置文件中的 Redis 地址是否正确。

### 3. JWT Token 过期
默认有效期为 24 小时，可在配置文件中修改。

### 4. WebSocket 连接失败
检查防火墙设置，确保 WebSocket 端口未被屏蔽。

## 贡献指南

欢迎提交 Issue 和 Pull Request！

## 许可证

MIT License

## 联系方式

如有问题，请提交 Issue 或发送邮件。

---

**祝您使用愉快！** 🚀
