<template>
  <div>
    <div class="page-header">
      <span class="page-title">还车管理</span>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="query.vehicleNo" placeholder="车牌号" clearable style="width:140px" />
      <el-input v-model="query.driverName" placeholder="驾驶员" clearable style="width:140px" />
      <el-select v-model="query.returnStatus" placeholder="还车状态" clearable style="width:130px">
        <el-option label="待还车" :value="0" />
        <el-option label="已还车" :value="1" />
        <el-option label="已取消" :value="2" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="loadList">搜索</el-button>
      <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
    </div>

    <!-- 列表 -->
    <div class="page-card">
      <el-table :data="list" v-loading="loading" stripe border style="width:100%" table-layout="auto">
        <el-table-column prop="id"           label="记录ID"   width="80" />
        <el-table-column prop="vehicleNo"    label="车牌号"   width="110" />
        <el-table-column                     label="车辆信息" width="150">
          <template #default="{ row }">
            <span v-if="row.vehicleType || row.brand">
              {{ row.vehicleType }}{{ row.brand ? ' · ' + row.brand : '' }}
            </span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="driverName"   label="驾驶员"   width="90" />
        <el-table-column prop="endLocation"  label="目的地"   min-width="100" show-overflow-tooltip />
        <el-table-column prop="planEndTime"  label="计划还车" width="160" />
        <el-table-column prop="actualEndTime" label="实际还车" width="160">
          <template #default="{ row }">
            <span v-if="row.actualEndTime">{{ row.actualEndTime }}</span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="里程(km)" width="110" align="center">
          <template #default="{ row }">
            <span v-if="row.returnStatus === 1">
              {{ row.mileageBefore }} → {{ row.mileageAfter }}
            </span>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="油量" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="fuelType(row.fuelLevel)" size="small">{{ fuelLabel(row.fuelLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="车况" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="conditionType(row.vehicleCondition)" size="small">{{ conditionLabel(row.vehicleCondition) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="returnStatus" label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="returnStatusType(row.returnStatus)" size="small">
              {{ returnStatusLabel(row.returnStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="150">
          <template #default="{ row }">
            <el-button
              v-if="row.returnStatus === 0"
              text type="primary" size="small"
              :icon="Van"
              @click="openReturn(row)"
            >还车</el-button>
            <el-button
              v-if="row.returnStatus === 1"
              text type="info" size="small"
              :icon="View"
              @click="openDetail(row)"
            >详情</el-button>
            <el-button text type="danger" size="small" :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        class="pagination" background layout="total, prev, pager, next"
        :total="total" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        @change="loadList"
      />
    </div>

    <!-- 还车弹窗 -->
    <el-dialog v-model="returnVisible" title="办理还车" width="520px" draggable>
      <!-- 基本信息展示 -->
      <el-descriptions :column="2" border size="small" style="margin-bottom:20px">
        <el-descriptions-item label="车牌号">{{ currentRow?.vehicleNo }}</el-descriptions-item>
        <el-descriptions-item label="车辆信息">
          {{ currentRow?.vehicleType }}{{ currentRow?.brand ? ' · ' + currentRow.brand : '' }}
        </el-descriptions-item>
        <el-descriptions-item label="驾驶员">{{ currentRow?.driverName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="目的地">{{ currentRow?.endLocation || '—' }}</el-descriptions-item>
        <el-descriptions-item label="计划还车">{{ currentRow?.planEndTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="出发里程(km)">{{ currentRow?.mileageBefore ?? 0 }}</el-descriptions-item>
      </el-descriptions>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="归还里程" prop="mileageAfter">
          <el-input-number v-model="form.mileageAfter" :min="form.mileageBefore" style="width:100%" />
          <div class="form-tip">本次行驶：{{ (form.mileageAfter - form.mileageBefore) }} km</div>
        </el-form-item>
        <el-form-item label="油量状态" prop="fuelLevel">
          <el-radio-group v-model="form.fuelLevel">
            <el-radio :value="0"><el-tag type="success" size="small">充足</el-tag></el-radio>
            <el-radio :value="1"><el-tag type="warning" size="small">偏少</el-tag></el-radio>
            <el-radio :value="2"><el-tag type="danger"  size="small">需加油</el-tag></el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="车辆状况" prop="vehicleCondition">
          <el-radio-group v-model="form.vehicleCondition">
            <el-radio :value="0"><el-tag type="success" size="small">正常</el-tag></el-radio>
            <el-radio :value="1"><el-tag type="warning" size="small">轻微损伤</el-tag></el-radio>
            <el-radio :value="2"><el-tag type="danger"  size="small">需维修</el-tag></el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="如有异常请填写说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="returnVisible = false">取 消</el-button>
        <el-button type="primary" :loading="saving" @click="submitReturn">确认还车</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="还车详情" width="480px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="车牌号">{{ currentRow?.vehicleNo }}</el-descriptions-item>
        <el-descriptions-item label="车辆信息">
          {{ currentRow?.vehicleType }}{{ currentRow?.brand ? ' · ' + currentRow.brand : '' }}
        </el-descriptions-item>
        <el-descriptions-item label="驾驶员">{{ currentRow?.driverName || '—' }}</el-descriptions-item>
        <el-descriptions-item label="目的地">{{ currentRow?.endLocation || '—' }}</el-descriptions-item>
        <el-descriptions-item label="计划还车">{{ currentRow?.planEndTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="实际还车">{{ currentRow?.actualEndTime || '—' }}</el-descriptions-item>
        <el-descriptions-item label="出发里程">{{ currentRow?.mileageBefore ?? 0 }} km</el-descriptions-item>
        <el-descriptions-item label="归还里程">{{ currentRow?.mileageAfter ?? 0 }} km</el-descriptions-item>
        <el-descriptions-item label="本次行驶">{{ (currentRow?.mileageAfter ?? 0) - (currentRow?.mileageBefore ?? 0) }} km</el-descriptions-item>
        <el-descriptions-item label="油量状态">
          <el-tag :type="fuelType(currentRow?.fuelLevel)" size="small">{{ fuelLabel(currentRow?.fuelLevel) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="车辆状况">
          <el-tag :type="conditionType(currentRow?.vehicleCondition)" size="small">{{ conditionLabel(currentRow?.vehicleCondition) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentRow?.remark || '—' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, Delete, View, Van } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReturnList, doReturn, deleteReturn } from '@/api/vehicleReturn'

const loading = ref(false)
const saving  = ref(false)
const list    = ref([])
const total   = ref(0)
const query   = reactive({ vehicleNo: '', driverName: '', returnStatus: null, pageNum: 1, pageSize: 10 })

const returnVisible = ref(false)
const detailVisible = ref(false)
const currentRow    = ref(null)
const formRef       = ref()

const form = reactive({
  id: null,
  mileageBefore: 0,
  mileageAfter: 0,
  fuelLevel: 0,
  vehicleCondition: 0,
  remark: ''
})

const rules = {
  mileageAfter: [{ required: true, message: '请填写归还里程' }],
  fuelLevel:    [{ required: true, message: '请选择油量状态' }],
  vehicleCondition: [{ required: true, message: '请选择车辆状况' }]
}

const fuelLabel     = (v) => ['充足', '偏少', '需加油'][v ?? 0] ?? '充足'
const fuelType      = (v) => ['success', 'warning', 'danger'][v ?? 0] ?? 'success'
const conditionLabel = (v) => ['正常', '轻微损伤', '需维修'][v ?? 0] ?? '正常'
const conditionType  = (v) => ['success', 'warning', 'danger'][v ?? 0] ?? 'success'
const returnStatusLabel = (v) => ({ 0: '待还车', 1: '已还车', 2: '已取消' }[v] ?? '待还车')
const returnStatusType  = (v) => ({ 0: 'warning', 1: 'success', 2: 'info' }[v] ?? 'warning')

async function loadList() {
  loading.value = true
  try {
    const res = await getReturnList(query)
    list.value  = res?.records ?? []
    total.value = res?.total   ?? 0
  } finally { loading.value = false }
}

function resetQuery() {
  Object.assign(query, { vehicleNo: '', driverName: '', returnStatus: null, pageNum: 1 })
  loadList()
}

function openReturn(row) {
  currentRow.value = row
  Object.assign(form, {
    id: row.id,
    mileageBefore: row.mileageBefore ?? 0,
    mileageAfter:  row.mileageBefore ?? 0,
    fuelLevel: 0,
    vehicleCondition: 0,
    remark: ''
  })
  returnVisible.value = true
}

function openDetail(row) {
  currentRow.value = row
  detailVisible.value = true
}

async function submitReturn() {
  await formRef.value.validate()
  saving.value = true
  try {
    await doReturn(form)
    ElMessage.success('还车成功，车辆已恢复空闲状态')
    returnVisible.value = false
    loadList()
  } finally { saving.value = false }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该还车记录？', '确认删除', { type: 'warning' })
  await deleteReturn(row.id)
  ElMessage.success('删除成功')
  loadList()
}

onMounted(loadList)
</script>

<style scoped>
.pagination { margin-top: 16px; justify-content: flex-end; }
.text-muted  { color: #c0c4cc; }
.form-tip    { font-size: 12px; color: #909399; margin-top: 4px; }
</style>
