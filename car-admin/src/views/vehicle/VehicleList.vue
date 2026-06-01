<template>
  <div>
    <div class="page-header">
      <span class="page-title">车辆管理</span>
      <el-button type="primary" :icon="Plus" @click="openAdd">新增车辆</el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="query.vehicleNo" placeholder="车牌号" clearable style="width:160px" />
      <el-input v-model="query.vehicleType" placeholder="车辆类型" clearable style="width:140px" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width:120px">
        <el-option label="空闲" :value="0" />
        <el-option label="在用" :value="1" />
        <el-option label="维修" :value="2" />
        <el-option label="报废" :value="3" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="loadList">搜索</el-button>
      <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
    </div>

    <!-- 表格 -->
    <div class="page-card">
      <el-table :data="list" v-loading="loading" stripe border row-key="id" style="width:100%">
        <el-table-column prop="vehicleNo"   label="车牌号"  width="120" />
        <el-table-column prop="terminalNo" label="北斗终端号" min-width="130" show-overflow-tooltip>
          <template #default="{ row }">
            <span>{{ row.terminalNo || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="vehicleType" label="车辆类型" width="120" />
        <el-table-column prop="brand"       label="品牌"    width="120" />
        <el-table-column prop="seats"       label="座位数"  width="80" align="center" />
        <el-table-column prop="mileage"     label="里程(km)" width="110" align="right" />
        <el-table-column prop="status"      label="状态"    width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="driverName"  label="当前驾驶员" />
        <el-table-column prop="remark"      label="备注" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button text type="danger"  :icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pagination"
        background
        layout="total, sizes, prev, pager, next"
        :total="total"
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :page-sizes="[10, 20, 50]"
        @change="loadList"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑车辆' : '新增车辆'" width="520px" draggable>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="车牌号" :prop="isEdit ? '' : 'vehicleNo'">
              <el-input v-model="form.vehicleNo" :disabled="isEdit" placeholder="如：京A12345" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="车辆类型" :prop="isEdit ? '' : 'vehicleType'">
              <el-input v-model="form.vehicleType" :disabled="isEdit" placeholder="轿车/SUV/货车" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="品牌" prop="brand">
              <el-input v-model="form.brand" placeholder="品牌" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="北斗终端号" prop="terminalNo">
              <el-input
                v-model="form.terminalNo"
                clearable
                placeholder="与设备上报 JSON 中的 device_id 一致，如 esp32_sim7670x"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="座位数" prop="seats">
              <el-input-number v-model="form.seats" :min="1" :max="60" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-select v-model="form.status" style="width:100%">
                <el-option label="空闲" :value="0" />
                <el-option label="在用" :value="1" />
                <el-option label="维修" :value="2" />
                <el-option label="报废" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="里程(km)" prop="mileage">
              <el-input-number v-model="form.mileage" :min="0" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="2" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted } from 'vue'
import { Plus, Search, Refresh, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getVehicleList, addVehicle, updateVehicle, deleteVehicle } from '@/api/vehicle'

const loading = ref(false)
const saving  = ref(false)
const list    = ref([])
const total   = ref(0)
const query   = reactive({ vehicleNo: '', vehicleType: '', status: null, pageNum: 1, pageSize: 10 })

const dialogVisible = ref(false)
const isEdit  = ref(false)
const formRef = ref()
const form    = reactive({ vehicleNo: '', vehicleType: '', brand: '', terminalNo: '', seats: 5, status: 0, mileage: 0, remark: '' })
const rules = {
  vehicleNo:   [{ required: true, message: '请输入车牌号', trigger: 'blur' }],
  vehicleType: [{ required: true, message: '请输入车辆类型', trigger: 'blur' }]
}

const statusLabel = (s) => ({ 0: '空闲', 1: '在用', 2: '维修', 3: '报废' }[s] ?? '未知')
const statusType  = (s) => ({ 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }[s] ?? '')

async function loadList() {
  loading.value = true
  try {
    const res = await getVehicleList(query)
    list.value  = res?.records ?? []
    total.value = res?.total   ?? 0
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  Object.assign(query, { vehicleNo: '', vehicleType: '', status: null, pageNum: 1 })
  loadList()
}

function openAdd() {
  isEdit.value = false
  Object.assign(form, { vehicleNo: '', vehicleType: '', brand: '', terminalNo: '', seats: 5, status: 0, mileage: 0, remark: '' })
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    vehicleNo:   row.vehicleNo,
    vehicleType: row.vehicleType,
    brand:       row.brand ?? '',
    terminalNo:  row.terminalNo ?? '',
    seats:       row.seats ?? 5,
    status:      row.status ?? 0,
    mileage:     row.mileage ?? 0,
    remark:      row.remark ?? '',
  })
  dialogVisible.value = true
  nextTick(() => formRef.value?.clearValidate())
}

async function handleSave() {
  if (!isEdit.value) {
    await formRef.value.validate()
  }
  saving.value = true
  try {
    if (isEdit.value) {
      await updateVehicle(form)
      ElMessage.success('更新成功')
    } else {
      await addVehicle(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadList()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除车辆「${row.vehicleNo}」？`, '确认删除', { type: 'warning' })
  await deleteVehicle(row.vehicleNo)
  ElMessage.success('删除成功')
  loadList()
}

onMounted(loadList)
</script>

<style scoped>
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
