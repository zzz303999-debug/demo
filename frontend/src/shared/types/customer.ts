/** 客户信息（响应） */
export interface Customer {
  id: number
  username: string
  email: string
  phone: string
  address: string
  createdAt: string
}

/** 注册表单 */
export interface RegisterForm {
  username: string
  password: string
  email?: string
  phone?: string
  address?: string
}

/** 修改信息表单（所有字段可选） */
export interface UpdateCustomerForm {
  email?: string
  phone?: string
  address?: string
}
