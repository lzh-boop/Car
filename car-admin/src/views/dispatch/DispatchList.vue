<template>
  <div>
    <div class="page-header">
      <span class="page-title">调度管理</span>
      <el-button type="primary" :icon="Plus" @click="openAdd">创建调度单</el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="query.vehicleNo" placeholder="车牌号" clearable style="width:150px" />
      <el-select v-model="query.dispatchStatus" placeholder="调度状态" clearable style="width:130px">
        <el-option label="待出车" :value="0" />
        <el-option label="行驶中" :value="1" />
        <el-option label="已完成" :value="2" />
        <el-option label="已取消" :value="3" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="loadList">搜索</el-button>
      <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
    </div>

    <div class="page-card">
      <el-table :data="list" v-loading="loading" stripe border style="width:100%" table-layout="auto">
        <el-table-column prop="id"             label="调度ID"   width="80" />
        <el-table-column prop="vehicleNo"      label="车牌号"   width="110" />
        <el-table-column                       label="车辆信息" width="150">
          <template #default="{ row }">
            <span v-if="row.vehicleType || row.brand">{{ row.vehicleType }}{{ row.brand ? ' · ' + row.brand : '' }}</span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="driverName" label="驾驶员" width="130">
          <template #default="{ row }">
            <!-- driverId 为 null 时表示驾驶员未在系统中正式分配 -->
            <span v-if="row.driverId == null && row.driverName">
              {{ row.driverName }}
              <el-tag type="info" size="small" style="margin-left:4px">申请人</el-tag>
            </span>
            <el-tag v-else-if="!row.driverName" type="warning" size="small">待分配</el-tag>
            <span v-else>{{ row.driverName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="endLocation"    label="目的地"   min-width="100" show-overflow-tooltip />
        <el-table-column prop="planStartTime"  label="计划出发" width="155" />
        <el-table-column prop="planEndTime"    label="计划到达" width="155" />
        <el-table-column prop="dispatchStatus" label="状态"     width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.dispatchStatus)" size="small">{{ statusLabel(row.dispatchStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="230">
          <template #default="{ row }">
            <!-- 待出车且没有驾驶员姓名：显示"分配驾驶员"按钮 -->
            <el-button
              v-if="row.dispatchStatus === 0 && !row.driverName"
              text type="warning" size="small" :icon="User"
              @click="openAssignDriver(row)"
            >分配驾驶员</el-button>
            <!-- 待出车且已有驾驶员姓名（含申请人同步过来的姓名）：直接出车 -->
            <el-button v-if="row.dispatchStatus === 0 && row.driverName"
              text type="primary" size="small" @click="doStart(row)">出车</el-button>
            <el-button v-if="row.dispatchStatus === 1" text type="success" size="small" @click="doComplete(row)">完成</el-button>
            <el-button v-if="row.dispatchStatus === 0" text type="warning" size="small" @click="doCancel(row)">取消</el-button>
            <el-button text type="primary" :icon="Edit"   size="small" @click="openEdit(row)">编辑</el-button>
            <el-button text type="danger"  :icon="Delete" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        class="pagination" background layout="total, prev, pager, next"
        :total="total" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        @change="loadList"
      />
    </div>

    <!-- 创建 / 编辑调度单 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑调度单' : '创建调度单'" width="520px" draggable>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">

        <!-- 选择空闲车辆 -->
        <el-form-item label="选择车辆" prop="vehicleNo">
          <el-select
            v-model="form.vehicleNo"
            placeholder="请选择空闲车辆"
            style="width:100%"
            no-data-text="暂无空闲车辆"
            @change="onVehicleChange"
          >
            <el-option
              v-for="v in freeVehicles"
              :key="v.vehicleNo"
              :value="v.vehicleNo"
              :label="`${v.vehicleNo}（${v.vehicleType}${v.brand ? ' · ' + v.brand : ''}）`"
            />
          </el-select>
        </el-form-item>

        <!-- 已选车辆信息展示 -->
        <el-form-item v-if="selectedVehicle" label="车辆详情">
          <el-descriptions :column="2" border size="small" style="width:100%">
            <el-descriptions-item label="车型">{{ selectedVehicle.vehicleType }}</el-descriptions-item>
            <el-descriptions-item label="品牌">{{ selectedVehicle.brand || '—' }}</el-descriptions-item>
            <el-descriptions-item label="座位数">{{ selectedVehicle.seats || '—' }}</el-descriptions-item>
            <el-descriptions-item label="里程(km)">{{ selectedVehicle.mileage ?? '—' }}</el-descriptions-item>
          </el-descriptions>
        </el-form-item>

        <!-- 选择驾驶员 -->
        <el-form-item label="驾驶员" prop="driverId">
          <el-select
            v-model="form.driverId"
            placeholder="请选择驾驶员"
            style="width:100%"
            @change="onDriverChange"
          >
            <el-option
              v-for="d in drivers"
              :key="d.id"
              :value="d.id"
              :label="d.driverName"
            />
          </el-select>
        </el-form-item>

        <!-- 目的地 -->
        <el-form-item label="目的地" prop="endLocation">
          <el-input v-model="form.endLocation" placeholder="请输入目的地" />
        </el-form-item>

        <!-- 计划出发 -->
        <el-form-item label="计划出发" prop="planStartTime">
          <el-date-picker v-model="form.planStartTime" type="datetime" style="width:100%"
            value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择出发时间" />
        </el-form-item>

        <!-- 计划到达 -->
        <el-form-item label="计划到达" prop="planEndTime">
          <el-date-picker v-model="form.planEndTime" type="datetime" style="width:100%"
            value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择到达时间" />
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 分配驾驶员弹窗 -->
    <el-dialog v-model="assignDriverDialog" title="分配驾驶员" width="400px" draggable>
      <el-alert type="info" :closable="false" style="margin-bottom:16px">
        <template #title>
          <span style="font-size:13px">
            请为车辆 <strong>{{ assignRow?.vehicleNo }}</strong> 分配驾驶员。
            <span v-if="assignRow?.driverName">当前申请人：<strong>{{ assignRow.driverName }}</strong></span>
          </span>
        </template>
      </el-alert>
      <el-form ref="assignFormRef" :model="assignForm" :rules="assignRules" label-width="80px">
        <el-form-item label="驾驶员" prop="driverId">
          <el-select v-model="assignForm.driverId" placeholder="请选择驾驶员" style="width:100%" @change="onAssignDriverChange">
            <el-option
              v-for="d in drivers"
              :key="d.id"
              :value="d.id"
              :label="d.driverName + (d.phone ? '  ' + d.phone : '')"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDriverDialog = false">取 消</el-button>
        <el-button type="primary" :loading="assignSaving" @click="handleAssignDriver">确认分配</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus, Search, Refresh, Edit, Delete, User } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getDispatchList, createDispatch, updateDispatch,
  startDispatch, completeDispatch, cancelDispatch, deleteDispatch
} from '@/api/dispatch'
import { getVehicleList } from '@/api/vehicle'
import { getDriverList }  from '@/api/driver'

