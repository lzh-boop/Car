# Car 车辆管理系统（全栈）

一个基于 **Spring Boot 3.2 + Vue 3 + Vite** 的车辆管理系统，包含车辆申请、调度、归还、维护、统计、定位等模块。

## 项目结构

```
.
├── car/          后端：Spring Boot 3.2 + MyBatis-Plus + MySQL + Redis + JWT + Spring Security
└── car-admin/    前端：Vue 3 + Vite + Element Plus + Pinia + ECharts + Vue Router
```

## 技术栈

### 后端 `car/`

- Java 17
- Spring Boot 3.2.1
- Spring Security + JWT
- MyBatis-Plus 3.5.6
- MySQL 8 + HikariCP
- Redis (Lettuce)
- Hutool / FastJSON2
- SpringDoc OpenAPI (Swagger)
- WebSocket

### 前端 `car-admin/`

- Vue 3.5
- Vite 7
- Element Plus 2.13
- Pinia 3 状态管理
- Vue Router
- ECharts 6
- Axios

## 快速开始

### 1. 准备本地敏感配置（仅首次需要）

为了保护敏感信息，仓库内的 `application.yml` 仅提供占位符（如 `CHANGE_ME_DB_PASSWORD`）。
在启动后端前，请按以下任一方式注入您本地的真实配置：

**方式 A：复制模板创建本地配置（推荐）**

```bash
cd car/src/main/resources
cp application-local.yml.example application-local.yml
# 然后编辑 application-local.yml 填入真实的密码和 Key
```

启动时加上 profile：

```bash
# Windows
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
# macOS / Linux
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

> `application-local.yml` 已在 `.gitignore` 中，**永远不会被提交到仓库**。

**方式 B：用环境变量注入**

```bash
# Windows PowerShell
$env:DB_PASSWORD="你的MySQL密码"
$env:REDIS_HOST="localhost"
$env:REDIS_PASSWORD="你的Redis密码"
$env:JWT_SECRET="你的JWT密钥"
$env:AMAP_KEY="你的高德Key"
```

### 2. 准备数据库

```bash
# MySQL 中执行 car/database/ 下的初始化脚本
mysql -u root -p < car/database/init.sql
```

### 3. 启动后端

```bash
cd car
./mvnw spring-boot:run        # macOS / Linux
.\mvnw.cmd spring-boot:run    # Windows
```

后端默认监听 `http://localhost:8080`。

### 启动前端

```bash
cd car-admin
npm install
npm run dev
```

前端默认监听 `http://localhost:5173`，开发态通过 Vite proxy 把 `/api` 转发到 `localhost:8080`。

## 默认账号

- 用户名：`admin`
- 密码：`123456`

## 部署

参考 `car/deploy.sh` 和 `car/nginx-car.conf`。

## License

仅用于学习交流。
