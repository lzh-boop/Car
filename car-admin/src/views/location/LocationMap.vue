<template>
  <div>
    <div class="page-header">
      <span class="page-title">北斗实时定位</span>
      <div style="display:flex;gap:8px;align-items:center">
        <el-tag type="success" effect="dark">
          <el-icon style="margin-right:4px"><Position /></el-icon>北斗卫星系统
        </el-tag>
        <el-tag type="warning" size="small">高德地图北斗版</el-tag>
        <el-button :icon="Refresh" @click="onRefresh" :loading="listLoading">刷新</el-button>
      </div>
    </div>

    <el-row :gutter="16">
      <!-- 在线车辆列表 -->
      <el-col :sm="7">
        <div class="page-card vehicle-list">
          <div class="list-header">
            <span>在线车辆</span>
            <el-badge :value="list.length" type="success" />
          </div>
          <div
            v-for="item in list"
            :key="item.vehicleId ?? item.terminalNo"
            class="vehicle-item"
            :class="{ active: (selected?.vehicleId ?? selected?.terminalNo) === (item.vehicleId ?? item.terminalNo) }"
            @click="selectVehicle(item)"
          >
            <div class="v-avatar">
              <el-icon :size="18"><Van /></el-icon>
            </div>
            <div class="v-info">
              <div class="v-no">{{ item.vehicleNo }}</div>
              <div class="v-meta">
                <span>{{ item.speed }} km/h</span>
                <span class="dot">·</span>
                <span>{{ item.satelliteCount ?? '--' }} 颗卫星</span>
              </div>
              <div class="v-addr">
                <el-icon v-if="item.addressLoading" class="is-loading" :size="10"><Loading /></el-icon>
                {{ item.address || `${item.latitude}, ${item.longitude}` }}
              </div>
            </div>
            <div class="v-signal">
              <div class="signal-bars">
                <span v-for="n in 4" :key="n" :class="{ active: signalLevel(item.signalStrength) >= n }"></span>
              </div>
              <span class="signal-text">{{ signalLabel(item.signalStrength) }}</span>
            </div>
          </div>
          <el-empty v-if="!list.length && !listLoading" description="暂无在线车辆" :image-size="80" />
          <div v-if="listLoading" class="loading-tip">
            <el-icon class="is-loading"><Loading /></el-icon> 正在获取北斗信号...
          </div>
        </div>
      </el-col>

      <!-- 高德地图区域 -->
      <el-col :sm="17">
        <div class="page-card map-wrap">
          <!-- 地图头部工具栏 -->
          <div class="map-toolbar">
            <span class="map-title">
              <el-icon><Location /></el-icon> 实时位置态势图
            </span>
            <div style="display:flex;gap:8px;align-items:center">
              <el-tag size="small" type="success">北斗-3</el-tag>
              <el-tag size="small" type="info">BDS</el-tag>
              <el-tag size="small" type="warning">高德地图</el-tag>
              <!-- 地图类型切换 -->
              <el-button-group size="small">
                <el-button :type="mapType==='normal'?'primary':''" @click="switchMapType('normal')">标准</el-button>
                <el-button :type="mapType==='satellite'?'primary':''" @click="switchMapType('satellite')">卫星</el-button>
              </el-button-group>
            </div>
          </div>

          <!-- 高德地图容器 -->
          <div class="map-container">
            <!-- 地图未加载时的占位 -->
            <div v-if="mapStatus === 'loading'" class="map-status-overlay">
              <el-icon class="is-loading" :size="36" style="color:#0ea5e9"><Loading /></el-icon>
              <p style="margin-top:12px;color:#0ea5e9;font-weight:600">正在加载高德地图...</p>
            </div>
            <div v-if="mapStatus === 'error'" class="map-status-overlay map-error">
              <el-icon :size="48" style="color:#f59e0b"><Warning /></el-icon>
              <p style="margin-top:12px;color:#92400e;font-weight:600">高德地图加载失败</p>
              <p style="color:#b45309;font-size:13px;margin-top:4px">请检查 API Key 是否正确配置</p>
              <el-button size="small" type="primary" style="margin-top:12px" @click="initMap">重试</el-button>
            </div>
            <!-- 真实地图 DOM 挂载节点 -->
            <div id="amap-container" ref="mapContainerRef"></div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 北斗定位记录查询 -->
    <div class="page-card" style="margin-top:16px">
      <div class="card-header">
        <span class="card-title">北斗定位记录</span>
        <div style="display:flex;gap:8px;align-items:center">
          <el-tag size="small" type="success" effect="dark">
            <el-icon style="margin-right:3px"><Loading class="is-loading" /></el-icon>实时更新
          </el-tag>
          <el-tag size="small">数据来源：北斗卫星导航系统（BDS）× 高德逆地理编码</el-tag>
        </div>
      </div>
      <div class="search-bar" style="box-shadow:none;padding:0;margin-bottom:12px">
        <el-input v-model="recordQuery.vehicleNo" placeholder="车牌号" clearable style="width:140px" />
        <el-input v-model="recordQuery.terminalNo" placeholder="北斗终端号" clearable style="width:160px" />
        <el-button type="primary" :icon="Search" @click="loadRecords">查询</el-button>
        <el-button :icon="Refresh" @click="resetRecordQuery">重置</el-button>
      </div>
      <el-table :data="records" v-loading="recordLoading" stripe size="small" style="width:100%">
        <el-table-column prop="terminalNo"     label="北斗终端号"  width="140" />
        <el-table-column prop="vehicleNo"      label="车牌号"     width="110" />
        <el-table-column prop="latitude"       label="纬度"       width="120" />
        <el-table-column prop="longitude"      label="经度"       width="120" />
        <el-table-column prop="speed"          label="速度(km/h)" width="100" align="right" />
        <el-table-column prop="satelliteCount" label="卫星数"     width="80"  align="center" />
        <el-table-column prop="signalStrength" label="信号强度"   width="110" align="center">
          <template #default="{ row }">
            <el-progress :percentage="row.signalStrength ?? 0" :stroke-width="5" style="width:80px" />
          </template>
        </el-table-column>
        <el-table-column prop="address" label="详细地址" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.address" style="color:#1e293b">{{ row.address }}</span>
            <span v-else style="color:#94a3b8;display:inline-flex;align-items:center;gap:4px">
              <el-icon class="is-loading" :size="12"><Loading /></el-icon>
              解析中...
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="locationTime"   label="定位时间"   width="160" />
      </el-table>
      <el-pagination
        class="pagination"
        background
        layout="total, prev, pager, next"
        :total="recordTotal"
        v-model:current-page="recordQuery.pageNum"
        v-model:page-size="recordQuery.pageSize"
        @change="loadRecords"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { Refresh, Location, Van, Search, Position, Loading, Warning } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getBeidouList, resolveAddress as resolveAddressApi } from '@/api/location'

