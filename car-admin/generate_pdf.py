# -*- coding: utf-8 -*-
"""
生成 car-admin 前端核心难点 & 解决方法 PDF
"""
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.units import cm
from reportlab.lib.colors import HexColor
from reportlab.lib.enums import TA_LEFT, TA_JUSTIFY
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, PageBreak,
    Table, TableStyle, KeepTogether
)
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont

# 注册中文字体
pdfmetrics.registerFont(TTFont('YaHei',     'C:/Windows/Fonts/msyh.ttc'))
pdfmetrics.registerFont(TTFont('YaHei-Bold','C:/Windows/Fonts/msyhbd.ttc'))

# 颜色
COLOR_PRIMARY  = HexColor('#0369a1')
COLOR_ACCENT   = HexColor('#0ea5e9')
COLOR_DARK     = HexColor('#1e293b')
COLOR_GRAY     = HexColor('#64748b')
COLOR_LIGHT_BG = HexColor('#e0f2fe')
COLOR_CODE_BG  = HexColor('#f1f5f9')
COLOR_DANGER   = HexColor('#dc2626')

# 样式
styles = {
    'cover_title': ParagraphStyle(
        'cover_title', fontName='YaHei-Bold', fontSize=26,
        textColor=COLOR_PRIMARY, alignment=1, leading=36, spaceAfter=12
    ),
    'cover_sub': ParagraphStyle(
        'cover_sub', fontName='YaHei', fontSize=14,
        textColor=COLOR_GRAY, alignment=1, leading=22, spaceAfter=6
    ),
    'cover_meta': ParagraphStyle(
        'cover_meta', fontName='YaHei', fontSize=11,
        textColor=COLOR_GRAY, alignment=1, leading=18
    ),
    'h1': ParagraphStyle(
        'h1', fontName='YaHei-Bold', fontSize=18,
        textColor=COLOR_PRIMARY, leading=26, spaceBefore=10, spaceAfter=10
    ),
    'h2': ParagraphStyle(
        'h2', fontName='YaHei-Bold', fontSize=14,
        textColor=COLOR_DARK, leading=22, spaceBefore=14, spaceAfter=6,
        borderPadding=4, leftIndent=0
    ),
    'h3': ParagraphStyle(
        'h3', fontName='YaHei-Bold', fontSize=12,
        textColor=COLOR_ACCENT, leading=20, spaceBefore=8, spaceAfter=4
    ),
    'body': ParagraphStyle(
        'body', fontName='YaHei', fontSize=10.5,
        textColor=COLOR_DARK, leading=18, alignment=TA_JUSTIFY,
        spaceAfter=4, firstLineIndent=0
    ),
    'bullet': ParagraphStyle(
        'bullet', fontName='YaHei', fontSize=10.5,
        textColor=COLOR_DARK, leading=18, leftIndent=14,
        bulletIndent=2, spaceAfter=3
    ),
    'code': ParagraphStyle(
        'code', fontName='Courier', fontSize=9,
        textColor=COLOR_DANGER, leading=13, backColor=COLOR_CODE_BG,
        borderPadding=4, leftIndent=6, rightIndent=6, spaceAfter=4
    ),
    'tag': ParagraphStyle(
        'tag', fontName='YaHei-Bold', fontSize=10,
        textColor=COLOR_PRIMARY, leading=18, spaceAfter=2
    ),
}


def p(text, style='body'):
    return Paragraph(text, styles[style])


def bullets(items):
    return [Paragraph(f'• {t}', styles['bullet']) for t in items]


def section_box(title_text):
    """每个难点的标题条 - 用一个深色背景表格实现"""
    t = Table(
        [[Paragraph(f'<font color="white"><b>{title_text}</b></font>', styles['h2'])]],
        colWidths=[16 * cm]
    )
    t.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), COLOR_PRIMARY),
        ('LEFTPADDING', (0, 0), (-1, -1), 12),
        ('RIGHTPADDING', (0, 0), (-1, -1), 12),
        ('TOPPADDING', (0, 0), (-1, -1), 6),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 6),
    ]))
    return t


