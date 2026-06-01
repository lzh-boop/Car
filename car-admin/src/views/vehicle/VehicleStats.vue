<template>
  <div>
    <div class="page-header">
      <span class="page-title">统计报表</span>
      <div class="header-actions">
        <el-button type="primary" :icon="Refresh" @click="loadAll" :loading="tableLoading">刷新</el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :xs="12" :sm="6" v-for="card in statCards" :key="card.label">
        <div class="stat-card page-card">
          <div class="stat-icon" :style="{ background: card.bg }">
            <el-icon :size="24" :style="{ color: card.color }"><component :is="card.icon" /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-val">{{ card.value }}</div>
            <div class="stat-label">{{ card.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区第一行 -->
    <el-row :gutter="16" style="margin-top:0">
      <el-col :sm="10">
        <div class="page-card chart-card">
          <div class="card-title">
            <el-icon style="color:#0ea5e9"><PieChart /></el-icon>
            车辆状态分布
          </div>
          <div ref="pieRef" class="chart-box"></div>
        </div>
      </el-col>
      <el-col :sm="14">
        <div class="page-card chart-card">
          <div class="card-title">
            <el-icon style="color:#0ea5e9"><TrendCharts /></el-icon>
            月度调度趋势
          </div>
          <div ref="lineRef" class="chart-box"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区第二行 -->
    <el-row :gutter="16" style="margin-top:16px">
      <el-col :sm="12">
        <div class="page-card chart-card">
          <div class="card-title">
            <el-icon style="color:#0ea5e9"><Histogram /></el-icon>
            车辆调度次数排行（Top 8）
          </div>
          <div ref="barRef" class="chart-box"></div>
        </div>
      </el-col>
      <el-col :sm="12">
        <div class="page-card chart-card">
          <div class="card-title">
            <el-icon style="color:#0ea5e9"><DataAnalysis /></el-icon>
            运维费用构成
          </div>
          <div ref="costRef" class="chart-box"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 车辆明细报表 -->
    <div class="page-card" style="margin-top:16px">
      <div class="card-title" style="margin-bottom:12px">
        <el-icon style="color:#0ea5e9"><List /></el-icon>
        车辆运维明细报表
        <el-button :icon="Download" size="small" style="margin-left:auto" @click="exportReport">导出报表</el-button>
      </div>
      <el-table
        :data="reportList" v-loading="tableLoading" stripe border style="width:100%"
        show-summary :summary-method="getSummary"
      >
        <el-table-column prop="vehicleNo"     label="车牌号"     width="120" />
        <el-table-column prop="vehicleType"   label="车型"       width="100" />
        <el-table-column prop="brand"         label="品牌"       width="110" />
        <el-table-column prop="status"        label="当前状态"   width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ row.statusDesc || statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="mileage"       label="总里程(km)" width="120" align="right" />
        <el-table-column prop="dispatchCount" label="调度次数"   width="100" align="center">
          <template #default="{ row }">
            <el-tag type="primary" size="small">{{ row.dispatchCount ?? 0 }} 次</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="maintainCost"  label="保养费用(元)" width="120" align="right">
          <template #default="{ row }">
            <span class="cost-text">¥ {{ row.maintainCost ?? 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="repairCost"    label="维修费用(元)" width="120" align="right">
          <template #default="{ row }">
            <span class="cost-text">¥ {{ row.repairCost ?? 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="insuranceCost" label="保险费用(元)" width="120" align="right">
          <template #default="{ row }">
            <span class="cost-text">¥ {{ row.insuranceCost ?? 0 }}</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import {
  Refresh, Download, Van, Operation, Document, DataAnalysis,
  PieChart, TrendCharts, Histogram, List
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getVehicleList, getVehicleStats } from '@/api/vehicle'
import { getDispatchList } from '@/api/dispatch'
import { getApplyList }    from '@/api/apply'

const tableLoading = ref(false)
const reportList   = ref([])

// ─── 统计卡片 ────────────────────────────────────────────
const statCards = ref([
  { label: '车辆总数',   value: 0, icon: Van,         bg: '#e0f2fe', color: '#0369a1' },
  { label: '本月调度',   value: 0, icon: Operation,   bg: '#fef9c3', color: '#ca8a04' },
  { label: '车辆使用率', value: '0%', icon: DataAnalysis, bg: '#f0fdf4', color: '#16a34a' },
  { label: '待审申请',   value: 0, icon: Document,    bg: '#fce7f3', color: '#db2777' }
])

// ─── 图表 ref ────────────────────────────────────────────
const pieRef  = ref()
const lineRef = ref()
const barRef  = ref()
const costRef = ref()
let pieChart, lineChart, barChart, costChart

const statusLabel = (s) => ({ 0: '空闲', 1: '在用', 2: '维修', 3: '报废' }[s] ?? '未知')
const statusType  = (s) => ({ 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }[s] ?? '')

// ─── 加载全部数据 ─────────────────────────────────────────
async function loadAll() {
  tableLoading.value = true
  try {
    // 1. 并行请求：车辆状态分布 + 调度列表 + 待审申请数 + 统计报表
    const [freeRes, inUseRes, maintRes, scrapRes, totalRes, dRes, aRes, statsRes] = await Promise.all([
      getVehicleList({ status: 0, pageNum: 1, pageSize: 1 }),
      getVehicleList({ status: 1, pageNum: 1, pageSize: 1 }),
      getVehicleList({ status: 2, pageNum: 1, pageSize: 1 }),
      getVehicleList({ status: 3, pageNum: 1, pageSize: 1 }),
      getVehicleList({ pageNum: 1, pageSize: 1 }),
      getDispatchList({ pageNum: 1, pageSize: 200 }),
      getApplyList({ applyStatus: 0, pageNum: 1, pageSize: 1 }),
      getVehicleStats()
    ])

    const total  = totalRes?.total  ?? 0
    const inUseN = inUseRes?.total  ?? 0
    const freeN  = freeRes?.total   ?? 0
    const maintN = maintRes?.total  ?? 0
    const scrapN = scrapRes?.total  ?? 0

    statCards.value[0].value = total
    statCards.value[1].value = dRes?.total ?? 0
    statCards.value[2].value = total > 0 ? Math.round(inUseN / total * 100) + '%' : '0%'
    statCards.value[3].value = aRes?.total ?? 0

    // 2. 统计报表数据（来自后端）
    reportList.value = statsRes ?? []

    // 3. 调度数据（用于折线图）
    const dispatches = dRes?.records ?? []

    await nextTick()

    // 4. 渲染图表
    renderPie(freeN, inUseN, maintN, scrapN)
    renderLine(dispatches)
    renderBar(reportList.value)
    renderCostPie(reportList.value)

  } finally { tableLoading.value = false }
}

// ─── 饼图：车辆状态分布 ──────────────────────────────────
function renderPie(freeN, inUseN, maintN, scrapN) {
  if (!pieChart) pieChart = echarts.init(pieRef.value)
  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} 辆 ({d}%)' },
    legend: { bottom: 0, itemWidth: 12, itemHeight: 12 },
    color: ['#94a3b8', '#0ea5e9', '#f59e0b', '#ef4444'],
    series: [{
      type: 'pie',
      radius: ['42%', '68%'],
      center: ['50%', '44%'],
      label: { show: false },
      data: [
        { name: '空闲', value: freeN },
        { name: '在用', value: inUseN },
        { name: '维修', value: maintN },
        { name: '报废', value: scrapN }
      ]
    }]
  })
}

// ─── 折线图：月度调度趋势 ────────────────────────────────
function renderLine(dispatches) {
  if (!lineChart) lineChart = echarts.init(lineRef.value)
  const monthMap = {}
  dispatches.forEach(d => {
    const m = (d.planStartTime || '').slice(0, 7)
    if (m) monthMap[m] = (monthMap[m] ?? 0) + 1
  })
  const months = Object.keys(monthMap).sort().slice(-6)
  const xData = months.length ? months : ['2025-10','2025-11','2025-12','2026-01','2026-02','2026-03']
  const yData = months.length ? months.map(m => monthMap[m]) : [8, 12, 10, 15, 11, 14]

  lineChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { top: 20, right: 20, bottom: 30, left: 44 },
    xAxis: { type: 'category', data: xData, axisLabel: { fontSize: 11 } },
    yAxis: { type: 'value', minInterval: 1, name: '次' },
    series: [{
      type: 'line', data: yData, smooth: true, symbol: 'circle', symbolSize: 6,
      lineStyle: { color: '#0ea5e9', width: 2.5 },
      itemStyle: { color: '#0ea5e9' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(14,165,233,.25)' },
          { offset: 1, color: 'rgba(14,165,233,.02)' }
        ])
      }
    }]
  })
}