// ===================== 状态 =====================
const list         = ref([])
const listLoading  = ref(false)
const selected     = ref(null)

const records      = ref([])
const recordTotal  = ref(0)
const recordLoading = ref(false)
const recordQuery  = reactive({ vehicleNo: '', terminalNo: '', pageNum: 1, pageSize: 20 })

const mapContainerRef = ref(null)
const mapStatus  = ref('loading')  // 'loading' | 'ready' | 'error'
const mapType    = ref('normal')   // 'normal' | 'satellite'

let amapInstance    = null   // AMap.Map 实例
let markerMap       = {}     // vehicleId -> AMap.Marker
let refreshTimer    = null
let recordRefreshTimer = null

// ===================== 工具函数 =====================
function signalLevel(strength) {
  if (!strength) return 2
  if (strength >= 80) return 4
  if (strength >= 60) return 3
  if (strength >= 40) return 2
  return 1
}
function signalLabel(strength) {
  const lv = signalLevel(strength)
  return ['弱', '一般', '良好', '强'][lv - 1] ?? '良好'
}

// ===================== 高德地图初始化 =====================
function waitAMap(timeout = 10000) {
  return new Promise((resolve, reject) => {
    if (window.AMap) { resolve(window.AMap); return }
    const start = Date.now()
    const timer = setInterval(() => {
      if (window.AMap) { clearInterval(timer); resolve(window.AMap) }
      else if (Date.now() - start > timeout) { clearInterval(timer); reject(new Error('AMap 加载超时')) }
    }, 100)
  })
}

async function initMap() {
  mapStatus.value = 'loading'
  try {
    const AMap = await waitAMap()
    await nextTick()

    amapInstance = new AMap.Map('amap-container', {
      zoom: 5,
      center: [104.0, 35.5],   // 中国地理中心
      mapStyle: 'amap://styles/fresh',
      features: ['bg', 'road', 'building', 'point'],
    })

    // 比例尺 + 工具条
    amapInstance.addControl(new AMap.Scale())
    amapInstance.addControl(new AMap.ToolBar({ position: { bottom: '80px', right: '10px' } }))

    mapStatus.value = 'ready'
  } catch (e) {
    console.error('高德地图初始化失败:', e)
    mapStatus.value = 'error'
  }
}

