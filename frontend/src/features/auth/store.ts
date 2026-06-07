import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, register as registerApi, logout as logoutApi } from './api'
import type { LoginRequest, RegisterRequest } from './api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(localStorage.getItem('token'))
  const userId = ref<string | null>(null)
  const userName = ref<string | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  /** 登录 */
  async function login(form: LoginRequest) {
    loading.value = true
    error.value = null
    try {
      const res = await loginApi(form)
      token.value = res.token
      userId.value = res.userId
      userName.value = res.userName
      localStorage.setItem('token', res.token)
      return res
    } catch (e: any) {
      error.value = e.response?.data?.message ?? '登录失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  /** 注册 */
  async function register(form: RegisterRequest) {
    loading.value = true
    error.value = null
    try {
      return await registerApi(form)
    } catch (e: any) {
      error.value = e.response?.data?.message ?? '注册失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  /** 登出 */
  async function logout() {
    try {
      await logoutApi()
    } finally {
      token.value = null
      userId.value = null
      userName.value = null
      localStorage.removeItem('token')
    }
  }

  const isLoggedIn = () => !!token.value

  return { token, userId, userName, loading, error, login, register, logout, isLoggedIn }
})
