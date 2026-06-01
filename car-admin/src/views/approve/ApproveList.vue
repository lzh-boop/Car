<template>
  <div>
    <div class="page-header">
      <span class="page-title">审批管理</span>
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
        <el-table-column prop="id"            label="申请ID"  width="90" />
        <el-table-column prop="applyUserName" label="申请人"   width="100" />
        <el-table-column prop="vehicleNo"     label="车牌号"   width="120" />
        <el-table-column prop="purpose"       label="用车目的" width="110" show-overflow-tooltip />
        <el-table-column prop="endLocation"   label="目的地"   show-overflow-tooltip />
        <el-table-column prop="planStartTime" label="计划出发" width="160" />
        <el-table-column prop="planEndTime"   label="计划到达" width="160" />
        <el-table-column prop="applyTime"     label="申请时间" width="160" />
        <el-table-column prop="applyStatus"   label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row.applyStatus)" size="small">{{ statusLabel(row.applyStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button v-if="Number(row.applyStatus) === 0" text type="primary" size="small" @click="openApprove(row)">审批</el-button>
            <el-tag v-else-if="Number(row.applyStatus) === 1" type="success" size="small">已通过</el-tag>
            <el-tag v-else-if="Number(row.applyStatus) === 2" type="danger"  size="small">不通过</el-tag>
            <el-tag v-else                                    type="info"    size="small">已取消</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination class="pagination" background layout="total, prev, pager, next"
        :total="total" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" @change="loadList" />
    </div>

    <!-- 审批弹窗 -->
    <el-dialog v-model="approveVisible" title="审批申请" width="440px" draggable>
      <el-descriptions :column="1" border class="approve-info">
        <el-descriptions-item label="申请人">{{ currentRow?.applyUserName }}</el-descriptions-item>
        <el-descriptions-item label="车牌号">{{ currentRow?.vehicleNo }}</el-descriptions-item>
        <el-descriptions-item label="用车目的">{{ currentRow?.purpose }}</el-descriptions-item>
        <el-descriptions-item label="目的地">{{ currentRow?.endLocation }}</el-descriptions-item>
        <el-descriptions-item label="计划出发">{{ currentRow?.planStartTime }}</el-descriptions-item>
        <el-descriptions-item label="计划到达">{{ currentRow?.planEndTime }}</el-descriptions-item>
      </el-descriptions>
      <el-form label-width="80px" style="margin-top:20px">
        <el-form-item label="审批结果">
          <el-radio-group v-model="approveResult">
            <el-radio :value="1">
              <el-tag type="success" size="small">通 过</el-tag>
            </el-radio>
            <el-radio :value="2">
              <el-tag type="danger" size="small">拒 绝</el-tag>
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审批意见">
          <el-input v-model="approveRemark" type="textarea" :rows="3" placeholder="请填写审批意见（选填）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approveVisible = false">取 消</el-button>
        <el-button type="primary" :loading="saving" @click="submitApprove">提 交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getAdminApplyList, approveApply } from '@/api/apply'

const loading = ref(false), saving = ref(false)
const list = ref([]), total = ref(0)
const query = reactive({ applyUserName: '', vehicleNo: '', applyStatus: null, pageNum: 1, pageSize: 10 })

const approveVisible = ref(false)
const approveResult  = ref(1)
const approveRemark  = ref('')
const currentRow     = ref(null)

const statusLabel = (s) => ['待审批', '已通过', '已拒绝', '已取消'][s] ?? '未知'
const statusType  = (s) => ['warning', 'success', 'danger', 'info'][s] ?? ''

async function loadList() {
  loading.value = true
  try {
    const res = await getAdminApplyList(query)
    list.value  = res?.records ?? []
    total.value = res?.total   ?? 0
  } finally { loading.value = false }
}

function resetQuery() {
  Object.assign(query, { applyUserName: '', vehicleNo: '', applyStatus: null, pageNum: 1 })
  loadList()
}

function openApprove(row) {
  currentRow.value    = row
  approveResult.value = 1
  approveRemark.value = ''
  approveVisible.value = true
}

async function submitApprove() {
  saving.value = true
  try {
    await approveApply({
      id:     currentRow.value.id,
      status: approveResult.value,
      remark: approveRemark.value
    })
    // 立即更新本地行状态，无需等待网络
    const row = list.value.find(r => r.id === currentRow.value.id)
    if (row) row.applyStatus = approveResult.value
    ElMessage.success(approveResult.value === 1 ? '已通过该申请' : '已拒绝该申请')
    approveVisible.value = false
    // 同步刷新列表，保证页面数据与后端一致
    loadList()
  } finally {
    saving.value = false
  }
}

onMounted(loadList)
</script>

<style scoped>
.pagination { margin-top: 16px; justify-content: flex-end; }
.approve-info { margin-bottom: 4px; }
</style>
