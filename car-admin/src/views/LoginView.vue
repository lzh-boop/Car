<template>
  <div class="login-root">

    <!-- Left: Brand panel -->
    <div class="brand-panel">
      <!-- Subtle dot-grid texture -->
      <div class="dot-grid"></div>
      <!-- Gradient glow -->
      <div class="glow-top"></div>
      <div class="glow-bottom"></div>

      <div class="brand-body">
        <!-- Logo mark -->
        <div class="brand-mark">
          <el-icon :size="18"><Van /></el-icon>
        </div>

        <!-- Headline -->
        <div class="brand-headline">
          <h1 class="brand-name">智慧车辆<br>管理平台</h1>
          <p class="brand-en">Vehicle Intelligence System</p>
        </div>

        <!-- Divider -->
        <div class="brand-divider"></div>

        <!-- Feature list -->
        <ul class="feature-list">
          <li v-for="f in features" :key="f.text">
            <span class="feat-dot"></span>
            <span>{{ f.text }}</span>
          </li>
        </ul>
      </div>

      <div class="brand-foot">
        <span class="foot-copy">© 2025 智慧车辆管理平台</span>
        <span class="foot-ver">v2.0</span>
      </div>
    </div>

    <!-- Right: Form panel -->
    <div class="form-panel">
      <div class="form-wrap">

        <!-- Header -->
        <div class="form-head">
          <p class="form-eyebrow">管理员登录</p>
          <h2 class="form-title">欢迎回来</h2>
          <p class="form-sub">请输入您的账号信息以继续</p>
        </div>

        <!-- Form -->
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          class="login-form"
          @submit.prevent="handleLogin"
        >
          <div class="field">
            <label class="field-label">用户名</label>
            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                placeholder="admin"
                :prefix-icon="User"
                clearable
              />
            </el-form-item>
          </div>

          <div class="field">
            <label class="field-label">密码</label>
            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="••••••••"
                :prefix-icon="Lock"
                show-password
                @keyup.enter="handleLogin"
              />
            </el-form-item>
          </div>

          <button
            class="submit-btn"
            :class="{ loading }"
            @click.prevent="handleLogin"
            :disabled="loading"
          >
            <span v-if="!loading" class="btn-inner">
              登录
              <el-icon :size="14"><Right /></el-icon>
            </span>
            <span v-else class="btn-inner">
              <span class="spin-ring"></span>
              登录中
            </span>
          </button>
        </el-form>

        <!-- Hint -->
        <div class="form-hint">
          <el-icon :size="11"><InfoFilled /></el-icon>
          默认账号&nbsp;<strong>admin</strong>&nbsp;/&nbsp;<strong>admin123</strong>
        </div>

      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { User, Lock, Van, Right, InfoFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

const router    = useRouter()
const userStore = useUserStore()
const formRef   = ref()
const loading   = ref(false)
const form      = reactive({ username: '', password: '' })

const features = [
  { text: '车辆全生命周期档案管理' },
  { text: '用车申请与多级审批流程' },
  { text: '智能调度与驾驶员分配' },
  { text: '实时北斗卫星定位追踪' },
  { text: '保养维修记录与统计报表' },
]

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (_) {
    // handled in interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* ── Root ── */
.login-root {
  min-height: 100vh;
  display: flex;
  font-family: 'DM Sans', 'Noto Sans SC', 'PingFang SC', sans-serif;
}

/* ══════════════════════════════════════
   Brand panel (left)
   ══════════════════════════════════════ */
.brand-panel {
  flex: 0 0 44%;
  background: var(--navy-950);
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* Dot grid */
.dot-grid {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(rgba(59,111,212,.18) 1px, transparent 1px);
  background-size: 28px 28px;
  pointer-events: none;
}

/* Glows */
.glow-top {
  position: absolute;
  top: -100px;
  left: -60px;
  width: 360px;
  height: 360px;
  background: radial-gradient(circle, rgba(37,99,235,.45) 0%, transparent 65%);
  pointer-events: none;
}
.glow-bottom {
  position: absolute;
  bottom: -80px;
  right: -60px;
  width: 280px;
  height: 280px;
  background: radial-gradient(circle, rgba(96,165,250,.3) 0%, transparent 65%);
  pointer-events: none;
}

/* Body */
.brand-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 60px 52px;
  position: relative;
  z-index: 2;
}

/* Mark */
.brand-mark {
  width: 44px;
  height: 44px;
  background: #2563eb;
  border: 1px solid #3b82f6;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  margin-bottom: 36px;
}

/* Headline */
.brand-name {
  font-size: 38px;
  font-weight: 800;
  color: #e8edf5;
  line-height: 1.2;
  letter-spacing: -.03em;
  margin-bottom: 10px;
  font-family: 'DM Sans', sans-serif;
}
.brand-en {
  font-size: 11px;
  color: #2a3a52;
  letter-spacing: .2em;
  font-weight: 500;
  text-transform: uppercase;
}

/* Divider */
.brand-divider {
  width: 36px;
  height: 2px;
  background: #3b82f6;
  border-radius: 2px;
  margin: 32px 0;
}

/* Features */
.feature-list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.feature-list li {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: #3d5070;
  font-weight: 400;
  line-height: 1.4;
}
.feat-dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: #3b82f6;
  flex-shrink: 0;
}

/* Footer */
.brand-foot {
  padding: 20px 52px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  z-index: 2;
  border-top: 1px solid rgba(255,255,255,.04);
}
.foot-copy {
  font-size: 11px;
  color: #1e2d42;
  font-weight: 400;
}
.foot-ver {
  font-size: 10px;
  font-weight: 700;
  color: #1e2d42;
  letter-spacing: .1em;
  background: rgba(255,255,255,.04);
  padding: 2px 8px;
  border-radius: 4px;
  border: 1px solid rgba(255,255,255,.06);
}

/* ══════════════════════════════════════
   Form panel (right)
   ══════════════════════════════════════ */
.form-panel {
  flex: 1;
  background: #f8f7f5;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px 32px;
}

.form-wrap {
  width: 100%;
  max-width: 360px;
}

/* Header */
.form-head { margin-bottom: 40px; }
.form-eyebrow {
  font-size: 10.5px;
  font-weight: 700;
  letter-spacing: .14em;
  text-transform: uppercase;
  color: var(--accent);
  margin-bottom: 10px;
}
.form-title {
  font-size: 28px;
  font-weight: 800;
  color: var(--navy-950);
  letter-spacing: -.03em;
  margin-bottom: 6px;
  font-family: 'DM Sans', sans-serif;
  line-height: 1.1;
}
.form-sub {
  font-size: 13px;
  color: var(--text-tertiary);
  font-weight: 400;
}

/* Form */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field { display: flex; flex-direction: column; gap: 6px; }
.field-label {
  font-size: 11px;
  font-weight: 700;
  color: var(--text-secondary);
  letter-spacing: .06em;
  text-transform: uppercase;
}

/* Input overrides */
.login-form :deep(.el-form-item) { margin-bottom: 10px; }
.login-form :deep(.el-input__wrapper) {
  border-radius: 8px !important;
  box-shadow: 0 0 0 1.5px var(--border) !important;
  background: #ffffff !important;
  height: 42px !important;
  transition: box-shadow .15s !important;
}
.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1.5px var(--navy-300) !important;
}
.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1.5px var(--navy-600), 0 0 0 4px rgba(30,58,95,.1) !important;
}
.login-form :deep(.el-input__inner) {
  font-size: 13.5px !important;
  color: var(--text-primary) !important;
}
.login-form :deep(.el-input__prefix-inner .el-icon) {
  color: var(--text-disabled) !important;
}
.login-form :deep(.el-form-item__error) {
  font-size: 11.5px !important;
  color: var(--red) !important;
  padding-top: 3px !important;
}

