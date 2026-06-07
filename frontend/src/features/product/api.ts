import request from '@/shared/api/request'
import type { Product, CreateProductForm, UpdateStockForm } from '@/shared/types/product'

export const productApi = {
  /** POST /api/products — 上架 */
  create(data: CreateProductForm) {
    return request.post<Product>('/products', data)
  },

  /** GET /api/products/{id} — 详情 */
  getById(id: number) {
    return request.get<Product>(`/products/${id}`)
  },

  /** GET /api/products?name=&category= — 搜索 */
  search(params?: { name?: string; category?: string }) {
    return request.get<Product[]>('/products', { params })
  },

  /** PATCH /api/products/{id}/stock — 库存变更 */
  updateStock(id: number, data: UpdateStockForm) {
    return request.patch<Product>(`/products/${id}/stock`, data)
  },
}
