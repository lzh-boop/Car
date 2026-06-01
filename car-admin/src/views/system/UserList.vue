<template>
  <div>
    <div class="page-header">
      <span class="page-title">用户管理</span>
      <el-button type="primary" :icon="Plus" @click="openAdd">新增用户</el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="query.username" placeholder="用户名" clearable style="width:140px" />
      <el-input v-model="query.realName" placeholder="真实姓名" clearable style="width:140px" />
      <el-input v-model="query.phone"    placeholder="手机号" clearable style="width:150px" />
      <el-select v-model="query.status"  placeholder="状态" clearable style="width:120px">
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="loadList">搜索</el-button>
      <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
    </div>

    <div class="page-card">
      <el-table :data="list" v-loading="loading" stripe border style="width:100%">
        <el-table-column prop="id"       label="ID"    width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        <el-table-column prop="phone"    label="手机号" width="130" />
        <el-table-column prop="email"    label="邮箱" show-overflow-tooltip />
        <el-table-column prop="deptName" label="部门" width="120" />
        <el-table-column prop="status"   label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="(val) => handleStatusChange(row, val)"
              :disabled="row.username === 'admin'"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button text type="primary" :icon="Edit" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button text type="warning" :icon="Key"  size="small" @click="openReset(row)">重置密码</el-button>
            <el-button
              v-if="row.username !== 'admin'"
              text type="danger" :icon="Delete" size="small"
              @click="handleDelete(row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pagination" background layout="total, sizes, prev, pager, next"
        :total="total" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
        :page-sizes="[10,20,50]" @change="loadList" />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="480px" draggable>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="form.realName" />
            </el-form-item>
          </el-col>
          <el-col v-if="!isEdit" :span="12">
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" type="password" show-password />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" style="width:100%">
                <el-option label="启用" :value="1" />
                <el-option label="禁用" :value="0" />
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

    <!-- 重置密码 -->
    <el-dialog v-model="resetVisible" title="重置密码" width="380px">
      <el-form ref="resetFormRef" :model="resetForm" label-width="90px">
        <el-form-item label="新密码" prop="newPassword"
          :rules="[{ required: true, min: 6, message: '密码至少6位' }]">
          <el-input v-model="resetForm.newPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetVisible = false">取 消</el-button>
        <el-button type="primary" :loading="saving" @click="submitReset">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus, Search, Refresh, Edit, Delete, Key } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, addUser, updateUser, deleteUser, resetPassword, changeStatus } from '@/api/user'

const loading = ref(false), saving = ref(false)
const list = ref([]), total = ref(0)
const query = reactive({ username: '', realName: '', phone: '', status: null, pageNum: 1, pageSize: 10 })

const dialogVisible = ref(false), isEdit = ref(false)
const formRef = ref()
const form = reactive({ id: null, username: '', realName: '', password: '', phone: '', email: '', status: 1 })
const rules = {
  username: [{ required: true, message: '请输入用户名' }],
  realName: [{ required: true, message: '请输入真实姓名' }],
  password: [{ required: true, min: 6, message: '密码至少6位' }],
  phone:    [{ required: true, message: '请输入手机号' }]
}

const resetVisible = ref(false)
const resetFormRef = ref()
const resetForm    = reactive({ userId: null, newPassword: '' })

async function loadList() {
  loading.value = true
  try {
    const res = await getUserList(query)
    list.value  = res?.records ?? []
    total.value = res?.total   ?? 0
  } finally { loading.value = false }
}
function resetQuery() {
  Object.assign(query, { username: '', realName: '', phone: '', status: null, pageNum: 1 })
  loadList()
}
function openAdd() {
  isEdit.value = false
  Object.assign(form, { id: null, username: '', realName: '', password: '', phone: '', email: '', status: 1 })
  dialogVisible.value = true
}
function openEdit(row) {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}
async function handleSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (isEdit.value) { await updateUser(form); ElMessage.success('更新成功') }
    else              { await addUser(form);    ElMessage.success('新增成功') }
    dialogVisible.value = false; loadList()
  } finally { saving.value = false }
}
async function handleStatusChange(row, val) {
  await changeStatus(row.id, val ? 1 : 0)
  row.status = val ? 1 : 0
  ElMessage.success(`已${val ? '启用' : '禁用'}`)
}
function openReset(row) {
  resetForm.userId = row.id
  resetForm.newPassword = ''
  resetVisible.value = true
}
async function submitReset() {
  await resetFormRef.value.validate()
  saving.value = true
  try {
    await resetPassword(resetForm.userId, resetForm.newPassword)
    ElMessage.success('密码已重置')
    resetVisible.value = false
  } finally { saving.value = false }
}
async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除用户「${row.username}」？`, '确认删除', { type: 'warning' })
  await deleteUser(row.id)
  ElMessage.success('删除成功'); loadList()
}
onMounted(loadList)
</script>

<style scoped>
.pagination { margin-top: 16px; justify-content: flex-end; }
</style>
