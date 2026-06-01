/**
 * 用户认证状态管理（Pinia Store）
 *
 * 安全说明：
 *   Token 存储在 localStorage（同源 JS 可读）。
 *   采用集中式 getToken() / _clearCredentials() 模式，
 *   后续若需切换为 sessionStorage 或 HttpOnly Cookie 只需修改此文件。
 */
import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi, getUserInfo } from '@/api/auth'
// 不直接 import router，避免与 router/index.js 循环依赖
// 需要跳转时通过动态 import 获取

const TOKEN_KEY     = 'token'
const USER_INFO_KEY = 'userInfo'
// sessionStorage 中存储服务端返回的真实角色，页面刷新后重新由登录接口写入
// 用户无法通过修改 localStorage 来影响此值
const SESSION_ROLE_KEY = '_sr'

export const useUserStore = defineStore('user', {
  state: () => ({
    token:    localStorage.getItem(TOKEN_KEY) || '',
    userInfo: (() => {
      try {
        return JSON.parse(localStorage.getItem(USER_INFO_KEY) || 'null')
      } catch {
        return null
      }
    })()
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    /**
     * 权威角色字符串：'ADMIN' 或 'USER'（大写，无 ROLE_ 前缀）
     * 路由守卫和菜单权限判断以此为准
     */
    role: (state) => state.userInfo?.role || 'USER'
  },

  actions: {
    async login(credentials) {
      const data = await loginApi(credentials)

      this.token = data.token
      localStorage.setItem(TOKEN_KEY, data.token)

      // 登录时将角色信息缓存到本地，供路由守卫使用，无需额外请求 /me 接口。
      // 服务端始终是权威来源，此处缓存仅用于前端 UI 决策。
      this.userInfo = {
        username: data.username,
        realName: data.realName,
        avatar:   data.avatar,
        role:     data.role || 'USER'
      }
      localStorage.setItem(USER_INFO_KEY, JSON.stringify(this.userInfo))
      // 将服务端返回的真实角色写入 sessionStorage，防止用户篡改 localStorage 伪造角色
      sessionStorage.setItem(SESSION_ROLE_KEY, data.role || 'USER')

      return data
    },

    async fetchUserInfo() {
      const data = await getUserInfo()
      this.userInfo = data
      localStorage.setItem(USER_INFO_KEY, JSON.stringify(data))
      return data
    },

    /**
     * 主动登出：清除所有本地凭据并跳转至登录页
     */
    async logout() {
      try { await logoutApi() } catch (_) { /* 忽略服务端登出接口失败，继续清理本地状态 */ }
      this._clearCredentials()
      // 动态导入避免与 router/index.js 循环依赖
      const { default: router } = await import('@/router')
      router.push('/login')
    },

    /**
     * 被动登出：由 request.js 的 401 响应拦截器调用
     * 同时清理 Token 和用户信息，确保状态一致
     */
    _clearCredentials() {
      this.token    = ''
      this.userInfo = null
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_INFO_KEY)
      sessionStorage.removeItem(SESSION_ROLE_KEY)
    }
  }
})