/* Submit button — vivid blue, white text */
.submit-btn {
  width: 100%;
  height: 44px;
  margin-top: 12px;
  border: none;
  border-radius: 8px;
  background: var(--accent);
  color: #ffffff;
  font-size: 14px;
  font-weight: 700;
  letter-spacing: .04em;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-family: inherit;
  box-shadow: 0 2px 10px rgba(37,99,235,.4), 0 1px 2px rgba(37,99,235,.2);
  transition: background .15s, transform .12s, box-shadow .15s;
}
.submit-btn:hover:not(:disabled) {
  background: var(--accent-hover);
  box-shadow: 0 6px 20px rgba(37,99,235,.5);
  transform: translateY(-1px);
}
.submit-btn:active:not(:disabled) {
  transform: scale(.97);
  box-shadow: 0 1px 4px rgba(13,21,38,.2);
}
.submit-btn:disabled { opacity: .65; cursor: not-allowed; }
.btn-inner {
  display: flex;
  align-items: center;
  gap: 7px;
}

/* Spinner */
.spin-ring {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255,255,255,.25);
  border-top-color: #e8edf5;
  border-radius: 50%;
  animation: spin .65s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* Hint */
.form-hint {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-top: 22px;
  font-size: 11.5px;
  color: var(--text-disabled);
  justify-content: center;
}
.form-hint strong { color: var(--text-tertiary); font-weight: 600; }

/* Mobile */
@media (max-width: 768px) {
  .login-root { flex-direction: column; }
  .brand-panel { flex: 0 0 auto; min-height: 260px; }
  .brand-name  { font-size: 28px; }
  .feature-list { display: none; }
  .brand-body { padding: 40px 32px; }
  .brand-foot { padding: 16px 32px; }
}
</style>
