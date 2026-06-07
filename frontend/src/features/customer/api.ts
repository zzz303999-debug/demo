import request from '@/shared/api/request'
import type { Customer, UpdateCustomerForm } from '@/shared/types/customer'

export const customerApi = {
  /** GET /api/customers/{id} — 查询客户信息 */
  getById(id: number) {
    return request.get<Customer>(`/customers/${id}`)
  },

  /** PUT /api/customers/{id} — 修改客户信息 */
  update(id: number, data: UpdateCustomerForm) {
    return request.put<Customer>(`/customers/${id}`, data)
  },
}
