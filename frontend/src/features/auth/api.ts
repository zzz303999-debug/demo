import request from '@/shared/api/request'


interface LoginRequest {
  username: string
  password: string
}

interface LoginResponse {
  token: string
  userId: string
  userName: string
}

interface RegisterRequest {
  username: string
  password: string
  email: string
  phone: string
  address: string
}

interface RegisterResponse {
  id: number
  username: string
  email: string
  phone: string
  address: string
  createdAt: string
}

interface ResetPasswordRequest {
  token: string
  newPassword: string
}


/** POST /api/auth/login — 登录 */
export async function login(data: LoginRequest): Promise<LoginResponse> {
  const res = await request<LoginResponse>({
    url: '/auth/login',
    method: 'post',
    data,
  })
  return res.data
}

/** POST /api/customers — 注册 */
export async function register(data: RegisterRequest): Promise<RegisterResponse> {
  const res = await request<RegisterResponse>({
    url: '/customers',
    method: 'post',
    data,
  })
  return res.data
}

/** POST /api/auth/forgot-password — 忘记密码 */
export async function forgotPassword(data: { email: string }): Promise<void> {
  const res = await request<void>({
    url: '/auth/forgot-password',
    method: 'post',
    data,
  })
  return res.data
}

/** POST /api/auth/reset-password — 重置密码 */
export async function resetPassword(data: ResetPasswordRequest): Promise<void> {
  const res = await request<void>({
    url: '/auth/reset-password',
    method: 'post',
    data,
  })
  return res.data
}

/** POST /api/auth/logout — 登出 */
export async function logout(): Promise<void> {
  const res = await request<void>({
    url: '/auth/logout',
    method: 'post',
  })
  return res.data
}

export type { LoginRequest, LoginResponse, RegisterRequest, RegisterResponse, ResetPasswordRequest }