const loading = ref(false)
const saving  = ref(false)
const list    = ref([])
const total   = ref(0)
const query   = reactive({ vehicleNo: '', dispatchStatus: null, pageNum: 1, pageSize: 10 })

// 空闲车辆列表
const freeVehicles    = ref([])
const selectedVehicle = ref(null)

async function loadFreeVehicles() {
  const res = await getVehicleList({ status: 0, pageNum: 1, pageSize: 200 })
  freeVehicles.value = res?.records ?? []
}

function onVehicleChange(vehicleNo) {
  selectedVehicle.value = freeVehicles.value.find(v => v.vehicleNo === vehicleNo) ?? null
  if (selectedVehicle.value) {
    form.vehicleId = selectedVehicle.value.id
  }
}

// 驾驶员列表
const drivers = ref([])
async function loadDrivers() {
  const res = await getDriverList({ pageNum: 1, pageSize: 200 })
  drivers.value = res?.records ?? []
}

function onDriverChange(driverId) {
  const d = drivers.value.find(d => d.id === driverId)
  form.driverName = d ? d.driverName : ''
}

// 表单
const dialogVisible = ref(false)
const isEdit        = ref(false)
const formRef       = ref()
const form = reactive({
  id: null, vehicleId: null, vehicleNo: '', driverId: null, driverName: '',
  endLocation: '', planStartTime: '', planEndTime: '', remark: ''
})
const rules = {
  vehicleNo:     [{ required: true, message: '请选择车辆' }],
  driverId:      [{ required: true, message: '请选择驾驶员' }],
  endLocation:   [{ required: true, message: '请输入目的地' }],
  planStartTime: [{ required: true, message: '请选择出发时间' }],
  planEndTime:   [{ required: true, message: '请选择到达时间' }]
}

// ─── 分配驾驶员弹窗 ──────────────────────────────────────
const assignDriverDialog = ref(false)
const assignSaving       = ref(false)
const assignRow          = ref(null)
const assignFormRef      = ref()
const assignForm = reactive({ driverId: null, driverName: '' })
const assignRules = {
  driverId: [{ required: true, message: '请选择驾驶员' }]
}

