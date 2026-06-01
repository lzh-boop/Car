<template>
  <div>
    <div class="page-header">
      <span class="page-title">保养维修</span>
      <div class="header-actions">
        <el-select v-model="selectedVehicleNo" placeholder="选择车辆查看档案" style="width:220px" clearable @change="onVehicleChange">
          <el-option
            v-for="v in vehicleOptions"
            :key="v.vehicleNo"
            :value="v.vehicleNo"
            :label="`${v.vehicleNo}（${v.vehicleType}${v.brand ? ' · ' + v.brand : ''}）`"
          />
        </el-select>
      </div>
    </div>

    <!-- 一车一档卡片 -->
    <transition name="fade">
      <div v-if="selectedVehicle" class="archive-header page-card">
        <div class="archive-badge">
          <el-icon :size="36" style="color:#0ea5e9"><Van /></el-icon>
        </div>
        <div class="archive-info">
          <div class="archive-no">{{ selectedVehicle.vehicleNo }}</div>
          <div class="archive-meta">
            <el-tag size="small" type="info">{{ selectedVehicle.vehicleType }}</el-tag>
            <span v-if="selectedVehicle.brand" class="meta-item">{{ selectedVehicle.brand }}</span>
            <span class="meta-item">{{ selectedVehicle.seats }} 座</span>
            <span class="meta-item">{{ selectedVehicle.mileage ?? 0 }} km</span>
            <el-tag :type="vehicleStatusType(selectedVehicle.status)" size="small">
              {{ vehicleStatusLabel(selectedVehicle.status) }}
            </el-tag>
          </div>
        </div>
        <div class="archive-stats">
          <div class="arc-stat">
            <div class="arc-val">{{ maintainTotal }}</div>
            <div class="arc-label">保养记录</div>
          </div>
          <div class="arc-stat">
            <div class="arc-val">{{ repairTotal }}</div>
            <div class="arc-label">维修记录</div>
          </div>
          <div class="arc-stat">
            <div class="arc-val">{{ insuranceTotal }}</div>
            <div class="arc-label">保险记录</div>
          </div>
        </div>
      </div>
    </transition>

    <!-- Tab 页签 -->
    <div class="page-card tab-card">
      <el-tabs v-model="activeTab" @tab-change="onTabChange">

        <!-- 保养记录 maintenanceType=1 -->
        <el-tab-pane label="🔧 保养记录" name="maintain">
          <div class="tab-toolbar">
            <el-button type="primary" :icon="Plus" size="small" @click="openAdd(1)" :disabled="!selectedVehicleNo">
              新增保养
            </el-button>
            <el-button :icon="Download" size="small" @click="exportData('maintain')" :disabled="maintainList.length === 0">
              导出归档
            </el-button>
          </div>
          <el-empty v-if="!selectedVehicleNo" description="请先选择车辆" :image-size="80" />
          <template v-else>
            <el-table :data="maintainList" v-loading="tabLoading" stripe border style="width:100%">
              <el-table-column prop="maintenanceDate"     label="保养日期"     width="120" />
              <el-table-column prop="maintenanceItem"     label="保养项目"     show-overflow-tooltip />
              <el-table-column prop="currentMileage"      label="里程(km)"     width="120" align="right" />
              <el-table-column prop="maintenanceCost"     label="费用(元)"     width="110" align="right">
                <template #default="{ row }">
                  <span class="cost-text">¥ {{ row.maintenanceCost ?? 0 }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="nextMaintenanceDate" label="下次保养"     width="120" />
              <el-table-column prop="serviceProvider"     label="服务站"       show-overflow-tooltip />
              <el-table-column prop="remark"              label="备注"         show-overflow-tooltip />
              <el-table-column label="操作" width="80" fixed="right">
                <template #default="{ row }">
                  <el-button text type="danger" :icon="Delete" size="small" @click="handleDelete(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              class="pagination" background layout="total, prev, pager, next"
              :total="maintainTotal" v-model:current-page="maintainPage" :page-size="pageSize"
              @current-change="loadMaintain"
            />
          </template>
        </el-tab-pane>

        <!-- 维修记录 maintenanceType=2 -->
        <el-tab-pane label="🔩 维修记录" name="repair">
          <div class="tab-toolbar">
            <el-button type="primary" :icon="Plus" size="small" @click="openAdd(2)" :disabled="!selectedVehicleNo">
              新增维修
            </el-button>
            <el-button :icon="Download" size="small" @click="exportData('repair')" :disabled="repairList.length === 0">
              导出归档
            </el-button>
          </div>
          <el-empty v-if="!selectedVehicleNo" description="请先选择车辆" :image-size="80" />
          <template v-else>
            <el-table :data="repairList" v-loading="tabLoading" stripe border style="width:100%">
              <el-table-column prop="maintenanceDate"  label="维修日期"   width="120" />
              <el-table-column prop="maintenanceItem"  label="维修内容"   show-overflow-tooltip />
              <el-table-column prop="maintenanceCost"  label="费用(元)"   width="110" align="right">
                <template #default="{ row }">
                  <span class="cost-text">¥ {{ row.maintenanceCost ?? 0 }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="serviceProvider"  label="维修厂"     show-overflow-tooltip />
              <el-table-column prop="remark"           label="备注"       show-overflow-tooltip />
              <el-table-column label="操作" width="80" fixed="right">
                <template #default="{ row }">
                  <el-button text type="danger" :icon="Delete" size="small" @click="handleDelete(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              class="pagination" background layout="total, prev, pager, next"
              :total="repairTotal" v-model:current-page="repairPage" :page-size="pageSize"
              @current-change="loadRepair"
            />
          </template>
        </el-tab-pane>

        <!-- 保险记录 maintenanceType=4 -->
        <el-tab-pane label="🛡️ 保险记录" name="insurance">
          <div class="tab-toolbar">
            <el-button type="primary" :icon="Plus" size="small" @click="openAdd(4)" :disabled="!selectedVehicleNo">
              新增保险
            </el-button>
            <el-button :icon="Download" size="small" @click="exportData('insurance')" :disabled="insuranceList.length === 0">
              导出归档
            </el-button>
          </div>
          <el-empty v-if="!selectedVehicleNo" description="请先选择车辆" :image-size="80" />
          <template v-else>
            <el-table :data="insuranceList" v-loading="tabLoading" stripe border style="width:100%">
              <el-table-column prop="maintenanceDate"     label="生效日期"   width="120" />
              <el-table-column prop="nextMaintenanceDate" label="到期日期"   width="120">
                <template #default="{ row }">
                  <span :class="isExpiringSoon(row.nextMaintenanceDate) ? 'expiring-soon' : ''">
                    {{ row.nextMaintenanceDate }}
                    <el-tag v-if="isExpiringSoon(row.nextMaintenanceDate)" type="danger" size="small" style="margin-left:4px">即将到期</el-tag>
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="maintenanceItem"  label="险种/保单信息" show-overflow-tooltip />
              <el-table-column prop="maintenanceCost"  label="保费(元)"     width="110" align="right">
                <template #default="{ row }">
                  <span class="cost-text">¥ {{ row.maintenanceCost ?? 0 }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="serviceProvider"  label="保险公司"     show-overflow-tooltip />
              <el-table-column prop="remark"           label="备注"         show-overflow-tooltip />
              <el-table-column label="操作" width="80" fixed="right">
                <template #default="{ row }">
                  <el-button text type="danger" :icon="Delete" size="small" @click="handleDelete(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
              class="pagination" background layout="total, prev, pager, next"
              :total="insuranceTotal" v-model:current-page="insurancePage" :page-size="pageSize"
              @current-change="loadInsurance"
            />
          </template>
        </el-tab-pane>

      </el-tabs>
    </div>

    <!-- 新增弹窗（通用，根据 addType 切换标题和字段） -->
    <el-dialog v-model="addDialog" :title="addDialogTitle" width="500px" draggable>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">

        <!-- 公共：日期 -->
        <el-form-item :label="addType === 4 ? '生效日期' : (addType === 1 ? '保养日期' : '维修日期')" prop="maintenanceDate">
          <el-date-picker v-model="form.maintenanceDate" type="date" style="width:100%"
            value-format="YYYY-MM-DD" :placeholder="addType === 4 ? '选择生效日期' : '选择日期'" />
        </el-form-item>

        <!-- 保养：项目 -->
        <el-form-item v-if="addType === 1" label="保养类型" prop="maintenanceItem">
          <el-select v-model="form.maintenanceItem" style="width:100%" placeholder="请选择保养类型">
            <el-option label="常规保养（换机油+三滤）" value="常规保养（换机油+三滤）" />
            <el-option label="小保养" value="小保养" />
            <el-option label="大保养" value="大保养" />
            <el-option label="换轮胎" value="换轮胎" />
            <el-option label="换刹车片" value="换刹车片" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <!-- 维修：内容 -->
        <el-form-item v-if="addType === 2" label="维修内容" prop="maintenanceItem">
          <el-input v-model="form.maintenanceItem" type="textarea" :rows="2" placeholder="请描述维修内容" />
        </el-form-item>

        <!-- 保险：险种+保单 -->
        <el-form-item v-if="addType === 4" label="险种/保单" prop="maintenanceItem">
          <el-input v-model="form.maintenanceItem" placeholder="如：交强险 保单号：XXXXXX" />
        </el-form-item>

        <!-- 保养/维修：里程 -->
        <el-form-item v-if="addType !== 4" label="当前里程(km)" prop="currentMileage">
          <el-input-number v-model="form.currentMileage" :min="0" :precision="0" style="width:100%" />
        </el-form-item>

        <!-- 费用 -->
        <el-form-item :label="addType === 4 ? '保费(元)' : '费用(元)'" prop="maintenanceCost">
          <el-input-number v-model="form.maintenanceCost" :min="0" :precision="2" style="width:100%" />
        </el-form-item>

        <!-- 保养：下次保养日期 -->
        <el-form-item v-if="addType === 1" label="下次保养">
          <el-date-picker v-model="form.nextMaintenanceDate" type="date" style="width:100%"
            value-format="YYYY-MM-DD" placeholder="选择下次保养日期" />
        </el-form-item>

        <!-- 保险：到期日期 -->
        <el-form-item v-if="addType === 4" label="到期日期" prop="nextMaintenanceDate">
          <el-date-picker v-model="form.nextMaintenanceDate" type="date" style="width:100%"
            value-format="YYYY-MM-DD" placeholder="选择到期日期" />
        </el-form-item>

        <!-- 服务商/维修厂/保险公司 -->
        <el-form-item :label="addType === 1 ? '服务站' : (addType === 2 ? '维修厂' : '保险公司')">
          <el-input v-model="form.serviceProvider" :placeholder="addType === 1 ? '请输入服务站名称' : (addType === 2 ? '请输入维修厂名称' : '请输入保险公司名称')" />
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialog = false">取 消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus, Delete, Download, Van } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getVehicleList } from '@/api/vehicle'
import { getMaintenanceList, addMaintenance, deleteMaintenance } from '@/api/vehicle'

// ─── 车辆选择 ───────────────────────────────────────────
const vehicleOptions    = ref([])
const selectedVehicleNo = ref('')
const selectedVehicle   = ref(null)
const activeTab         = ref('maintain')
const tabLoading        = ref(false)
const saving            = ref(false)
const pageSize          = 10

const vehicleStatusLabel = (s) => ({ 0: '空闲', 1: '在用', 2: '维修', 3: '报废' }[s] ?? '未知')
const vehicleStatusType  = (s) => ({ 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }[s] ?? '')

async function loadVehicles() {
  const res = await getVehicleList({ pageNum: 1, pageSize: 200 })
  vehicleOptions.value = res?.records ?? []
}

function onVehicleChange(vehicleNo) {
  selectedVehicle.value = vehicleOptions.value.find(v => v.vehicleNo === vehicleNo) ?? null
  if (vehicleNo) {
    loadMaintain()
    loadRepair()
    loadInsurance()
  } else {
    maintainList.value  = []
    repairList.value    = []
    insuranceList.value = []
    maintainTotal.value = repairTotal.value = insuranceTotal.value = 0
  }
}

function onTabChange() {
  // 切换 Tab 时数据已加载，无需重复请求
}

// ─── 保养 maintenanceType=1 ──────────────────────────────
const maintainList  = ref([])
const maintainTotal = ref(0)
const maintainPage  = ref(1)

async function loadMaintain() {
  if (!selectedVehicleNo.value) return
  tabLoading.value = true
  try {
    const res = await getMaintenanceList({
      vehicleNo: selectedVehicleNo.value,
      maintenanceType: 1,
      pageNum: maintainPage.value,
      pageSize
    })
    maintainList.value  = res?.records ?? []
    maintainTotal.value = res?.total   ?? 0
  } finally { tabLoading.value = false }
}

// ─── 维修 maintenanceType=2 ──────────────────────────────
const repairList  = ref([])
const repairTotal = ref(0)
const repairPage  = ref(1)

async function loadRepair() {
  if (!selectedVehicleNo.value) return
  tabLoading.value = true
  try {
    const res = await getMaintenanceList({
      vehicleNo: selectedVehicleNo.value,
      maintenanceType: 2,
      pageNum: repairPage.value,
      pageSize
    })
    repairList.value  = res?.records ?? []
    repairTotal.value = res?.total   ?? 0
  } finally { tabLoading.value = false }
}

// ─── 保险 maintenanceType=4 ──────────────────────────────
const insuranceList  = ref([])
const insuranceTotal = ref(0)
const insurancePage  = ref(1)

async function loadInsurance() {
  if (!selectedVehicleNo.value) return
  tabLoading.value = true
  try {
    const res = await getMaintenanceList({
      vehicleNo: selectedVehicleNo.value,
      maintenanceType: 4,
      pageNum: insurancePage.value,
      pageSize
    })
    insuranceList.value  = res?.records ?? []
    insuranceTotal.value = res?.total   ?? 0
  } finally { tabLoading.value = false }
}

// ─── 新增弹窗（通用）────────────────────────────────────
const addDialog = ref(false)
const addType   = ref(1)   // 1=保养 2=维修 4=保险
const formRef   = ref()
const form = reactive({
  maintenanceDate:     '',
  maintenanceItem:     '',
  currentMileage:      0,
  maintenanceCost:     0,
  nextMaintenanceDate: '',
  serviceProvider:     '',
  remark:              ''
})

const addDialogTitle = computed(() => ({ 1: '新增保养记录', 2: '新增维修记录', 4: '新增保险记录' }[addType.value] ?? '新增记录'))

const formRules = computed(() => ({
  maintenanceDate: [{ required: true, message: '请选择日期' }],
  maintenanceItem: [{ required: true, message: '请填写内容' }],
  ...(addType.value === 4 ? { nextMaintenanceDate: [{ required: true, message: '请选择到期日期' }] } : {})
}))

function openAdd(type) {
  addType.value = type
  Object.assign(form, {
    maintenanceDate:     '',
    maintenanceItem:     '',
    currentMileage:      selectedVehicle.value?.mileage ?? 0,
    maintenanceCost:     0,
    nextMaintenanceDate: '',
    serviceProvider:     '',
    remark:              ''
  })
  addDialog.value = true
}

async function handleSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    await addMaintenance({
      vehicleNo:           selectedVehicleNo.value,
      maintenanceType:     addType.value,
      maintenanceDate:     form.maintenanceDate,
      maintenanceItem:     form.maintenanceItem,
      currentMileage:      form.currentMileage,
      maintenanceCost:     form.maintenanceCost,
      nextMaintenanceDate: form.nextMaintenanceDate || null,
      serviceProvider:     form.serviceProvider,
      remark:              form.remark
    })
    ElMessage.success('保存成功')
    addDialog.value = false
    // 刷新对应 Tab
    if (addType.value === 1) loadMaintain()
    else if (addType.value === 2) loadRepair()
    else loadInsurance()
  } finally { saving.value = false }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该记录？', '确认删除', { type: 'warning' })
  await deleteMaintenance(row.id)
  ElMessage.success('删除成功')
  if (activeTab.value === 'maintain')  loadMaintain()
  else if (activeTab.value === 'repair') loadRepair()
  else loadInsurance()
}

// ─── 判断保险即将到期（30天内）────────────────────────────
function isExpiringSoon(endDate) {
  if (!endDate) return false
  const diff = new Date(endDate) - new Date()
  return diff > 0 && diff < 30 * 24 * 3600 * 1000
}

// ─── 导出归档（CSV）─────────────────────────────────────
function exportData(type) {
  const listMap = { maintain: maintainList, repair: repairList, insurance: insuranceList }
  const data = listMap[type].value
  if (!data.length) return

  const headers = {
    maintain:  ['保养日期','保养项目','里程(km)','费用(元)','下次保养','服务站','备注'],
    repair:    ['维修日期','维修内容','费用(元)','维修厂','备注'],
    insurance: ['生效日期','到期日期','险种/保单','保费(元)','保险公司','备注']
  }
  const fields = {
    maintain:  ['maintenanceDate','maintenanceItem','currentMileage','maintenanceCost','nextMaintenanceDate','serviceProvider','remark'],
    repair:    ['maintenanceDate','maintenanceItem','maintenanceCost','serviceProvider','remark'],
    insurance: ['maintenanceDate','nextMaintenanceDate','maintenanceItem','maintenanceCost','serviceProvider','remark']
  }

  const rows = [headers[type], ...data.map(r => fields[type].map(f => r[f] ?? ''))]
  const csv  = rows.map(r => r.join(',')).join('\n')
  const blob = new Blob(['\uFEFF' + csv], { type: 'text/csv;charset=utf-8;' })
  const url  = URL.createObjectURL(blob)
  const a    = document.createElement('a')
  a.href     = url
  a.download = `${selectedVehicleNo.value}_${type}_${new Date().toLocaleDateString()}.csv`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

onMounted(loadVehicles)
</script>

<style scoped>
/* 档案头部 */
.archive-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 16px 24px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-left: 4px solid #0ea5e9;
}
.archive-badge {
  width: 60px; height: 60px;
  background: #fff;
  border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 2px 8px rgba(14,165,233,.2);
  flex-shrink: 0;
}
.archive-info { flex: 1; }
.archive-no {
  font-size: 22px;
  font-weight: 700;
  color: #0369a1;
  letter-spacing: 1px;
}
.archive-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 6px;
  flex-wrap: wrap;
}
.meta-item { font-size: 13px; color: #64748b; }
.archive-stats {
  display: flex;
  gap: 24px;
  flex-shrink: 0;
}
.arc-stat { text-align: center; }
.arc-val {
  font-size: 24px;
  font-weight: 700;
  color: #0ea5e9;
  line-height: 1;
}
.arc-label { font-size: 12px; color: #64748b; margin-top: 4px; }

/* Tab 卡片 */
.tab-card { padding: 0; overflow: hidden; }
.tab-card :deep(.el-tabs__header) { padding: 0 20px; margin: 0; background: #fafafa; border-bottom: 1px solid #e0f2fe; }
.tab-card :deep(.el-tabs__content) { padding: 16px 20px; }
.tab-card :deep(.el-tabs__item) { font-size: 14px; }

.tab-toolbar { display: flex; gap: 8px; margin-bottom: 12px; }
.pagination   { margin-top: 12px; justify-content: flex-end; }

.cost-text    { color: #0369a1; font-weight: 500; }
.expiring-soon { color: #dc2626; font-weight: 500; }
.header-actions { display: flex; gap: 8px; align-items: center; }
</style>
