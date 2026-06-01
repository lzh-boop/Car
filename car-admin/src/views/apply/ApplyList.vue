<template>
  <div>
    <div class="page-header">
      <span class="page-title">用车申请</span>
      <el-button type="primary" :icon="Plus" @click="openAdd">发起申请</el-button>
    </div>

    <!-- 用车全流程步骤条 -->
    <div class="page-card flow-card">
      <div class="flow-title">
        <el-icon style="color:#0ea5e9;margin-right:6px"><Guide /></el-icon>
        用车全流程
      </div>
      <el-steps :active="4" finish-status="success" align-center class="flow-steps">
        <el-step title="用车申请" description="员工发起用车申请，填写目的地、时间等信息" :icon="Document" />
        <el-step title="审批管理" description="管理员/部门负责人审批，支持通过或拒绝" :icon="Stamp" />
        <el-step title="调度管理" description="调度员分配车辆与驾驶员，安排出车计划" :icon="Operation" />
        <el-step title="出车行驶" description="驾驶员按计划出车，系统实时记录行驶状态" :icon="Van" />
        <el-step title="还车归档" description="完成行程后还车，记录里程、油量及车况" :icon="CircleCheck" />
      </el-steps>
    </div>

    <div class="search-bar">
      <el-input v-model="query.applyUserName" placeholder="申请人" clearable style="width:140px" />
      <el-input v-model="query.vehicleNo" placeholder="车牌号" clearable style="width:140px" />
      <el-select v-model="query.applyStatus" placeholder="申请状态" clearable style="width:130px">
        <el-option label="待审批" :value="0" />
        <el-option label="已通过" :value="1" />
        <el-option label="已拒绝" :value="2" />
        <el-option label="已取消" :value="3" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="loadList">搜索</el-button>
      <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
    </div>

    <div class="page-card">
      <el-table :data="list" v-loading="loading" stripe border style="width:100%">
        <el-table-column prop="id"             label="申请ID"  width="90" />
        <el-table-column prop="applyUserName"  label="申请人"   width="100" />
        <el-table-column prop="vehicleNo"      label="车牌号"   width="120" />
        <el-table-column prop="purpose"        label="用车目的" width="110" show-overflow-tooltip />
        <el-table-column prop="endLocation"    label="目的地"   show-overflow-tooltip />
        <el-table-column prop="planStartTime"  label="计划出发" width="160" />
        <el-table-column prop="planEndTime"    label="计划到达" width="160" />
        <el-table-column prop="applyStatus"    label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.applyStatus)" size="small">{{ statusLabel(row.applyStatus) }}</el-tag>
          </template>
        </el-table-column>
        <!-- 流程进度列 -->
        <el-table-column label="流程进度" width="200" align="center">
          <template #default="{ row }">
            <div class="mini-flow">
              <span
                v-for="(step, i) in flowSteps"
                :key="i"
                class="mini-step"
                :class="getMiniStepClass(row, i)"
              >
                <el-icon :size="13"><component :is="step.icon" /></el-icon>
                <span class="mini-step-label">{{ step.label }}</span>
                <span v-if="i < flowSteps.length - 1" class="mini-arrow">›</span>
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.applyStatus === 0" text type="warning" size="small" @click="doCancel(row)">撤回</el-button>
            <el-button text type="primary" :icon="View" size="small" @click="openDetail(row)">详情</el-button>
            <el-button text type="danger" :icon="Delete" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pagination" background layout="total, prev, pager, next"
        :total="total" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" @change="loadList" />
    </div>

    <!-- 发起申请 -->
    <el-dialog v-model="dialogVisible" title="发起用车申请" width="520px" draggable>
      <!-- 申请流程说明 -->
      <el-alert type="info" :closable="false" style="margin-bottom:16px">
        <template #title>
          <span style="font-size:13px">
            申请提交后将进入审批流程：
            <strong>用车申请</strong> → <strong>审批管理</strong> → <strong>调度管理</strong> → <strong>出车行驶</strong> → <strong>还车归档</strong>
          </span>
        </template>
      </el-alert>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="申请人" prop="applyUserName">
          <el-input v-model="form.applyUserName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="选择车辆" prop="vehicleNo">
          <el-select v-model="form.vehicleNo" placeholder="请选择空闲车辆" style="width:100%" no-data-text="暂无空闲车辆">
            <el-option
              v-for="v in freeVehicles"
              :key="v.vehicleNo"
              :value="v.vehicleNo"
              :label="`${v.vehicleNo}（${v.vehicleType} · ${v.brand}）`"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="用车目的" prop="purpose">
          <el-select v-model="form.purpose" placeholder="请选择用车目的" style="width:100%">
            <el-option label="公务出行" value="公务出行" />
            <el-option label="商务接待" value="商务接待" />
            <el-option label="物资采购" value="物资采购" />
            <el-option label="设备维修" value="设备维修" />
            <el-option label="员工通勤" value="员工通勤" />
            <el-option label="外出培训" value="外出培训" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="目的地" prop="endLocation">
          <el-input v-model="form.endLocation" placeholder="请输入目的地" />
        </el-form-item>
        <el-form-item label="计划出发" prop="planStartTime">
          <el-date-picker v-model="form.planStartTime" type="datetime" style="width:100%"
            value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择出发时间" />
        </el-form-item>
        <el-form-item label="计划到达" prop="planEndTime">
          <el-date-picker v-model="form.planEndTime" type="datetime" style="width:100%"
            value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择到达时间" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">提 交</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="申请详情" width="560px" draggable>
      <!-- 流程步骤条 -->
      <el-steps :active="getDetailFlowStep(currentRow)" finish-status="success" align-center style="margin-bottom:24px">
        <el-step title="已申请"  :icon="Document" />
        <el-step title="已审批"  :icon="Stamp" />
        <el-step title="已调度"  :icon="Operation" />
        <el-step title="出车中"  :icon="Van" />
        <el-step title="已还车"  :icon="CircleCheck" />
      </el-steps>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="申请ID">{{ currentRow?.id }}</el-descriptions-item>
        <el-descriptions-item label="申请人">{{ currentRow?.applyUserName }}</el-descriptions-item>
        <el-descriptions-item label="车牌号">{{ currentRow?.vehicleNo }}</el-descriptions-item>
        <el-descriptions-item label="用车目的">{{ currentRow?.purpose }}</el-descriptions-item>
        <el-descriptions-item label="目的地" :span="2">{{ currentRow?.endLocation }}</el-descriptions-item>
        <el-descriptions-item label="计划出发">{{ currentRow?.planStartTime }}</el-descriptions-item>
        <el-descriptions-item label="计划到达">{{ currentRow?.planEndTime }}</el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ currentRow?.applyTime }}</el-descriptions-item>
        <el-descriptions-item label="申请状态">
          <el-tag :type="statusType(currentRow?.applyStatus)" size="small">
            {{ statusLabel(currentRow?.applyStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="currentRow?.approveRemark" label="审批意见" :span="2">
          {{ currentRow?.approveRemark }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus, Search, Refresh, View, Delete, Document, Stamp, Operation, Van, CircleCheck, Guide } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getApplyList, createApply, cancelApply, deleteApply } from '@/api/apply'
import { getVehicleList } from '@/api/vehicle'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const loading = ref(false), saving = ref(false)
const list = ref([]), total = ref(0)
const query = reactive({ applyUserName: '', vehicleNo: '', applyStatus: null, pageNum: 1, pageSize: 10 })

const dialogVisible = ref(false)
const detailVisible = ref(false)
const currentRow    = ref(null)
const formRef = ref()
const form = reactive({ applyUserName: '', vehicleNo: '', purpose: '', endLocation: '', planStartTime: '', planEndTime: '' })
const rules = {
  applyUserName:  [{ required: true, message: '请输入申请人姓名' }],
  vehicleNo:      [{ required: true, message: '请选择车辆' }],
  purpose:        [{ required: true, message: '请选择用车目的' }],
  endLocation:    [{ required: true, message: '请输入目的地' }],
  planStartTime:  [{ required: true, message: '请选择出发时间' }],
  planEndTime:    [{ required: true, message: '请选择到达时间' }]
}

const freeVehicles = ref([])
async function loadFreeVehicles() {
  const res = await getVehicleList({ status: 0, pageNum: 1, pageSize: 200 })
  freeVehicles.value = res?.records ?? []
}

const statusLabel = (s) => ['待审批','已通过','已拒绝','已取消'][s] ?? '未知'
const statusType  = (s) => ['warning','success','danger','info'][s] ?? ''

// 流程步骤定义
const flowSteps = [
  { label: '申请', icon: Document },
  { label: '审批', icon: Stamp },
  { label: '调度', icon: Operation },
  { label: '出车', icon: Van },
  { label: '还车', icon: CircleCheck }
]

// 根据申请状态判断当前流程步骤（用于详情弹窗步骤条）
function getDetailFlowStep(row) {
  if (!row) return 0
  const s = Number(row.applyStatus)
  if (s === 3) return 0  // 已取消
  if (s === 2) return 0  // 已拒绝
  if (s === 0) return 1  // 待审批 → 第1步完成
  if (s === 1) return 2  // 已通过 → 第2步完成
  return 1
}

// 迷你流程步骤样式
function getMiniStepClass(row, stepIndex) {
  const s = Number(row.applyStatus)
  // 0:待审批 1:已通过 2:已拒绝 3:已取消
  let activeStep = 0
  if (s === 0) activeStep = 1
  else if (s === 1) activeStep = 2
  else if (s === 2) activeStep = -1  // 拒绝
  else if (s === 3) activeStep = -1  // 取消

  if (activeStep === -1) {
    return stepIndex === 0 ? 'step-done' : 'step-inactive'
  }
  if (stepIndex < activeStep) return 'step-done'
  if (stepIndex === activeStep) return 'step-active'
  return 'step-inactive'
}

async function loadList() {
  loading.value = true
  try {
    const res = await getApplyList(query)
    list.value  = res?.records ?? []
    total.value = res?.total   ?? 0
  } finally { loading.value = false }
}
function resetQuery() {
  Object.assign(query, { applyUserName: '', vehicleNo: '', applyStatus: null, pageNum: 1 })
  loadList()
}
function openAdd() {
  Object.assign(form, {
    applyUserName: userStore.userInfo?.realName || userStore.userInfo?.username || '',
    vehicleNo: '', purpose: '', endLocation: '', planStartTime: '', planEndTime: ''
  })
  loadFreeVehicles()
  dialogVisible.value = true
}
function openDetail(row) {
  currentRow.value = row
  detailVisible.value = true
}
async function handleSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    await createApply(form)
    ElMessage.success('申请提交成功，等待审批')
    dialogVisible.value = false
    loadList()
  } finally { saving.value = false }
}
async function doCancel(row) {
  await ElMessageBox.confirm('确认撤回该申请？', '提示', { type: 'warning' })
  await cancelApply(row.id)
  ElMessage.success('已撤回')
  loadList()
}
async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该申请记录？', '确认删除', { type: 'warning' })
  await deleteApply(row.id)
  ElMessage.success('删除成功')
  loadList()
}
onMounted(loadList)
</script>

<style scoped>
.pagination { margin-top: 16px; justify-content: flex-end; }

/* 流程卡片 */
.flow-card { margin-bottom: 0; padding: 16px 20px 20px; }
.flow-title {
  font-size: 14px;
  font-weight: 600;
  color: #0369a1;
  margin-bottom: 14px;
  display: flex;
  align-items: center;
}
.flow-steps :deep(.el-step__title) { font-size: 13px; font-weight: 600; }
.flow-steps :deep(.el-step__description) { font-size: 11px; color: #64748b; }
.flow-steps :deep(.el-step__icon) { width: 34px; height: 34px; }

/* 迷你流程进度 */
.mini-flow {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2px;
  flex-wrap: nowrap;
}
.mini-step {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  font-size: 11px;
  padding: 2px 4px;
  border-radius: 4px;
  white-space: nowrap;
}
.mini-step-label { font-size: 11px; }
.mini-arrow { color: #cbd5e1; font-size: 12px; margin: 0 1px; }
.step-done    { color: #16a34a; background: #f0fdf4; }
.step-active  { color: #0ea5e9; background: #e0f2fe; font-weight: 600; }
.step-inactive { color: #cbd5e1; }
</style>