function onAssignDriverChange(driverId) {
  const d = drivers.value.find(d => d.id === driverId)
  assignForm.driverName = d ? d.driverName : ''
}

function openAssignDriver(row) {
  assignRow.value = row
  assignForm.driverId   = null
  assignForm.driverName = ''
  // 确保驾驶员列表已加载
  if (drivers.value.length === 0) loadDrivers()
  assignDriverDialog.value = true
}

async function handleAssignDriver() {
  await assignFormRef.value.validate()
  assignSaving.value = true
  try {
    // 通过 updateDispatch 接口更新驾驶员信息
    await updateDispatch({
      id:         assignRow.value.id,
      vehicleId:  assignRow.value.vehicleId,
      vehicleNo:  assignRow.value.vehicleNo,
      driverId:   assignForm.driverId,
      driverName: assignForm.driverName,
      endLocation:   assignRow.value.endLocation,
      planStartTime: assignRow.value.planStartTime,
      planEndTime:   assignRow.value.planEndTime
    })
    ElMessage.success(`已为 ${assignRow.value.vehicleNo} 分配驾驶员：${assignForm.driverName}`)
    assignDriverDialog.value = false
    loadList()
  } finally { assignSaving.value = false }
}

const statusLabel = (s) => ['待出车', '行驶中', '已完成', '已取消'][s] ?? '未知'
const statusType  = (s) => ['warning', 'primary', 'success', 'info'][s] ?? ''

async function loadList() {
  loading.value = true
  try {
    const res = await getDispatchList(query)
    list.value  = res?.records ?? []
    total.value = res?.total   ?? 0
  } finally { loading.value = false }
}

function resetQuery() {
  Object.assign(query, { vehicleNo: '', dispatchStatus: null, pageNum: 1 })
  loadList()
}

function resetForm() {
  Object.assign(form, {
    id: null, vehicleId: null, vehicleNo: '', driverId: null, driverName: '',
    endLocation: '', planStartTime: '', planEndTime: '', remark: ''
  })
  selectedVehicle.value = null
}

function openAdd() {
  isEdit.value = false
  resetForm()
  loadFreeVehicles()
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  resetForm()
  Object.assign(form, {
    id:            row.id,
    vehicleId:     row.vehicleId,
    vehicleNo:     row.vehicleNo,
    driverId:      row.driverId ?? null,
    driverName:    row.driverName || '',
    endLocation:   row.endLocation,
    planStartTime: row.planStartTime,
    planEndTime:   row.planEndTime,
    remark:        row.remark
  })
  loadFreeVehiclesForEdit(row)
  dialogVisible.value = true
}

async function loadFreeVehiclesForEdit(row) {
  const res = await getVehicleList({ status: 0, pageNum: 1, pageSize: 200 })
  const free = res?.records ?? []
  const exists = free.some(v => v.vehicleNo === row.vehicleNo)
  if (!exists && row.vehicleNo) {
    const detail = await getVehicleList({ vehicleNo: row.vehicleNo, pageNum: 1, pageSize: 1 })
    const cur = detail?.records?.[0]
    if (cur) free.unshift(cur)
  }
  freeVehicles.value = free
  selectedVehicle.value = free.find(v => v.vehicleNo === row.vehicleNo) ?? null
}

async function handleSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (isEdit.value) {
      await updateDispatch(form)
      ElMessage.success('更新成功')
    } else {
      await createDispatch(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadList()
  } finally { saving.value = false }
}

async function doStart(row) {
  await ElMessageBox.confirm('确认开始出车？', '提示', { type: 'info' })
  await startDispatch(row.id)
  ElMessage.success('已开始出车')
  loadList()
}
async function doComplete(row) {
  await ElMessageBox.confirm('确认完成调度？', '提示', { type: 'success' })
  await completeDispatch(row.id)
  ElMessage.success('调度已完成')
  loadList()
}
async function doCancel(row) {
  await ElMessageBox.confirm('确认取消该调度单？', '提示', { type: 'warning' })
  await cancelDispatch(row.id)
  ElMessage.success('已取消')
  loadList()
}
async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除调度单 #${row.id}？`, '确认删除', { type: 'warning' })
  await deleteDispatch(row.id)
  ElMessage.success('删除成功')
  loadList()
}

onMounted(() => {
  loadList()
  loadDrivers()
})
</script>

<style scoped>
.pagination { margin-top: 16px; justify-content: flex-end; }
.text-muted { color: #c0c4cc; }
</style>
