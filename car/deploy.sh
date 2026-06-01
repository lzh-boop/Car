#!/bin/bash
# ====================================================
# 车辆管理系统 — 阿里云 ECS 一键部署脚本
# 服务器 IP : 47.103.7.135
# 系统      : AlibabaCloud Linux (兼容 CentOS/Rocky)
# 使用方式  : bash deploy.sh
# ====================================================

set -e

echo "=================================================="
echo "  车辆管理系统 一键部署脚本"
echo "  服务器: 47.103.7.135"
echo "=================================================="

# ─── 1. 安装基础依赖 ───────────────────────────────
echo "[1/7] 安装 Java 17、MySQL、Redis、Nginx..."
yum install -y java-17-openjdk-headless nginx || \
  apt-get install -y openjdk-17-jre-headless nginx 2>/dev/null || true

# ─── 2. 安装 MySQL 8（如果未安装）─────────────────
if ! command -v mysql &>/dev/null; then
  echo "  安装 MySQL 8..."
  # Alibaba Cloud Linux / CentOS 方式
  yum install -y mysql-server 2>/dev/null || \
    apt-get install -y mysql-server 2>/dev/null || true
  systemctl enable mysqld --now || systemctl enable mysql --now || true
fi

# ─── 3. 安装 Redis ─────────────────────────────────
if ! command -v redis-server &>/dev/null; then
  echo "  安装 Redis..."
  yum install -y redis 2>/dev/null || \
    apt-get install -y redis-server 2>/dev/null || true
  systemctl enable redis --now || true
fi

# ─── 4. 初始化数据库 ───────────────────────────────
echo "[2/7] 初始化数据库..."
DB_NAME="vehicle_gps"
DB_USER="root"
DB_PASS="${DB_PASSWORD:-123456}"

mysql -u$DB_USER -p$DB_PASS -e "
  CREATE DATABASE IF NOT EXISTS $DB_NAME
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
" 2>/dev/null && echo "  数据库 $DB_NAME 已就绪" || \
  echo "  ⚠️  数据库连接失败，请手动检查 MySQL 密码"

# ─── 5. 部署 Spring Boot 后端 ─────────────────────
echo "[3/7] 部署后端 Jar..."
mkdir -p /opt/car /var/log/car

JAR_FILE="car-*.jar"
if ls $JAR_FILE 1>/dev/null 2>&1; then
  cp $JAR_FILE /opt/car/app.jar
  echo "  Jar 已复制到 /opt/car/app.jar"
else
  echo "  ⚠️  未找到 Jar 包，请先在本地执行 mvn package -DskipTests，再将 target/*.jar 上传至当前目录"
fi

# 生成 systemd 服务（开机自启 + 自动重启）
cat > /etc/systemd/system/car.service << 'EOF'
[Unit]
Description=Car Management System (Spring Boot)
After=network.target mysqld.service redis.service

[Service]
Type=simple
User=root
WorkingDirectory=/opt/car
ExecStart=/usr/bin/java \
  -Xmx512m \
  -jar /opt/car/app.jar \
  --spring.profiles.active=prod
Restart=always
RestartSec=10
StandardOutput=append:/var/log/car/app.log
StandardError=append:/var/log/car/error.log

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
systemctl enable car --now && echo "  后端服务已启动" || echo "  后端服务启动失败，请检查 Jar 是否存在"

# ─── 6. 部署前端静态文件 ──────────────────────────
echo "[4/7] 部署前端静态文件..."
mkdir -p /usr/share/nginx/html/car

if [ -d "dist" ]; then
  cp -r dist/* /usr/share/nginx/html/car/
  echo "  前端文件已部署到 /usr/share/nginx/html/car/"
else
  echo "  ⚠️  未找到 dist 目录，请先在本地执行 npm run build，再将 dist 目录上传至当前目录"
fi

# ─── 7. 配置 Nginx ────────────────────────────────
echo "[5/7] 配置 Nginx..."
cp /opt/car/nginx-car.conf /etc/nginx/conf.d/car.conf 2>/dev/null || \
cat > /etc/nginx/conf.d/car.conf << 'NGINX_EOF'
server {
    listen       80;
    server_name  47.103.7.135;

    # 前端静态文件（Vue SPA）
    root   /usr/share/nginx/html/car;
    index  index.html;

    # Vue Router history 模式：所有非 /api 路径交给 index.html
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 反代后端 API
    location /api/ {
        proxy_pass         http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header   Host             $host;
        proxy_set_header   X-Real-IP        $remote_addr;
        proxy_set_header   X-Forwarded-For  $proxy_add_x_forwarded_for;
        proxy_connect_timeout 30s;
        proxy_read_timeout    60s;
    }

    # WebSocket 反代
    location /ws/ {
        proxy_pass         http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header   Upgrade    $http_upgrade;
        proxy_set_header   Connection "upgrade";
        proxy_set_header   Host       $host;
    }
}
NGINX_EOF

nginx -t && systemctl enable nginx --now && systemctl reload nginx && \
  echo "  Nginx 已启动" || echo "  Nginx 配置有误，请检查"

# ─── 8. 防火墙放行端口 ────────────────────────────
echo "[6/7] 放行端口 80 / 8080..."
firewall-cmd --permanent --add-port=80/tcp   2>/dev/null || true
firewall-cmd --permanent --add-port=8080/tcp 2>/dev/null || true
firewall-cmd --reload 2>/dev/null || true

echo ""
echo "=================================================="
echo "  部署完成！"
echo "  管理系统地址 : http://47.103.7.135"
echo "  设备上报地址 : http://47.103.7.135/api/location/beidou/device/report"
echo "  后端日志     : tail -f /var/log/car/app.log"
echo "=================================================="
