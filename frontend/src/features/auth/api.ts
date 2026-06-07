import request from '@/shared/api/request'

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  id: number
  username: string
  email: string
  phone: string
  address: string
  createdAt: string
}

export interface RegisterRequest {
  username: string
  password: string
  email?: string
  phone?: string
  address?: string
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

/** POST /api/auth/register — 注册（返回 token，注册即登录） */
export async function register(data: RegisterRequest): Promise<LoginResponse> {
  const res = await request<LoginResponse>({
    url: '/auth/register',
    method: 'post',
    data,
  })
  return res.data
}