def info_block(label, content_paragraphs, bg=COLOR_LIGHT_BG):
    """带左侧色条的内容块"""
    inner = [Paragraph(f'<b><font color="#0369a1">{label}</font></b>', styles['tag'])]
    inner += content_paragraphs
    t = Table([[inner]], colWidths=[16 * cm])
    t.setStyle(TableStyle([
        ('BACKGROUND', (0, 0), (-1, -1), bg),
        ('LEFTPADDING', (0, 0), (-1, -1), 12),
        ('RIGHTPADDING', (0, 0), (-1, -1), 12),
        ('TOPPADDING', (0, 0), (-1, -1), 8),
        ('BOTTOMPADDING', (0, 0), (-1, -1), 8),
        ('LINEBEFORE', (0, 0), (0, -1), 3, COLOR_ACCENT),
    ]))
    return t


# ========== 文档内容 ==========
def build_story():
    story = []

    # ── 封面 ──
    story.append(Spacer(1, 4 * cm))
    story.append(p('car-admin 前端项目', 'cover_title'))
    story.append(p('核心技术难点 &amp; 解决方法', 'cover_title'))
    story.append(Spacer(1, 0.6 * cm))
    story.append(p('Smart Fleet Management Platform - Frontend', 'cover_sub'))
    story.append(Spacer(1, 3 * cm))

    tech_table = Table([
        ['技术栈', 'Vue 3.5 + Vite 7 + Pinia 3 + Vue Router'],
        ['UI 框架', 'Element Plus 2.13 + @element-plus/icons-vue'],
        ['图表', 'ECharts 6 + vue-echarts'],
        ['HTTP', 'Axios 1.13(拦截器统一封装)'],
        ['地图', '高德地图 JS API(北斗实时定位)'],
        ['后端通信', 'REST + WebSocket(/ws/location)'],
    ], colWidths=[3 * cm, 11 * cm])
    tech_table.setStyle(TableStyle([
        ('FONT',      (0, 0), (-1, -1), 'YaHei', 10),
        ('FONT',      (0, 0), (0,  -1), 'YaHei-Bold', 10),
        ('TEXTCOLOR', (0, 0), (0,  -1), COLOR_PRIMARY),
        ('TEXTCOLOR', (1, 0), (1,  -1), COLOR_DARK),
        ('BACKGROUND',(0, 0), (0,  -1), COLOR_LIGHT_BG),
        ('GRID',      (0, 0), (-1, -1), 0.5, HexColor('#cbd5e1')),
        ('LEFTPADDING',  (0, 0), (-1, -1), 10),
        ('RIGHTPADDING', (0, 0), (-1, -1), 10),
        ('TOPPADDING',   (0, 0), (-1, -1), 8),
        ('BOTTOMPADDING',(0, 0), (-1, -1), 8),
    ]))
    story.append(tech_table)

    story.append(Spacer(1, 3 * cm))
    story.append(p('文档日期:2026 年 5 月 15 日', 'cover_meta'))
    story.append(PageBreak())

    # ── 目录 ──
    story.append(p('目 录', 'h1'))
    story.append(Spacer(1, 0.3 * cm))
    toc = [
        '难点 1  基于角色的多层权限控制(防篡改)',
        '难点 2  Axios 401 / 403 死循环与并发跳转',
        '难点 3  高德地图异步加载 + 北斗实时定位',
        '难点 4  多图表统计页 + 响应式自适应',
        '难点 5  循环依赖(router ↔ store)',
        '难点 6  Vite 打包体积优化与首屏加载',
        '难点 7  跨域代理与 WebSocket 转发',
        '难点 8  复杂业务流程状态机(用车申请全链路)',
        '难点 9  表格大数据导出与合计行',
        '总结  项目亮点与工程化收益',
    ]
    for i, t in enumerate(toc, 1):
        story.append(p(f'<font color="#0ea5e9"><b>{str(i).zfill(2)}</b></font>&nbsp;&nbsp;{t}', 'body'))
    story.append(PageBreak())

    # ── 难点 1 ──
    story.append(section_box('难点 1  基于角色的多层权限控制(防篡改)'))
    story.append(Spacer(1, 6))
    story.append(info_block('问题描述', [
        p('用户角色信息存储在 localStorage 中,JS 可读可写。恶意用户可直接在浏览器控制台执行 '
          '<font name="Courier" color="#dc2626">localStorage.setItem("userInfo", \'{"role":"ADMIN"}\')</font> '
          '伪造管理员身份,绕过前端菜单和路由限制,访问受限页面。')
    ]))
    story.append(Spacer(1, 6))
    story.append(info_block('解决方案', [
        p('<b>① 双存储防篡改</b>'),
    ] + bullets([
        'localStorage 仅缓存用于 UI 展示的非敏感信息(用户名、头像)',
        'sessionStorage 使用混淆键名 <font name="Courier">_sr</font> 单独保存服务端登录时返回的真实角色',
        '路由守卫和菜单计算优先读 sessionStorage,localStorage 即便被篡改也无法生效',
    ]) + [
        Spacer(1, 4),
        p('<b>② 多层路由 roles 校验</b>'),
        p('通过 <font name="Courier">to.matched.filter(r =&gt; r.meta.roles)</font> 遍历整条嵌套路由链,'
          '任意层级声明 roles 都必须满足,父子路由权限均受保护。'),
        Spacer(1, 4),
        p('<b>③ 菜单按角色渲染(v-if 而非 v-show)</b>'),
        p('MainLayout 用 computed 计算 isAdmin,配合 v-if 直接不渲染受限菜单节点,DOM 中完全不可见,'
          '避免通过开发者工具显示隐藏。'),
        Spacer(1, 4),
        p('<b>④ 服务端兜底权威</b>'),
        p('前端做的所有权限判断都只是 UI 优化,真正的访问控制由后端 401/403 兜底,前端代码注释中明确'
          '「服务端始终是权威来源」。')
    ]))
    story.append(Spacer(1, 6))
    story.append(info_block('关键代码', [
        p('<font name="Courier">// stores/user.js</font>'),
        p('<font name="Courier">sessionStorage.setItem(\'_sr\', data.role || \'USER\')</font>'),
        Spacer(1, 2),
        p('<font name="Courier">// main.js 路由守卫</font>'),
        p('<font name="Courier">const sessionRole = sessionStorage.getItem(\'_sr\')</font>'),
        p('<font name="Courier">const userRole = (sessionRole || useUserStore().role).toUpperCase()</font>'),
    ], bg=COLOR_CODE_BG))

    story.append(PageBreak())

    # ── 难点 2 ──
    story.append(section_box('难点 2  Axios 401 / 403 死循环与并发跳转'))
    story.append(Spacer(1, 6))
    story.append(info_block('问题描述', [
        p('Token 过期场景下,首屏可能同时发出 5~10 个并发请求,全部收到 401 响应。'
          '若不加控制会:①弹出 N 个相同的 ElMessage 错误提示;②触发多次 router.replace,'
          '可能造成 NavigationDuplicated 警告;③登录接口本身返回 401(账号密码错误)被误判为 Token 过期,'
          '强制跳到登录页造成用户体验混乱。')
    ]))
    story.append(Spacer(1, 6))
    story.append(info_block('解决方案', [
        p('<b>① 模块级 isRedirecting 标志位加锁</b>'),
    ] + bullets([
        '第一个 401 触发清除凭据 + 跳转登录页,同时上锁',
        '后续并发 401 检测到锁已置位,直接被静默丢弃',
        '跳转完成后通过 setTimeout 释放锁,允许下一轮请求',
    ]) + [
        Spacer(1, 4),
        p('<b>② 区分登录接口的 401</b>'),
        p('通过 <font name="Courier">err.config?.url?.includes(\'/api/auth/login\')</font> 识别:登录接口的 401 '
          '是用户名/密码错误,仅弹提示;其他接口的 401 才是 Token 过期需跳转。'),
        Spacer(1, 4),
        p('<b>③ 静默 403 自定义开关</b>'),
        p('在请求 config 上加 <font name="Courier">_silent403: true</font>,用于首页统计卡片这类'
          '"普通用户无权但不应跳 403 页"的场景。'),
        Spacer(1, 4),
        p('<b>④ router.replace 替代 push</b>'),
        p('被拦截到 /403 或 /login 后,history 栈中不留下受限页面记录,用户点"返回上一页"'
          '回到的是合法页面而不是再次撞墙。')
    ]))

    story.append(PageBreak())

    # ── 难点 3 ──
    story.append(section_box('难点 3  高德地图异步加载 + 北斗实时定位'))
    story.append(Spacer(1, 6))
    story.append(info_block('问题描述', [
        p('① 高德 JS API 通过 &lt;script&gt; 异步加载,组件 onMounted 时 window.AMap 可能尚未就绪,'
          'new AMap.Map() 会直接报错;② 几十辆车实时上报,每 30s 刷新一次,若全量重建 Marker '
          '会闪烁且性能差;③ 高德 Web 服务的逆地理编码接口有 QPS 限制,前端并发调用会被限流;'
          '④ Web 服务 Key 若暴露在前端会被盗用。')
    ]))
    story.append(Spacer(1, 6))
    story.append(info_block('解决方案', [
        p('<b>① 轮询 + 超时等待 AMap 就绪</b>'),
        p('封装 <font name="Courier">waitAMap()</font> 函数,每 100ms 检查 window.AMap,'
          '10 秒未就绪则切换 mapStatus=error,展示"地图加载失败"卡片与重试按钮。'),
        Spacer(1, 4),
        p('<b>② Marker 增量更新(diff 算法)</b>'),
    ] + bullets([
        '用 markerMap 字典缓存所有 Marker 实例,key 优先用 vehicleId,降级用 terminalNo',
        '新数据到达后:已存在的 key 调 setPosition + setContent 复用实例',
        '消失的 key 才执行 amapInstance.remove + delete,避免抖动闪烁',
    ]) + [
        Spacer(1, 4),
        p('<b>③ 逆地理编码后端代理 + 串行调用</b>'),
    ] + bullets([
        '前端只调 /api/location/beidou/resolve-address,后端用 Web 服务 Key 调高德 REST',
        '前端仅暴露 JS Key(域名白名单),敏感的 Web Key 不下发到浏览器',
        '批量解析用 for…of + await 串行执行,而非 Promise.all,规避 QPS 限制',
    ]) + [
        Spacer(1, 4),
        p('<b>④ 定时器与实例的生命周期管理</b>'),
        p('onMounted 注册 setInterval(loadList, 30000),onUnmounted 中 clearInterval + '
          'amapInstance.destroy() + markerMap = {},杜绝内存泄漏。')
    ]))

    story.append(PageBreak())

    # ── 难点 4 ──
    story.append(section_box('难点 4  多图表统计页 + 响应式自适应'))
    story.append(Spacer(1, 6))
    story.append(info_block('问题描述', [
        p('统计报表页面同时展示 4 个 ECharts(状态饼图、调度折线、车辆排行柱状、费用饼图)'
          '+ 4 张统计卡片 + 1 张明细表,共需 8 个接口数据。若串行请求首屏超过 3s;'
          '若不处理 DOM 时机,echarts.init 会因容器宽高为 0 渲染失败;窗口缩放后图表不会自动适配。')
    ]))
    story.append(Spacer(1, 6))
    story.append(info_block('解决方案', [
        p('<b>① Promise.all 并行 8 个接口</b>'),
        p('将车辆 4 种状态分布、总数、调度列表、待审申请、统计报表打包并发,首屏速度从 ~3s 降到 ~800ms。'),
        Spacer(1, 4),
        p('<b>② await nextTick() 保证 DOM 就绪</b>'),
        p('请求完成 → reactive 数据更新 → nextTick 等待下一帧 DOM patch 完成 → echarts.init,'
          '避免容器为 0 宽高时初始化失败。'),
        Spacer(1, 4),
        p('<b>③ 图表实例复用</b>'),
        p('使用 <font name="Courier">if (!pieChart) pieChart = echarts.init(ref)</font>,'
          '第二次刷新只执行 setOption,避免重复创建 canvas 内存暴涨。'),
        Spacer(1, 4),
        p('<b>④ 统一 resize 监听</b>'),
        p('onMounted 中 window.addEventListener(\'resize\'),回调里分别调用四个图表的 .resize(),'
          '响应式自适应窗口缩放。'),
        Spacer(1, 4),
        p('<b>⑤ 数据兜底</b>'),
        p('折线图无数据时回退到固定示例数据,保证图表不出现空白尴尬。')
    ]))

    story.append(PageBreak())

    # ── 难点 5 ──
    story.append(section_box('难点 5  循环依赖(router ↔ store)'))
    story.append(Spacer(1, 6))
    story.append(info_block('问题描述', [
        p('router/index.js 的 beforeEach 守卫里需要调用 useUserStore() 读取角色;'
          'stores/user.js 的 logout action 又需要 router.push(\'/login\')。'
          '若两个文件互相 import,ES Module 解析会出现一方为 undefined,'
          '运行时报 "Cannot read property of undefined"。')
    ]))
    story.append(Spacer(1, 6))
    story.append(info_block('解决方案', [
        p('<b>① 动态 import 打破循环</b>'),
        p('stores/user.js <font color="#dc2626"><b>不</b></font>静态 import router,'
          '需要跳转时改用:'),
        p('<font name="Courier" color="#0369a1">'
          'const { default: router } = await import(\'@/router\')</font>'),
        p('动态 import 在运行时执行,此时 router 模块已完成初始化,不会触发循环。'),
        Spacer(1, 4),
        p('<b>② request.js 可静态 import router</b>'),
        p('因为 router 模块不反向引用 request,所以是单向依赖,无循环风险。'),
        Spacer(1, 4),
        p('<b>③ main.js 注册顺序约束</b>'),
        p('必须先 <font name="Courier">app.use(pinia)</font> 再注册 router 守卫,'
          '否则 beforeEach 内 useUserStore() 会拿不到 pinia 实例而报错。'
          '代码中以注释明确说明此约束,防止后续误改顺序。')
    ]))

    story.append(PageBreak())

    # ── 难点 6 ──
    story.append(section_box('难点 6  Vite 打包体积优化与首屏加载'))
    story.append(Spacer(1, 6))
    story.append(info_block('问题描述', [
        p('Element Plus(完整引入约 800KB) + ECharts(约 1MB) + 高德 SDK + axios + vue 全家桶,'
          '若全部打包进单个 chunk,体积接近 2MB,首屏白屏时间超 2 秒;'
          '且每次修改业务代码,整个 vendor 都失效缓存,用户需重复下载。')
    ]))
    story.append(Spacer(1, 6))
    story.append(info_block('解决方案', [
        p('<b>① manualChunks 手动分包</b>'),
        p('在 vite.config.js 的 rollupOptions.output.manualChunks 中,按依赖来源划分独立 chunk:'),
    ] + bullets([
        'echarts + zrender → echarts.chunk',
        'element-plus + icons → element-plus.chunk',
        'axios → axios.chunk',
        'vue / vue-router / pinia / vue-echarts → vue-vendor.chunk',
    ]) + [
        p('好处:浏览器并行下载 + 业务代码更新时 vendor chunk 命中强缓存。'),
        Spacer(1, 4),
        p('<b>② 路由懒加载</b>'),
        p('所有视图组件用 <font name="Courier">() =&gt; import(\'@/views/...\')</font> 动态导入,'
          '每个路由独立 chunk,按需加载。'),
        Spacer(1, 4),
        p('<b>③ chunkSizeWarningLimit 合理上调</b>'),
        p('设置为 1200 KB,避免 element-plus 等大库的无意义告警淹没真实警告。')
    ]))

    story.append(PageBreak())

    # ── 难点 7 ──
    story.append(section_box('难点 7  跨域代理与 WebSocket 转发'))
    story.append(Spacer(1, 6))
    story.append(info_block('问题描述', [
        p('开发环境前端运行在 5173 端口,后端在 8080 端口,直接请求会触发 CORS 拦截;'
          '北斗位置实时推送使用 WebSocket(/ws/location/{username}),'
          'WebSocket 不走标准 HTTP 代理,需要单独配置;生产环境又不需要代理。')
    ]))
    story.append(Spacer(1, 6))
    story.append(info_block('解决方案', [
        p('<b>① Vite 内置代理(开发环境)</b>'),
    ] + bullets([
        'REST: /api → http://localhost:8080  (changeOrigin: true)',
        'WebSocket: /ws → ws://localhost:8080  (ws: true)',
        '前端代码无需区分环境,统一以 /api、/ws 开头即可',
    ]) + [
        Spacer(1, 4),
        p('<b>② 生产环境环境变量切换</b>'),
        p('Axios baseURL 从 <font name="Courier">import.meta.env.VITE_API_BASE_URL</font> 读取,'
          '开发环境为空走 Vite 代理,生产环境读 .env.production 中的真实 API 域名,'
          '同一份源码无缝部署。'),
        Spacer(1, 4),
        p('<b>③ 后端白名单一致</b>'),
        p('Spring Security 的 publicPath 白名单包含 /ws/**,代理路径与后端实际端点前缀完全一致,'
          '避免握手 403。')
    ]))

    story.append(PageBreak())

    # ── 难点 8 ──
    story.append(section_box('难点 8  复杂业务流程状态机(用车申请全链路)'))
    story.append(Spacer(1, 6))
    story.append(info_block('问题描述', [
        p('用车业务包含 5 个阶段:申请 → 审批 → 调度 → 出车 → 还车,每个阶段又有多种子状态'
          '(待审批/已通过/已拒绝/已取消、待出车/行驶中/已完成 等)。'
          '每条记录所处阶段决定:①能看到的操作按钮;②表格中状态标签颜色;③整条流程进度可视化;'
          '存在大量条件分支组合,极易出现"非法操作按钮"或"状态显示错乱"。')
    ]))
    story.append(Spacer(1, 6))
    story.append(info_block('解决方案', [
        p('<b>① 顶部 el-steps 全流程可视化</b>'),
        p('在 ApplyList 顶部用 5 步流程图清晰展示完整业务路径,降低用户认知负担。'),
        Spacer(1, 4),
        p('<b>② 表格内 mini-flow 进度列</b>'),
        p('封装 getMiniStepClass(row, i),根据每条记录的 applyStatus 与 dispatchStatus 动态点亮'
          '当前所处阶段,一眼看清每个申请单的进度。'),
        Spacer(1, 4),
        p('<b>③ 按钮条件渲染矩阵</b>'),
        p('每个操作按钮的 v-if 都基于明确的状态组合,例如:'),
    ] + bullets([
        '待出车 + 无驾驶员 → 显示"分配驾驶员"',
        '待出车 + 已有驾驶员 → 显示"出车"',
        '行驶中 → 显示"完成"',
        '待出车 → 额外显示"取消"',
    ]) + [
        p('从源头杜绝出现违反业务约束的按钮。'),
        Spacer(1, 4),
        p('<b>④ 申请人自驾的特殊标记</b>'),
        p('当 driverId 为 null 但 driverName 有值时,渲染"申请人"tag 与正式驾驶员区分,'
          '处理"先有姓名后绑系统驾驶员"的中间态。')
    ]))

    story.append(PageBreak())

    # ── 难点 9 ──
    story.append(section_box('难点 9  表格大数据导出与合计行'))
    story.append(Spacer(1, 6))
    story.append(info_block('问题描述', [
        p('车辆运维明细报表需要支持:①导出 CSV 供财务核算;②表格底部展示费用合计;'
          '③中文在 Excel 中不能乱码;④不希望增加后端导出接口的复杂度。')
    ]))
    story.append(Spacer(1, 6))
    story.append(info_block('解决方案', [
        p('<b>① 纯前端 Blob 导出</b>'),
    ] + bullets([
        '手工拼装 headers + rows 数组,join(\',\') 为 CSV',
        '前缀加 BOM 头 <font name="Courier">\\uFEFF</font>,Excel 打开中文不乱码',
        'new Blob → URL.createObjectURL → a.download → revokeObjectURL 标准流程',
        '零依赖,不引入 xlsx 等大库',
    ]) + [
        Spacer(1, 4),
        p('<b>② el-table 合计行</b>'),
        p('使用 show-summary + summary-method 自定义聚合规则:'),
    ] + bullets([
        '首列固定显示"合计"',
        '数值列(mileage/dispatchCount) reduce 求和',
        '金额列(maintainCost/repairCost/insuranceCost) 求和后 toFixed(2) 加 ¥ 前缀',
        '非数值列返回空字符串',
    ])))

    story.append(PageBreak())

    # ── 总结 ──
    story.append(section_box('总结  项目亮点与工程化收益'))
    story.append(Spacer(1, 6))

    summary_table = Table([
        ['维度', '具体表现'],
        ['安全意识', '角色防篡改双存储;敏感 Key 后端代理;Token 注释中说明 HttpOnly Cookie 迁移路径'],
        ['健壮性',   '401 跳转加锁;地图加载超时重试;批量逆地理串行限速;循环依赖动态 import 化解'],
        ['性能',     'Pinia + 路由懒加载 + manualChunks 分包 + ECharts 实例复用 + Marker 增量更新'],
        ['体验',     'el-steps 流程可视化 + mini-flow 行内进度 + router.replace 不污染历史 + CSV BOM 防乱码'],
        ['可维护',   '集中式 getToken / clearCredentials;Vite 代理统一开发线上;关键约束注释明示'],
    ], colWidths=[3 * cm, 13 * cm])
    summary_table.setStyle(TableStyle([
        ('FONT',         (0, 0), (-1, -1), 'YaHei', 10),
        ('FONT',         (0, 0), (-1, 0),  'YaHei-Bold', 11),
        ('FONT',         (0, 1), (0,  -1), 'YaHei-Bold', 10),
        ('BACKGROUND',   (0, 0), (-1, 0),  COLOR_PRIMARY),
        ('TEXTCOLOR',    (0, 0), (-1, 0),  HexColor('#ffffff')),
        ('BACKGROUND',   (0, 1), (0,  -1), COLOR_LIGHT_BG),
        ('TEXTCOLOR',    (0, 1), (0,  -1), COLOR_PRIMARY),
        ('TEXTCOLOR',    (1, 1), (1,  -1), COLOR_DARK),
        ('GRID',         (0, 0), (-1, -1), 0.5, HexColor('#cbd5e1')),
        ('VALIGN',       (0, 0), (-1, -1), 'MIDDLE'),
        ('LEFTPADDING',  (0, 0), (-1, -1), 10),
        ('RIGHTPADDING', (0, 0), (-1, -1), 10),
        ('TOPPADDING',   (0, 0), (-1, -1), 8),
        ('BOTTOMPADDING',(0, 0), (-1, -1), 8),
    ]))
    story.append(summary_table)

    story.append(Spacer(1, 16))
    story.append(p('—— 文档结束 ——', 'cover_meta'))

    return story


# 页眉页脚
def on_page(canvas, doc):
    canvas.saveState()
    canvas.setFont('YaHei', 9)
    canvas.setFillColor(COLOR_GRAY)
    # 页眉
    canvas.drawString(2 * cm, A4[1] - 1.2 * cm, 'car-admin 前端核心难点 & 解决方法')
    canvas.setStrokeColor(COLOR_LIGHT_BG)
    canvas.line(2 * cm, A4[1] - 1.4 * cm, A4[0] - 2 * cm, A4[1] - 1.4 * cm)
    # 页脚
    canvas.drawCentredString(A4[0] / 2, 1.2 * cm, f'- {doc.page} -')
    canvas.restoreState()


def main():
    output = 'd:/javajdk/dkd/car-admin/car-admin-前端核心难点与解决方法.pdf'
    doc = SimpleDocTemplate(
        output, pagesize=A4,
        leftMargin=2.2 * cm, rightMargin=2.2 * cm,
        topMargin=2 * cm, bottomMargin=2 * cm,
        title='car-admin 前端核心难点与解决方法',
        author='car-admin team'
    )
    doc.build(build_story(), onFirstPage=on_page, onLaterPages=on_page)
    print(f'PDF 已生成: {output}')


if __name__ == '__main__':
    main()
