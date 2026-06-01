<template>
  <div class="layout-root">

    <!-- ══ Sidebar ══ -->
    <aside class="sidebar" :class="{ collapsed }">

      <!-- Logo -->
      <div class="sidebar-logo" @click="router.push('/dashboard')">
        <div class="logo-mark">
          <el-icon :size="15"><Van /></el-icon>
        </div>
        <transition name="label-fade">
          <div v-if="!collapsed" class="logo-words">
            <span class="logo-title">智慧车辆</span>
            <span class="logo-sub">管理平台 v2</span>
          </div>
        </transition>
      </div>

      <!-- Nav -->
      <nav class="sidebar-nav">

        <a class="nav-item" :class="{ active: activeMenu === '/dashboard' }" @click="navigate('/dashboard')">
          <span class="nav-icon"><el-icon><HomeFilled /></el-icon></span>
          <transition name="label-fade"><span v-if="!collapsed" class="nav-label">首页概览</span></transition>
          <span v-if="activeMenu === '/dashboard'" class="active-bar"></span>
        </a>

        <!-- 用车流程（所有角色可见） -->
        <transition name="label-fade">
          <div v-if="!collapsed" class="nav-section-label">用车流程</div>
        </transition>

        <div class="nav-group" :class="{ open: openGroups.flow || collapsed }">
          <div v-if="!collapsed" class="nav-group-trigger" @click="toggleGroup('flow')">
            <span class="nav-icon"><el-icon><Tickets /></el-icon></span>
            <span class="nav-label">流程管理</span>
            <el-icon class="chevron" :class="{ rotated: openGroups.flow }"><ArrowRight /></el-icon>
          </div>
          <div class="nav-sub">
            <!-- 用车申请：所有角色可见 -->
            <a class="nav-sub-item" :class="{ active: activeMenu === '/apply' }" @click="navigate('/apply')">
              <span class="sub-pip"></span>
              <transition name="label-fade"><span v-if="!collapsed">用车申请</span></transition>
              <el-icon v-if="collapsed" :size="14"><Document /></el-icon>
            </a>
            <!-- 审批管理：仅 ADMIN -->
            <a v-if="isAdmin" class="nav-sub-item" :class="{ active: activeMenu === '/approve' }" @click="navigate('/approve')">
              <span class="sub-pip"></span>
              <transition name="label-fade"><span v-if="!collapsed">审批管理</span></transition>
              <el-icon v-if="collapsed" :size="14"><Stamp /></el-icon>
            </a>
            <!-- 调度管理：仅 ADMIN -->
            <a v-if="isAdmin" class="nav-sub-item" :class="{ active: activeMenu === '/dispatch' }" @click="navigate('/dispatch')">
              <span class="sub-pip"></span>
              <transition name="label-fade"><span v-if="!collapsed">调度管理</span></transition>
              <el-icon v-if="collapsed" :size="14"><Operation /></el-icon>
            </a>
            <!-- 还车管理：所有角色可见 -->
            <a class="nav-sub-item" :class="{ active: activeMenu === '/return' }" @click="navigate('/return')">
              <span class="sub-pip"></span>
              <transition name="label-fade"><span v-if="!collapsed">还车管理</span></transition>
              <el-icon v-if="collapsed" :size="14"><CircleCheck /></el-icon>
            </a>
          </div>
        </div>

        <!-- 车辆档案：仅 ADMIN 可见 -->
        <template v-if="isAdmin">
          <transition name="label-fade">
            <div v-if="!collapsed" class="nav-section-label">车辆管理</div>
          </transition>

          <div class="nav-group" :class="{ open: openGroups.vehicle || collapsed }">
            <div v-if="!collapsed" class="nav-group-trigger" @click="toggleGroup('vehicle')">
              <span class="nav-icon"><el-icon><Van /></el-icon></span>
              <span class="nav-label">车辆档案</span>
              <el-icon class="chevron" :class="{ rotated: openGroups.vehicle }"><ArrowRight /></el-icon>
            </div>
            <div class="nav-sub">
              <a class="nav-sub-item" :class="{ active: activeMenu === '/vehicle' }" @click="navigate('/vehicle')">
                <span class="sub-pip"></span>
                <transition name="label-fade"><span v-if="!collapsed">车辆信息</span></transition>
                <el-icon v-if="collapsed" :size="14"><List /></el-icon>
              </a>
              <a class="nav-sub-item" :class="{ active: activeMenu === '/vehicle/maintain' }" @click="navigate('/vehicle/maintain')">
                <span class="sub-pip"></span>
                <transition name="label-fade"><span v-if="!collapsed">保养维修</span></transition>
                <el-icon v-if="collapsed" :size="14"><Tools /></el-icon>
              </a>
              <a class="nav-sub-item" :class="{ active: activeMenu === '/vehicle/stats' }" @click="navigate('/vehicle/stats')">
                <span class="sub-pip"></span>
                <transition name="label-fade"><span v-if="!collapsed">统计报表</span></transition>
                <el-icon v-if="collapsed" :size="14"><DataAnalysis /></el-icon>
              </a>
              <a class="nav-sub-item" :class="{ active: activeMenu === '/driver' }" @click="navigate('/driver')">
                <span class="sub-pip"></span>
                <transition name="label-fade"><span v-if="!collapsed">驾驶员</span></transition>
                <el-icon v-if="collapsed" :size="14"><User /></el-icon>
              </a>
            </div>
          </div>
        </template>

        <!-- 监控·系统 -->
        <transition name="label-fade">
          <div v-if="!collapsed" class="nav-section-label">监控 · 系统</div>
        </transition>

        <!-- 北斗定位：仅 ADMIN 可见 -->
        <a v-if="isAdmin" class="nav-item" :class="{ active: activeMenu === '/location' }" @click="navigate('/location')">
          <span class="nav-icon"><el-icon><Position /></el-icon></span>
          <transition name="label-fade"><span v-if="!collapsed" class="nav-label">北斗定位</span></transition>
          <span v-if="activeMenu === '/location'" class="active-bar"></span>
        </a>
        <!-- 系统管理：仅 ADMIN 可见 -->
        <a v-if="isAdmin" class="nav-item" :class="{ active: activeMenu === '/system/user' }" @click="navigate('/system/user')">
          <span class="nav-icon"><el-icon><Setting /></el-icon></span>
          <transition name="label-fade"><span v-if="!collapsed" class="nav-label">系统管理</span></transition>
          <span v-if="activeMenu === '/system/user'" class="active-bar"></span>
        </a>

      </nav>

      <!-- Collapse -->
      <button class="collapse-btn" @click="collapsed = !collapsed" :title="collapsed ? '展开' : '收起'">
        <el-icon :size="13"><Expand v-if="collapsed" /><Fold v-else /></el-icon>
        <transition name="label-fade"><span v-if="!collapsed" class="collapse-label">收起侧栏</span></transition>
      </button>
    </aside>

    <!-- ══ Main ══ -->
    <div class="main-area">

      <!-- Topbar -->
      <header class="topbar">
        <div class="topbar-left">
          <!-- Breadcrumb path -->
          <div class="topbar-path">
            <span class="path-icon"><el-icon :size="12"><component :is="currentIcon" /></el-icon></span>
            <span class="path-title">{{ currentTitle }}</span>
          </div>
        </div>

        <div class="topbar-right">
          <div class="topbar-time">{{ timeStr }}</div>

          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-chip">
              <div class="user-av">{{ userInitial }}</div>
              <span class="user-name">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
              <el-icon class="user-arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item divided command="logout" class="logout-item">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- Content -->
      <main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import {
  Van, HomeFilled, Operation, Document, Position, Setting,
  Expand, Fold, ArrowDown, ArrowRight, User, SwitchButton, Stamp,
  Tickets, CircleCheck, List, Tools, DataAnalysis
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router    = useRouter()
const route     = useRoute()
const userStore = useUserStore()

// 是否为管理员角色：优先读 sessionStorage 里登录时服务端写入的真实角色，防止篡改 localStorage 伪造
const isAdmin = computed(() => {
  const sessionRole = sessionStorage.getItem('_sr')
  return (sessionRole || userStore.role || '').toUpperCase() === 'ADMIN'
})

const collapsed  = ref(false)
const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta?.title || '概览')

