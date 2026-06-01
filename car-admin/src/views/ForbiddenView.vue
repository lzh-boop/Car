<template>
  <div class="forbidden-page">
    <div class="card">
      <div class="icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <circle cx="12" cy="12" r="10" />
          <path d="M4.93 4.93l14.14 14.14" />
        </svg>
      </div>
      <h1>403</h1>
      <p class="title">无访问权限</p>
      <p class="desc">您没有权限访问该页面，请联系管理员获取相应权限。</p>
      <div class="actions">
        <el-button type="primary" @click="router.back()">返回上一页</el-button>
        <el-button @click="router.replace('/')">回到首页</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<style scoped>
.forbidden-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-page, #f1f5f9);
}

.card {
  text-align: center;
  padding: 56px 48px;
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0,0,0,.08);
  max-width: 420px;
  width: 100%;
}

.icon {
  width: 72px;
  height: 72px;
  margin: 0 auto 24px;
  color: #ef4444;
}

.icon svg {
  width: 100%;
  height: 100%;
}

h1 {
  font-size: 56px;
  font-weight: 800;
  color: #ef4444;
  margin: 0 0 8px;
  line-height: 1;
}

.title {
  font-size: 20px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 12px;
}

.desc {
  font-size: 14px;
  color: #64748b;
  margin: 0 0 32px;
  line-height: 1.6;
}

.actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}
</style>
