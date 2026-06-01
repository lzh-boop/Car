<template>
  <div>
    <div class="page-header">
      <span class="page-title">驾驶员管理</span>
      <el-button type="primary" :icon="Plus" @click="openAdd">新增驾驶员</el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="query.driverName" placeholder="姓名" clearable style="width:140px" />
      <el-input v-model="query.phone" placeholder="手机号" clearable style="width:150px" />
      <el-select v-model="query.status" placeholder="状态" clearable style="width:120px">
        <el-option label="在职" :value="1" />
        <el-option label="离职" :value="0" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="loadList">搜索</el-button>
      <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
    </div>

    <div class="page-card">
      <el-table :data="list" v-loading="loading" stripe border style="width:100%">
        <el-table-column prop="id"          label="ID"    width="80" />
        <el-table-column prop="driverName"  label="姓名"  width="100" />
        <el-table-column prop="phone"       label="手机号" width="130" />
        <el-table-column prop="licenseType" label="驾照类型" width="100" align="center" />
        <el-table-column prop="licenseDate" label="领证日期" width="120" />
        <el-table-column prop="status"       label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '在职' : '离职' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" :icon="Edit"   size="small" @click="openEdit(row)">编辑</el-button>
            <el-button text type="danger"  :icon="Delete" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pagination" background layout="total, prev, pager, next"
        :total="total" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" @change="loadList" />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑驾驶员' : '新增驾驶员'" width="500px" draggable>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="姓名" prop="driverName">
              <el-input v-model="form.driverName" placeholder="真实姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="驾照类型">
              <el-select v-model="form.licenseType" style="width:100%">
                <el-option label="A1" value="A1" />
                <el-option label="A2" value="A2" />
                <el-option label="B1" value="B1" />
                <el-option label="B2" value="B2" />
                <el-option label="C1" value="C1" />
                <el-option label="C2" value="C2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="领证日期">
              <el-date-picker v-model="form.licenseDate" type="date" style="width:100%"
                value-format="YYYY-MM-DD" placeholder="选择日期" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" style="width:100%">
                <el-option label="在职" :value="1" />
                <el-option label="离职" :value="0" />
              </el-select>
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
import { ref, reactive, onMounted } from 'vue'
import { Plus, Search, Refresh, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDriverList, addDriver, updateDriver, deleteDriver } from '@/api/driver'

const loading = ref(false), saving = ref(false)
const list = ref([]), total = ref(0)
const query = reactive({ driverName: '', phone: '', status: null, pageNum: 1, pageSize: 10 })
const dialogVisible = ref(false), isEdit = ref(false)
const formRef = ref()
const form = reactive({ id: null, driverName: '', phone: '', licenseType: 'C1', licenseDate: '', status: 1, remark: '' })
const rules = {
  driverName: [{ required: true, message: '请输入姓名' }],
  phone:      [{ required: true, message: '请输入手机号' }]
}

async function loadList() {
  loading.value = true
  try {
    const res = await getDriverList(query)
    list.value  = res?.records ?? []
    total.value = res?.total   ?? 0
  } finally { loading.value = false }
}
function resetQuery() {
  Object.assign(query, { driverName: '', phone: '', status: null, pageNum: 1 })
  loadList()
}
function openAdd() {
  isEdit.value = false
  Object.assign(form, { id: null, driverName: '', phone: '', licenseType: 'C1', licenseDate: '', status: 1, remark: '' })
  dialogVisible.value = true
}
function openEdit(row) { isEdit.value = true; Object.assign(form, row); dialogVisible.value = true }
async function handleSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (isEdit.value) { await updateDriver(form); ElMessage.success('更新成功') }
    else              { await addDriver(form); ElMessage.success('新增成功') }
    dialogVisible.value = false; loadList()
  } finally { saving.value = false }
}
async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除驾驶员「${row.driverName}」？`, '确认删除', { type: 'warning' })
  await deleteDriver(row.id)
  ElMessage.success('删除成功'); loadList()
}
onMounted(loadList)
</script>

<style scoped>
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