// ===================== 逆地理编码：调用后端接口（后端再调高德Web服务API） =====================
async function reverseGeocode(lng, lat, id = null) {
  try {
    // 调用后端 /api/location/beidou/resolve-address
    // 后端用 Web服务Key 调用高德 REST API，返回省市区街道完整地址
    const address = await resolveAddressApi(lng, lat, id)
    return address || null
  } catch {
    return null
  }
}

// ===================== 批量逆地理编码（串行，避免后端高德限速） =====================
async function batchReverseGeocode(vehicles) {
  for (const item of vehicles) {
    if (item.address && !item.address.includes('附近') && !item.address.includes('定位中')) continue
    item.addressLoading = true
    const addr = await reverseGeocode(item.longitude, item.latitude, item.locationId ?? null)
    item.address        = addr || `${item.latitude}, ${item.longitude}`
    item.addressLoading = false
    const key = String(item.vehicleId ?? item.terminalNo)
    const marker = markerMap[key]
    if (marker) marker.setContent(buildMarkerContent(item))
  }
}

// ===================== 加载在线车辆（仅使用真实北斗上报数据） =====================
async function loadList() {
  listLoading.value = true
  try {
    // 拉取最近 200 条北斗上报记录（已按 locationTime DESC 排序）
    const locRes = await getBeidouList({ pageNum: 1, pageSize: 200 })
    // 按「终端号」去重，取每个终端最新一条定位
    // key 优先用 terminalNo，没有时退用 vehicleNo，确保不会全部合并到空 key
    const locMap = {}
    if (locRes?.records?.length) {
      locRes.records.forEach(r => {
        const key = r.terminalNo || r.vehicleNo
        if (key && !locMap[key]) locMap[key] = r
      })
    }

    // 只展示经纬度有效的记录
    list.value = Object.values(locMap)
      .filter(loc => loc.latitude != null && loc.longitude != null
                  && String(loc.latitude).trim() !== '' && String(loc.longitude).trim() !== '')
      .map(loc => ({
        vehicleId:      loc.vehicleId,
        // 车牌号优先，未关联车辆时降级显示终端号
        vehicleNo:      loc.vehicleNo || loc.terminalNo || '未知终端',
        terminalNo:     loc.terminalNo,
        latitude:       parseFloat(loc.latitude),
        longitude:      parseFloat(loc.longitude),
        speed:          loc.speed          ?? 0,
        direction:      loc.direction      ?? 0,
        altitude:       loc.altitude       ?? 0,
        satelliteCount: loc.satelliteCount ?? 0,
        signalStrength: loc.signalStrength ?? 0,
        locationTime:   loc.locationTime,
        address:        loc.address        ?? '',
        locationId:     loc.id,
        addressLoading: false,
      }))

    // 同步更新已选中的数据
    if (selected.value) {
      const selKey = selected.value.vehicleId ?? selected.value.terminalNo
      const fresh = list.value.find(v => (v.vehicleId ?? v.terminalNo) === selKey)
      if (fresh) selected.value = { ...fresh }
    }

    // 更新地图标记
    if (mapStatus.value === 'ready') {
      updateMapMarkers()
    }

    // 批量逆地理编码获取详细地址
    if (mapStatus.value === 'ready') {
      batchReverseGeocode(list.value)
    }
  } finally {
    listLoading.value = false
  }
}

// ===================== 地图标记管理 =====================
function buildMarkerContent(item) {
  const color = '#0ea5e9'
  const addr  = item.address || `${item.latitude}, ${item.longitude}`
  return `
    <div class="bds-marker">
      <div class="bds-dot" style="background:${color}">
        <svg width="12" height="12" viewBox="0 0 24 24" fill="white">
          <path d="M5 17H3a2 2 0 01-2-2V5a2 2 0 012-2h11a2 2 0 012 2v3m-5 12H9l-1-4H5l-1 4H1m14-8h6l3 3v3h-9v-6z"/>
        </svg>
      </div>
      <div class="bds-pulse" style="border-color:${color}"></div>
      <div class="bds-label">
        <span class="bds-vno">${item.vehicleNo}</span>
        <span class="bds-addr">${addr}</span>
      </div>
    </div>
  `
}


