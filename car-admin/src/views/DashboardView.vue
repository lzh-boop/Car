<template>
  <div class="dash">

    <!-- ── Header ── -->
    <div class="dash-head">
      <div class="dash-head-left">
        <h1 class="dash-title">概览</h1>
        <span class="dash-date">{{ dateStr }}</span>
      </div>
      <div class="sync-pill">
        <span class="sync-dot"></span>
        实时同步
      </div>
    </div>

    <!-- ── KPI ── -->
    <div class="kpi-row">
      <div
        class="kpi-card"
        v-for="(card, i) in stats"
        :key="card.label"
        :style="{ '--i': i, '--accent-rgb': card.rgb }"
      >
        <div class="kpi-top">
          <div class="kpi-icon" :style="{ background: card.bg, color: card.color }">
            <el-icon :size="16"><component :is="card.icon" /></el-icon>
          </div>
          <span class="kpi-badge" :class="card.trendClass">
            <el-icon :size="9"><component :is="card.trendIcon" /></el-icon>
            {{ card.trend }}
          </span>
        </div>
        <div class="kpi-val">{{ card.value }}</div>
        <div class="kpi-label">{{ card.label }}</div>
        <div class="kpi-bar" :style="{ background: card.bar }"></div>
      </div>
    </div>

    <!-- ── Charts ── -->
    <div class="chart-row">
      <div class="chart-card span-wide">
        <div class="card-head">
          <div class="card-head-l">
            <span class="card-title">行程趋势</span>
            <span class="card-chip">近 30 天</span>
          </div>
        </div>
        <div ref="lineRef" class="chart-body"></div>
      </div>

      <div class="chart-card">
        <div class="card-head">
          <span class="card-title">车辆状态</span>
        </div>
        <div ref="pieRef" class="chart-body"></div>
      </div>
    </div>

    <!-- ── Tables ── -->
    <div class="table-row">
      <div class="table-card">
        <div class="card-head">
          <span class="card-title">最新调度</span>
          <a class="card-more" @click="$router.push('/dispatch')">查看全部</a>
        </div>
        <el-table :data="dispatchList" size="small" class="inner-table">
          <el-table-column prop="vehicleNo"      label="车牌"   width="100" />
          <el-table-column prop="driverName"     label="驾驶员" />
          <el-table-column prop="dispatchStatus" label="状态"   width="76">
            <template #default="{ row }">
              <span class="status-tag" :class="dispatchBadge(row.dispatchStatus)">
                {{ dispatchLabel(row.dispatchStatus) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="planStartTime"  label="出发"   width="108" />
        </el-table>
      </div>

      <div class="table-card">
        <div class="card-head">
          <span class="card-title">最新申请</span>
          <a class="card-more" @click="$router.push('/apply')">查看全部</a>
        </div>
        <el-table :data="applyList" size="small" class="inner-table">
          <el-table-column prop="applyUserName" label="申请人" width="76" />
          <el-table-column prop="vehicleNo"     label="车牌"   width="100" />
          <el-table-column prop="purpose"       label="目的"   show-overflow-tooltip />
          <el-table-column prop="applyStatus"   label="状态"   width="76">
            <template #default="{ row }">
              <span class="status-tag" :class="applyBadge(row.applyStatus)">
                {{ applyLabel(row.applyStatus) }}
              </span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { Van, User, Document, Operation, Top, Bottom, Minus } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getVehicleList } from '@/api/vehicle'
import { getDispatchList } from '@/api/dispatch'
import { getApplyList } from '@/api/apply'

const now = new Date()
const dateStr = `${now.getFullYear()} 年 ${now.getMonth()+1} 月 ${now.getDate()} 日`

const stats = ref([
  { label: '车辆总数',  value: 0, icon: Van,       bg: '#2563eb', color: '#ffffff', bar: '#60a5fa', trend: '+2',    trendClass: 'up',   trendIcon: Top },
  { label: '驾驶员',   value: 0, icon: User,      bg: '#059669', color: '#ffffff', bar: '#34d399', trend: '稳定',  trendClass: 'flat', trendIcon: Minus },
  { label: '本月调度', value: 0, icon: Operation, bg: '#d97706', color: '#ffffff', bar: '#fbbf24', trend: '+8',    trendClass: 'up',   trendIcon: Top },
  { label: '待审申请', value: 0, icon: Document,  bg: '#dc2626', color: '#ffffff', bar: '#f87171', trend: '待处理', trendClass: 'warn', trendIcon: Bottom },
])

const dispatchList = ref([])
const applyList    = ref([])
const lineRef = ref()
const pieRef  = ref()

const dispatchLabel = (s) => ['待出车','行驶中','已完成','已取消'][s] ?? '—'
const applyLabel    = (s) => ['待审批','已通过','已拒绝','已取消'][s] ?? '—'
const dispatchBadge = (s) => ['st-warn','st-blue','st-green','st-gray'][s] ?? ''
const applyBadge    = (s) => ['st-warn','st-green','st-red','st-gray'][s] ?? ''

function initLine() {
  const chart = echarts.init(lineRef.value)
  const days  = Array.from({ length: 30 }, (_, i) => `${i+1}`)
  const data  = days.map(() => Math.floor(Math.random() * 16 + 3))
  chart.setOption({
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff',
      borderColor: '#e4e2de',
      borderWidth: 1,
      textStyle: { color: '#0d1526', fontSize: 12, fontFamily: 'DM Sans' },
      formatter: (p) => `第 ${p[0].name} 天<br/><b style="color:#1e3a5f">${p[0].value}</b> 次调度`
    },
    grid: { top: 10, right: 10, bottom: 22, left: 34 },
    xAxis: {
      type: 'category', data: days,
      axisLabel: { interval: 4, color: '#8a96a8', fontSize: 11 },
      axisLine: { lineStyle: { color: '#f0eeea' } },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value', minInterval: 1,
      splitLine: { lineStyle: { color: '#f0eeea', type: 'dashed' } },
      axisLabel: { color: '#8a96a8', fontSize: 11 }
    },
    series: [{
      type: 'line', data, smooth: .45, symbol: 'none',
      lineStyle: { color: '#2563eb', width: 2.5 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0,0,0,1,[
          { offset: 0, color: 'rgba(37,99,235,.18)' },
          { offset: 1, color: 'rgba(37,99,235,.02)' }
        ])
      }
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}

function initPie(pieData) {
  const chart = echarts.init(pieRef.value)
  chart.setOption({
    tooltip: {
      trigger: 'item',
      backgroundColor: '#fff',
      borderColor: '#e4e2de',
      textStyle: { color: '#0d1526', fontSize: 12 }
    },
    legend: {
      bottom: 0, itemWidth: 7, itemHeight: 7,
      textStyle: { color: '#8a96a8', fontSize: 11 },
      itemGap: 12
    },
    color: ['#059669','#2563eb','#d97706','#dc2626'],
    series: [{
      type: 'pie',
      radius: ['44%', '68%'],
      center: ['50%', '42%'],
      label: { show: false },
      itemStyle: { borderRadius: 4, borderWidth: 2, borderColor: '#f8f7f5' },
      data: pieData
    }]
  })
  window.addEventListener('resize', () => chart.resize())
}

onMounted(async () => {
  let pieData = [
    { name: '空闲', value: 0 },
    { name: '在用', value: 0 },
    { name: '维修', value: 0 },
    { name: '报废', value: 0 }
  ]
  // _silent403: true — 普通用户无权访问这些 ADMIN 接口时静默忽略，不跳转 /403
  const s403 = { _silent403: true }

  try {
    const [f,u,m,s,t] = await Promise.all([
      getVehicleList({ status:0, pageNum:1, pageSize:1 }, s403),
      getVehicleList({ status:1, pageNum:1, pageSize:1 }, s403),
      getVehicleList({ status:2, pageNum:1, pageSize:1 }, s403),
      getVehicleList({ status:3, pageNum:1, pageSize:1 }, s403),
      getVehicleList({ pageNum:1, pageSize:1 }, s403)
    ])
    stats.value[0].value = t?.total ?? 0
    pieData = [
      { name:'空闲', value: f?.total ?? 0 },
      { name:'在用', value: u?.total ?? 0 },
      { name:'维修', value: m?.total ?? 0 },
      { name:'报废', value: s?.total ?? 0 }
    ]
  } catch (_) {}

  try {
    const d = await getDispatchList({ pageNum:1, pageSize:5 }, s403)
    stats.value[2].value = d?.total ?? 0
    dispatchList.value   = d?.records ?? []
  } catch (_) {}

  try {
    const p = await getApplyList({ applyStatus:0, pageNum:1, pageSize:1 })
    stats.value[3].value = p?.total ?? 0
    const a = await getApplyList({ pageNum:1, pageSize:5 })
    applyList.value = a?.records ?? []
  } catch (_) {}

  await nextTick()
  initLine()
  initPie(pieData)
})
</script>

<style scoped>
.dash {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* ── Header ── */
.dash-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.dash-head-left { display: flex; align-items: baseline; gap: 12px; }
.dash-title {
  font-size: 20px;
  font-weight: 800;
  color: var(--text-primary);
  letter-spacing: -.03em;
  font-family: 'DM Sans', sans-serif;
  line-height: 1;
}
.dash-date {
  font-size: 12px;
  color: var(--text-tertiary);
  font-weight: 400;
}
.sync-pill {
  display: flex;
  align-items: center;
  gap: 6px;
  background: #059669;
  border-radius: 20px;
  padding: 5px 13px;
  font-size: 11.5px;
  font-weight: 700;
  color: #ffffff;
  box-shadow: 0 2px 8px rgba(5,150,105,.3);
}
.sync-dot {
  width: 6px; height: 6px;
  border-radius: 50%;
  background: #ffffff;
  box-shadow: 0 0 5px rgba(255,255,255,.8);
  animation: pulse 2s ease infinite;
}
@keyframes pulse { 0%,100% { opacity:1; } 50% { opacity:.35; } }

/* ── KPI ── */
.kpi-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
@media (max-width: 1100px) { .kpi-row { grid-template-columns: repeat(2, 1fr); } }

.kpi-card {
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-radius: var(--r-lg);
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  box-shadow: var(--sh-xs);
  animation: slideUp .28s var(--ease-out) both;
  animation-delay: calc(var(--i) * 55ms);
  transition: box-shadow var(--dur) var(--ease), transform var(--dur) var(--ease-spring), border-color var(--dur);
  position: relative;
  overflow: hidden;
}
.kpi-card:hover {
  box-shadow: var(--sh-md);
  transform: translateY(-2px);
  border-color: var(--border-strong);
}
/* Subtle bottom accent bar */
.kpi-bar {
  position: absolute;
  bottom: 0; left: 0; right: 0;
  height: 3px;
  border-radius: 0 0 var(--r-lg) var(--r-lg);
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(10px); }
  to   { opacity: 1; transform: none; }
}

.kpi-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}
.kpi-icon {
  width: 38px; height: 38px;
  border-radius: var(--r-sm);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0,0,0,.15);
}
.kpi-badge {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 10.5px;
  font-weight: 700;
  padding: 2px 7px;
  border-radius: 20px;
}
.kpi-badge.up   { background: #059669; color: #ffffff; }
.kpi-badge.flat { background: #64748b; color: #ffffff; }
.kpi-badge.warn { background: #d97706; color: #ffffff; }

.kpi-val {
  font-size: 30px;
  font-weight: 800;
  color: var(--text-primary);
  line-height: 1;
  letter-spacing: -.04em;
  font-variant-numeric: tabular-nums;
  font-family: 'DM Sans', sans-serif;
}
.kpi-label {
  font-size: 11px;
  color: var(--text-tertiary);
  font-weight: 600;
  letter-spacing: .05em;
  text-transform: uppercase;
}

/* ── Charts ── */
.chart-row {
  display: grid;
  grid-template-columns: 3fr 2fr;
  gap: 12px;
}
@media (max-width: 900px) { .chart-row { grid-template-columns: 1fr; } }

.chart-card {
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-radius: var(--r-lg);
  padding: 18px 20px;
  display: flex;
  flex-direction: column;
  height: 264px;
  box-shadow: var(--sh-xs);
}

/* ── Card head ── */
.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
  flex-shrink: 0;
}
.card-head-l { display: flex; align-items: center; gap: 8px; }
.card-title {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -.01em;
  font-family: 'DM Sans', sans-serif;
}
.card-chip {
  font-size: 10px;
  font-weight: 600;
  color: var(--text-tertiary);
  background: var(--bg-elevated);
  border: 1px solid var(--border);
  border-radius: 20px;
  padding: 2px 8px;
  letter-spacing: .03em;
}
.card-more {
  font-size: 11.5px;
  color: var(--navy-500);
  cursor: pointer;
  font-weight: 600;
  transition: color var(--dur-fast);
  text-decoration: none;
}
.card-more:hover { color: var(--navy-700); }

.chart-body { flex: 1; min-height: 0; }

/* ── Tables ── */
.table-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
@media (max-width: 800px) { .table-row { grid-template-columns: 1fr; } }

.table-card {
  background: var(--bg-surface);
  border: 1px solid var(--border);
  border-radius: var(--r-lg);
  padding: 18px 20px;
  box-shadow: var(--sh-xs);
}

/* Inner table overrides */
.inner-table { border: none !important; }
.inner-table :deep(.el-table__inner-wrapper::before) { display: none !important; }
.inner-table :deep(th.el-table__cell) {
  background: transparent !important;
  font-size: 10px !important;
  letter-spacing: .07em !important;
  padding: 6px 10px !important;
  border-bottom: 1px solid var(--border-light) !important;
}
.inner-table :deep(td.el-table__cell) {
  padding: 9px 10px !important;
  font-size: 12.5px !important;
  border-bottom: 1px solid var(--border-light) !important;
}
.inner-table :deep(tr:last-child td.el-table__cell) { border-bottom: none !important; }
.inner-table :deep(tr:hover > td.el-table__cell) { background: var(--navy-50) !important; }

/* Status tags — 实色背景，白色文字，高对比度 */
.status-tag {
  display: inline-flex;
  align-items: center;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: .02em;
  color: #ffffff;
}
.st-blue  { background: #2563eb; }
.st-green { background: #059669; }
.st-warn  { background: #d97706; }
.st-red   { background: #dc2626; }
.st-gray  { background: #64748b; }
</style>
