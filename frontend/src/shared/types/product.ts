/** 商品信息（响应） */
export interface Product {
  id: number
  name: string
  description: string
  price: number
  stock: number
  category: string
  author: string
  isbn: string
  createdAt: string
}

/** 上架表单 */
export interface CreateProductForm {
  name: string
  description?: string
  price: number
  stock?: number
  category?: string
  author?: string
  isbn?: string
}

/** 库存变更表单 */
export interface UpdateStockForm {
  quantity: number // > 0 入库，< 0 出库
}