function updateMapMarkers() {
  if (!amapInstance || !window.AMap) return
  const AMap = window.AMap

  // 唯一 key：vehicleId 优先，无则用 terminalNo（设备未绑车辆时）
  const getKey = (item) => item.vehicleId ?? item.terminalNo

  // 清理已不存在终端的标记
  const currentKeys = new Set(list.value.map(getKey))
  Object.keys(markerMap).forEach(k => {
    const numKey = isNaN(k) ? k : Number(k)
    if (!currentKeys.has(numKey) && !currentKeys.has(k)) {
      amapInstance.remove(markerMap[k])
      delete markerMap[k]
    }
  })

  list.value.forEach(item => {
    const pos = new AMap.LngLat(item.longitude, item.latitude)
    const key = String(getKey(item))

    if (markerMap[key]) {
      const marker = markerMap[key]
      marker.setPosition(pos)
      marker.setContent(buildMarkerContent(item))
    } else {
      const marker = new AMap.Marker({
        position: pos,
        content:  buildMarkerContent(item),
        offset:   new AMap.Pixel(-14, -14),
        title:    `${item.vehicleNo}｜${item.address || '定位中'}`,
        zIndex:   100,
      })
      amapInstance.add(marker)
      markerMap[key] = marker
    }
  })

  // 若有车辆，自适应视野
  if (list.value.length > 0) {
    const positions = list.value.map(v => [v.longitude, v.latitude])
    if (list.value.length === 1) {
      amapInstance.setCenter(positions[0])
      amapInstance.setZoom(12)
    } else {
      amapInstance.setFitView(Object.values(markerMap))
    }
  }
}

function selectVehicle(item) {
  selected.value = { ...item }
  const key = String(item.vehicleId ?? item.terminalNo)
  const marker = markerMap[key]
  if (marker) {
    amapInstance?.setCenter(marker.getPosition())
    amapInstance?.setZoom(13)
  }
}

// ===================== 地图类型切换 =====================
function switchMapType(type) {
  mapType.value = type
  if (!amapInstance || !window.AMap) return
  if (type === 'satellite') {
    amapInstance.setMapStyle('amap://styles/satellite')
  } else {
    amapInstance.setMapStyle('amap://styles/fresh')
  }
}

// ===================== 刷新 =====================
async function onRefresh() {
  await loadList()
  ElMessage.success('北斗定位数据已刷新')
}

// ===================== 定位记录 =====================
async function loadRecords() {
  recordLoading.value = true
  try {
    const res = await getBeidouList(recordQuery)
    records.value     = (res?.records ?? []).map(r => ({ ...r, _addrLoading: false }))
    recordTotal.value = res?.total ?? 0
    // 加载完毕后自动解析没有地址的记录
    autoResolveRecordAddresses()
  } catch (_) {
    records.value = []
  } finally {
    recordLoading.value = false
  }
}

/** 自动批量解析定位记录中没有地址的条目（串行，避免限速） */
async function autoResolveRecordAddresses() {
  for (const row of records.value) {
    if (row.address) continue
    if (!row.latitude || !row.longitude) continue
    row._addrLoading = true
    const addr = await reverseGeocode(
      parseFloat(row.longitude), parseFloat(row.latitude), row.id ?? null
    )
    row.address      = addr || `${row.latitude}, ${row.longitude}`
    row._addrLoading = false
  }
}

function resetRecordQuery() {
  Object.assign(recordQuery, { vehicleNo: '', terminalNo: '', pageNum: 1 })
  loadRecords()
}

// ===================== 生命周期 =====================
onMounted(async () => {
  await initMap()
  await loadList()
  loadRecords()
  // 每30秒刷新地图车辆位置
  refreshTimer = setInterval(loadList, 30000)
  // 每30秒刷新定位记录表（与后端定时任务同步）
  recordRefreshTimer = setInterval(loadRecords, 30000)
})

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
  if (recordRefreshTimer) clearInterval(recordRefreshTimer)
  if (amapInstance) { amapInstance.destroy(); amapInstance = null }
  markerMap = {}
})
</script>