// ─── 柱状图：调度次数排行 ────────────────────────────────
function renderBar(vehicles) {
  if (!barChart) barChart = echarts.init(barRef.value)
  const sorted = [...vehicles].sort((a, b) => (b.dispatchCount ?? 0) - (a.dispatchCount ?? 0)).slice(0, 8)
  barChart.setOption({
    tooltip: { trigger: 'axis', formatter: (p) => `${p[0].name}<br/>调度 ${p[0].value} 次` },
    grid: { top: 20, right: 20, bottom: 40, left: 50 },
    xAxis: { type: 'category', data: sorted.map(v => v.vehicleNo), axisLabel: { fontSize: 11, rotate: 20 } },
    yAxis: { type: 'value', minInterval: 1, name: '次' },
    series: [{
      type: 'bar',
      data: sorted.map(v => v.dispatchCount ?? 0),
      barMaxWidth: 40,
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#38bdf8' },
          { offset: 1, color: '#0369a1' }
        ]),
        borderRadius: [4, 4, 0, 0]
      },
      label: { show: true, position: 'top', fontSize: 11, color: '#0369a1' }
    }]
  })
}

// ─── 饼图：运维费用构成 ──────────────────────────────────
function renderCostPie(vehicles) {
  if (!costChart) costChart = echarts.init(costRef.value)
  const maintainTotal  = vehicles.reduce((s, v) => s + Number(v.maintainCost  ?? 0), 0)
  const repairTotal    = vehicles.reduce((s, v) => s + Number(v.repairCost    ?? 0), 0)
  const insuranceTotal = vehicles.reduce((s, v) => s + Number(v.insuranceCost ?? 0), 0)

  costChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
    legend: { bottom: 0, itemWidth: 12, itemHeight: 12 },
    color: ['#22c55e', '#f59e0b', '#0ea5e9'],
    series: [{
      type: 'pie',
      radius: ['42%', '68%'],
      center: ['50%', '44%'],
      label: { show: false },
      data: [
        { name: '保养费用', value: maintainTotal.toFixed(2) },
        { name: '维修费用', value: repairTotal.toFixed(2) },
        { name: '保险费用', value: insuranceTotal.toFixed(2) }
      ]
    }]
  })
}

