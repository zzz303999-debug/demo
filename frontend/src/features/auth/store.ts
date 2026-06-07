import { defineStore } from 'pinia'
import { ref, watch } from 'vue'
import { login as loginApi, register as registerApi } from './api'
import type { LoginRequest, RegisterRequest } from './api'

function loadStored(key: string): string | null {
  try {
    return localStorage.getItem(key)
  } catch {
    return null
  }
}

function loadStoredNumber(key: string): number | null {
  const v = loadStored(key)
  return v ? Number(v) : null
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(loadStored('token'))
  const userId = ref<number | null>(loadStoredNumber('userId'))
  const userName = ref<string | null>(loadStored('userName'))
  const loading = ref(false)
  const error = ref<string | null>(null)

  // 持久化到 localStorage
  watch(token, (v) => {
    if (v) localStorage.setItem('token', v)
    else localStorage.removeItem('token')
  })
  watch(userId, (v) => {
    if (v != null) localStorage.setItem('userId', String(v))
    else localStorage.removeItem('userId')
  })
  watch(userName, (v) => {
    if (v) localStorage.setItem('userName', v)
    else localStorage.removeItem('userName')
  })

  /** 登录 */
  async function login(form: LoginRequest) {
    loading.value = true
    error.value = null
    try {
      const res = await loginApi(form)
      token.value = res.token
      userId.value = res.id
      userName.value = res.username
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
      const res = await registerApi(form)
      token.value = res.token
      userId.value = res.id
      userName.value = res.username
      return res
    } catch (e: any) {
      error.value = e.response?.data?.message ?? '注册失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  /** 登出 */
  function logout() {
    token.value = null
    userId.value = null
    userName.value = null
  }

  const isLoggedIn = () => !!token.value

  return { token, userId, userName, loading, error, login, register, logout, isLoggedIn }
})
