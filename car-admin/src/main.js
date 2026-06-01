import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import { ElMessage, ElNotification } from 'element-plus'
import { useUserStore } from '@/stores/user'

import App from './App.vue'
import router from './router'
import './assets/styles/global.css'

const app = createApp(App)
const pinia = createPinia()

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 必须先 use(pinia)，路由守卫里的 useUserStore() 才能正常工作
app.use(pinia)
app.use(router)
app.use(ElementPlus, { locale: zhCn })

// ── 路由守卫：在 app.use(pinia) 之后注册，确保 store 可用 ──────────────────
router.beforeEach((to) => {
  document.title = `${to.meta.title || '智能车队'} - 智能车队管理平台`

  const token = localStorage.getItem('token')

  // 1. 未登录：public 页面放行，其余跳登录页
  if (!to.meta.public && !token) {
    ElMessage.warning('请先登录')
    return '/login'
  }

  // 2. 已登录不允许再访问登录页
  if (to.path === '/login' && token) {
    return '/'
  }

  // 3. 角色权限校验：遍历所有匹配层级，任意一层有 roles 限制就校验
  const restrictedRoutes = to.matched.filter(
    r => r.meta.roles && r.meta.roles.length > 0
  )

  if (token && restrictedRoutes.length > 0) {
    // 优先读 sessionStorage 里登录时由服务端写入的真实角色
    // sessionStorage 无法通过修改 localStorage 来篡改，防止伪造身份绕过路由守卫
    const sessionRole = sessionStorage.getItem('_sr')
    const userRole = (sessionRole || useUserStore().role || '').toUpperCase()

    const denied = restrictedRoutes.some(
      r => !r.meta.roles.some(required => userRole === required.toUpperCase())
    )

    if (denied) {
      ElNotification({
        title: '权限不足',
        message: `[${to.meta.title}] 需要相应权限才能访问`,
        type: 'error',
        duration: 3000
      })
      return '/403'
    }
  }
})
// ─────────────────────────────────────────────────────────────────────────────

app.mount('#app')
