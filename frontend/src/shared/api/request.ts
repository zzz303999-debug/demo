import axios from 'axios'
import { message } from 'ant-design-vue'
import type { ApiError } from '@/shared/types/api'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器 — 自动携带 token + 日志
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  console.log(`[request] ${config.method?.toUpperCase()} ${config.baseURL}${config.url}`, config.data || '')
  return config
})

// 响应拦截器 — 日志 + 统一错误处理
request.interceptors.response.use(
  (response) => {
    console.log(`[response] ${response.config.method?.toUpperCase()} ${response.config.url}`, response.data)
    return response
  },
  (error) => {
    if (error.response) {
      const err = error.response.data as ApiError
      const status = error.response.status

      // 401 未登录 → 跳转登录页
      if (status === 401) {
        localStorage.removeItem('token')
        window.location.href = '/auth/login'
        return Promise.reject(error)
      }

      // 409 业务冲突交给调用方自行处理
      if (status === 409) {
        return Promise.reject(error)
      }

      const msg = err?.message ?? `请求失败 (${status})`
      message.error(msg)
      return Promise.reject(error)
    }

    message.error('网络异常，请检查连接')
    return Promise.reject(error)
  },
)

export default request