const iconMap = {
  '/dashboard':        HomeFilled,
  '/apply':            Document,
  '/approve':          Stamp,
  '/dispatch':         Operation,
  '/return':           CircleCheck,
  '/vehicle':          Van,
  '/vehicle/maintain': Tools,
  '/vehicle/stats':    DataAnalysis,
  '/driver':           User,
  '/location':         Position,
  '/system/user':      Setting,
}
const currentIcon = computed(() => iconMap[activeMenu.value] || HomeFilled)

const openGroups = reactive({ flow: true, vehicle: true })

const userInitial = computed(() => {
  const name = userStore.userInfo?.realName || userStore.userInfo?.username || ''
  return name.charAt(0).toUpperCase()
})

const timeStr = ref('')
let timer = null
function updateTime() {
  const d = new Date()
  timeStr.value = `${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}
onMounted(() => { updateTime(); timer = setInterval(updateTime, 10000) })
onUnmounted(() => clearInterval(timer))

function toggleGroup(key) {
  if (collapsed.value) return
  openGroups[key] = !openGroups[key]
}
function navigate(path) { router.push(path) }

watch(activeMenu, (path) => {
  if (['/apply','/approve','/dispatch','/return'].includes(path)) openGroups.flow = true
  if (['/vehicle','/vehicle/maintain','/vehicle/stats','/driver'].includes(path)) openGroups.vehicle = true
}, { immediate: true })

onMounted(async () => {
  if (!userStore.userInfo) {
    try { await userStore.fetchUserInfo() } catch (_) {}
  }
})

async function handleCommand(cmd) {
  if (cmd === 'logout') {
    await ElMessageBox.confirm('确认退出登录？', '退出确认', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await userStore.logout()
    ElMessage.success('已安全退出')
  }
}
</script>

<style scoped>
/* ── Root ── */
.layout-root {
  height: 100vh;
  display: flex;
  overflow: hidden;
  background: var(--bg-base);
}

/* ══════════════════════════════════════
   Sidebar
   ══════════════════════════════════════ */
.sidebar {
  width: var(--sidebar-width);
  background: var(--navy-950);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  transition: width .24s cubic-bezier(.4,0,.2,1);
  overflow: hidden;
  position: relative;
  z-index: 100;
  border-right: 1px solid rgba(255,255,255,.04);
}
.sidebar.collapsed { width: 60px; }

/* Logo */
.sidebar-logo {
  height: var(--header-height);
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 14px;
  cursor: pointer;
  border-bottom: 1px solid rgba(255,255,255,.05);
  flex-shrink: 0;
  white-space: nowrap;
  overflow: hidden;
}
.logo-mark {
  width: 32px;
  height: 32px;
  background: #2563eb;
  border: 1px solid #3b82f6;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  flex-shrink: 0;
}
.logo-words { display: flex; flex-direction: column; gap: 1px; }
.logo-title {
  font-size: 13.5px;
  font-weight: 700;
  color: #e8edf5;
  letter-spacing: .2px;
  line-height: 1;
  font-family: 'DM Sans', sans-serif;
}
.logo-sub {
  font-size: 10px;
  color: #3d5070;
  letter-spacing: .4px;
  font-weight: 500;
  line-height: 1;
}

/* Nav */
.sidebar-nav {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 10px 0 4px;
}
.sidebar-nav::-webkit-scrollbar { width: 0; }

/* Section label */
.nav-section-label {
  padding: 14px 14px 5px;
  font-size: 9.5px;
  font-weight: 700;
  letter-spacing: .12em;
  text-transform: uppercase;
  color: #2a3a52;
  white-space: nowrap;
}

/* Nav item */
.nav-item {
  display: flex;
  align-items: center;
  gap: 9px;
  height: 36px;
  padding: 0 10px;
  margin: 1px 6px;
  cursor: pointer;
  color: #4a6080;
  font-size: 13px;
  font-weight: 500;
  border-radius: 7px;
  white-space: nowrap;
  user-select: none;
  transition: color .15s, background .15s;
  position: relative;
  overflow: hidden;
}
.nav-item:hover {
  background: rgba(255,255,255,.04);
  color: #8aa4c8;
}
.nav-item.active {
  background: rgba(59,130,246,.22);
  color: #ffffff;
}

/* Active left bar — slides in from left */
.active-bar {
  position: absolute;
  left: 0;
  top: 6px;
  bottom: 6px;
  width: 3px;
  background: #60a5fa;
  border-radius: 0 2px 2px 0;
  animation: slideInBar .2s cubic-bezier(.34,1.56,.64,1) both;
}
@keyframes slideInBar {
  from { transform: scaleY(0); opacity: 0; }
  to   { transform: scaleY(1); opacity: 1; }
}

/* Icon */
.nav-icon {
  width: 26px;
  height: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  flex-shrink: 0;
  font-size: 14px;
  color: inherit;
}
.nav-item.active .nav-icon { color: #93c5fd; }
.nav-label { flex: 1; }

/* Group trigger */
.nav-group-trigger {
  display: flex;
  align-items: center;
  gap: 9px;
  height: 36px;
  padding: 0 10px;
  margin: 1px 6px;
  cursor: pointer;
  color: #4a6080;
  font-size: 13px;
  font-weight: 500;
  border-radius: 7px;
  white-space: nowrap;
  user-select: none;
  transition: color .15s, background .15s;
}
.nav-group-trigger:hover {
  background: rgba(255,255,255,.04);
  color: #8aa4c8;
}
.chevron {
  margin-left: auto;
  font-size: 10px;
  color: #2a3a52;
  transition: transform .2s cubic-bezier(.4,0,.2,1);
  flex-shrink: 0;
}
.chevron.rotated { transform: rotate(90deg); }

/* Sub */
.nav-group:not(.open) .nav-sub { display: none; }
.nav-sub { padding: 2px 0; }

.nav-sub-item {
  display: flex;
  align-items: center;
  gap: 8px;
  height: 33px;
  padding: 0 10px 0 46px;
  margin: 1px 6px;
  cursor: pointer;
  color: #3a5070;
  font-size: 12.5px;
  font-weight: 400;
  border-radius: 6px;
  white-space: nowrap;
  user-select: none;
  transition: color .15s, background .15s;
}
.sidebar.collapsed .nav-sub-item {
  padding: 0;
  justify-content: center;
  height: 36px;
}
.nav-sub-item:hover {
  background: rgba(255,255,255,.04);
  color: #8aa4c8;
}
.nav-sub-item.active {
  color: #bfdbfe;
  background: rgba(59,130,246,.18);
  font-weight: 600;
}
.sub-pip {
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: #334155;
  flex-shrink: 0;
  transition: background .15s, box-shadow .15s;
}
.nav-sub-item.active .sub-pip {
  background: #60a5fa;
  box-shadow: 0 0 5px rgba(96,165,250,.7);
}

/* Collapse btn */
.collapse-btn {
  height: 44px;
  border: none;
  background: transparent;
  border-top: 1px solid rgba(255,255,255,.04);
  color: #2a3a52;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  transition: color .15s, background .15s;
  flex-shrink: 0;
  padding: 0 14px;
  white-space: nowrap;
  overflow: hidden;
}
.collapse-btn:hover {
  color: #4a6080;
  background: rgba(255,255,255,.03);
}
.collapse-label {
  font-size: 11px;
  font-weight: 500;
  letter-spacing: .04em;
}

/* ══════════════════════════════════════
   Main area
   ══════════════════════════════════════ */
.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

/* Topbar */
.topbar {
  height: var(--header-height);
  background: var(--header-bg);
  border-bottom: 1px solid var(--header-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
  z-index: 50;
}

.topbar-left { display: flex; align-items: center; gap: 10px; }

/* Path badge */
.topbar-path {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}
.path-icon {
  width: 22px;
  height: 22px;
  border-radius: 5px;
  background: var(--accent);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
}
.path-title { letter-spacing: -.01em; }

.topbar-right { display: flex; align-items: center; gap: 12px; }

.topbar-time {
  font-size: 12.5px;
  font-weight: 600;
  color: var(--text-disabled);
  font-variant-numeric: tabular-nums;
  letter-spacing: .06em;
}

/* User chip */
.user-chip {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 3px 9px 3px 3px;
  border-radius: 20px;
  border: 1px solid var(--border);
  background: var(--bg-surface);
  cursor: pointer;
  transition: border-color .15s, box-shadow .15s, background .15s;
}
.user-chip:hover {
  border-color: var(--accent);
  background: var(--navy-100);
  box-shadow: 0 0 0 3px rgba(37,99,235,.1);
}
.user-av {
  width: 26px;
  height: 26px;
  border-radius: 50%;
  background: var(--accent);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
  flex-shrink: 0;
}
.user-name {
  font-size: 12.5px;
  color: var(--text-primary);
  font-weight: 500;
}
.user-arrow {
  font-size: 9px;
  color: var(--text-disabled);
}

/* Content */
.main-content {
  flex: 1;
  overflow-y: auto;
  padding: 22px 26px;
  background: var(--bg-base);
}

/* Transitions */
.label-fade-enter-active,
.label-fade-leave-active { transition: opacity .15s; }
.label-fade-enter-from,
.label-fade-leave-to { opacity: 0; }

:deep(.logout-item) { color: var(--red) !important; }
:deep(.logout-item:hover) { background: var(--red-bg) !important; }
</style>
