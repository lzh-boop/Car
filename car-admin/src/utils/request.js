/**
 * Axios 请求封装
 *
 * Token 存储：localStorage（同源 JS 可读）。
 * XSS 防护：在服务端/Nginx 层面配置严格的 CSP 策略。
 * 改进建议：未来可迁移至 HttpOnly Cookie，从根本上消除 JS 可读 Token 的 XSS 风险。
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const TOKEN_KEY     = 'token'
const USER_INFO_KEY = 'userInfo'

/**
 * 读取本地存储的 Token
 */
function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

/**
 * 清除所有本地凭据（Token + 用户信息），与 stores/user.js 保持一致
 */
function clearCredentials() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_INFO_KEY)
}

// 生产环境读取 .env.production 中的 VITE_API_BASE_URL；
// 开发环境为空（由 vite.config.js 中的 proxy 转发 /api 到 localhost:8080）
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000
})

// 请求拦截器：自动附加 Bearer Token
request.interceptors.request.use(config => {
  const token = getToken()
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`
  }
  return config
})

// 防止重复触发跳转的标志位
let isRedirecting = false

// 响应拦截器：统一处理业务错误和 HTTP 状态码
request.interceptors.response.use(
  res => {
    const { code, message, data } = res.data
    if (code === 200) return data
    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message))
  },
  err => {
    const status     = err.response?.status
    const isLoginUrl = err.config?.url?.includes('/api/auth/login')

    if (status === 401) {
      if (isLoginUrl) {
        // 登录接口本身返回 401 — 用户名或密码错误，仅弹提示不跳转
        const msg = err.response?.data?.message || '用户名或密码错误'
        ElMessage.error(msg)
      } else if (!isRedirecting) {
        // Token 过期或无效 — 清除凭据并跳转登录页，避免重复触发
        isRedirecting = true
        clearCredentials()
        ElMessage.error('登录已过期，请重新登录')
        // replace 不留 history，防止用户点返回又回到需要登录的页面
        setTimeout(() => {
          router.replace('/login')
          isRedirecting = false
        }, 1200)
      }
    } else if (status === 403) {
      // silent403: true 时静默忽略（如首页统计卡片，普通用户无权但不应跳转）
      if (err.config?._silent403) {
        // 静默忽略，不弹提示不跳转
      } else if (!isRedirecting) {
        isRedirecting = true
        ElMessage.error('权限不足，无法访问该页面')
        // replace 替换当前 history 记录为 /403，
        // 这样用户点"返回上一页"回到的是进入该页面之前的页面，而不是被拦截的页面
        router.replace('/403').finally(() => {
          isRedirecting = false
        })
      }
    } else if (status === 500) {
      const msg = err.response?.data?.message || '服务器内部错误'
      ElMessage.error(msg)
    } else if (status) {
      ElMessage.error(`请求失败（${status}）`)
    } else {
      ElMessage.error(err.message || '网络异常，请检查网络连接')
    }
    return Promise.reject(err)
  }
)

export default request