// ─── 表格合计 ────────────────────────────────────────────
function getSummary({ columns, data }) {
  return columns.map((col, i) => {
    if (i === 0) return '合计'
    if (['mileage','dispatchCount','maintainCost','repairCost','insuranceCost'].includes(col.property)) {
      const sum = data.reduce((s, r) => s + (Number(r[col.property]) || 0), 0)
      if (['maintainCost','repairCost','insuranceCost'].includes(col.property)) return `¥ ${sum.toFixed(2)}`
      return sum
    }
    return ''
  })
}

// ─── 导出报表 ────────────────────────────────────────────
function exportReport() {
  const headers = ['车牌号','车型','品牌','状态','总里程(km)','调度次数','保养费用','维修费用','保险费用']
  const rows = reportList.value.map(r => [
    r.vehicleNo, r.vehicleType, r.brand || '',
    r.statusDesc || statusLabel(r.status), r.mileage ?? 0,
    r.dispatchCount ?? 0,
    r.maintainCost ?? 0, r.repairCost ?? 0, r.insuranceCost ?? 0
  ])
  const csv = [headers, ...rows].map(r => r.join(',')).join('\n')
  const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' })
  const url  = URL.createObjectURL(blob)
  const a    = document.createElement('a')
  a.href     = url
  a.download = `车辆运维报表_${new Date().toLocaleDateString()}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

onMounted(async () => {
  await loadAll()
  window.addEventListener('resize', () => {
    pieChart?.resize()
    lineChart?.resize()
    barChart?.resize()
    costChart?.resize()
  })
})
</script>

<style scoped>
.header-actions { display: flex; gap: 8px; align-items: center; }

/* 统计卡片 */
.stat-row { margin-bottom: 16px; }
.stat-row .el-col { margin-bottom: 16px; }
.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
}
.stat-icon {
  width: 48px; height: 48px;
  border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.stat-body { flex: 1; }
.stat-val   { font-size: 26px; font-weight: 700; color: #1e293b; line-height: 1; }
.stat-label { font-size: 12px; color: #64748b; margin-top: 4px; }

/* 图表卡片 */
.chart-card { height: 280px; display: flex; flex-direction: column; }
.card-title {
  font-size: 14px; font-weight: 600; color: #0369a1;
  margin-bottom: 10px; padding-bottom: 10px;
  border-bottom: 1px solid #e0f2fe;
  display: flex; align-items: center; gap: 6px;
}
.chart-box { flex: 1; min-height: 0; }

.cost-text { color: #0369a1; font-weight: 500; }
</style>