<style scoped>
/* ===== 车辆列表 ===== */
.vehicle-list { min-height: 540px; display: flex; flex-direction: column; }
.list-header  {
  display: flex; align-items: center; justify-content: space-between;
  font-weight: 600; color: var(--sky-700);
  margin-bottom: 12px; padding-bottom: 10px; border-bottom: 1px solid #e0f2fe;
}
.vehicle-item {
  display: flex; align-items: flex-start; gap: 10px;
  padding: 10px 8px; border-radius: 8px; cursor: pointer;
  transition: background .15s; margin-bottom: 4px;
}
.vehicle-item:hover  { background: var(--sky-50); }
.vehicle-item.active { background: var(--sky-100); border-left: 3px solid var(--primary); }
.v-avatar {
  width: 36px; height: 36px; border-radius: 50%;
  background: var(--sky-100); display: flex; align-items: center;
  justify-content: center; color: var(--primary); flex-shrink: 0;
}
.vehicle-item.active .v-avatar { background: var(--primary); color: #fff; }
.v-info  { flex: 1; overflow: hidden; }
.v-no    { font-weight: 600; color: #1e293b; font-size: 13px; }
.v-meta  { font-size: 11px; color: var(--sky-600); margin: 2px 0; }
.v-meta .dot { margin: 0 4px; }
.v-addr  { font-size: 11px; color: #94a3b8; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; display: flex; align-items: center; gap: 3px; }
.v-signal { display: flex; flex-direction: column; align-items: center; gap: 3px; flex-shrink: 0; }
.signal-bars { display: flex; gap: 2px; align-items: flex-end; height: 16px; }
.signal-bars span { width: 3px; background: #e2e8f0; border-radius: 1px; transition: background .2s; }
.signal-bars span:nth-child(1) { height: 4px; }
.signal-bars span:nth-child(2) { height: 7px; }
.signal-bars span:nth-child(3) { height: 11px; }
.signal-bars span:nth-child(4) { height: 15px; }
.signal-bars span.active { background: #22c55e; }
.signal-text { font-size: 10px; color: #94a3b8; }
.loading-tip { text-align: center; padding: 20px; color: var(--sky-500); font-size: 13px; display: flex; align-items: center; justify-content: center; gap: 6px; }

/* ===== 地图区域 ===== */
.map-wrap      { padding: 0; overflow: hidden; }
.map-toolbar   { display: flex; align-items: center; justify-content: space-between; padding: 12px 16px; border-bottom: 1px solid #e0f2fe; }
.map-title     { font-weight: 600; color: var(--sky-700); display: flex; align-items: center; gap: 6px; }
.map-container { height: 500px; position: relative; background: #e8f4fd; }

#amap-container {
  width: 100%;
  height: 100%;
}

.map-status-overlay {
  position: absolute; inset: 0; z-index: 99;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  background: rgba(240, 249, 255, 0.95);
}
.map-error { background: rgba(254, 243, 199, 0.95); }

/* ===== 底部记录表 ===== */
.card-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.card-title  { font-size: 15px; font-weight: 600; color: var(--sky-800); }
.pagination  { margin-top: 12px; justify-content: flex-end; }
</style>

<!-- 高德地图标记 & 信息窗口样式（不能 scoped） -->
<style>
/* 车辆标记 */
.bds-marker {
  position: relative;
  display: flex; flex-direction: column; align-items: center;
  cursor: pointer;
}
.bds-dot {
  width: 28px; height: 28px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 2px 8px rgba(14,165,233,.5);
  transition: transform .2s, background .2s;
  position: relative; z-index: 2;
}
.bds-marker:hover .bds-dot,
.bds-marker-selected .bds-dot { transform: scale(1.2); }
.bds-pulse {
  position: absolute; top: 0; left: 0;
  width: 28px; height: 28px; border-radius: 50%;
  border: 2px solid #0ea5e9;
  animation: bds-pulse 2s ease-out infinite;
  z-index: 1;
}
@keyframes bds-pulse {
  0%   { transform: scale(1);   opacity: .7; }
  100% { transform: scale(2.4); opacity: 0;  }
}
.bds-label {
  margin-top: 4px;
  background: rgba(3,105,161,.92);
  color: #fff;
  border-radius: 5px;
  box-shadow: 0 1px 6px rgba(0,0,0,.22);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 3px 8px 4px;
  max-width: 180px;
}
.bds-vno {
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
  line-height: 1.4;
}
.bds-addr {
  font-size: 10px;
  color: #bae6fd;
  white-space: normal;
  word-break: break-all;
  text-align: center;
  line-height: 1.3;
  margin-top: 1px;
}

</style>
