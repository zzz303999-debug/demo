import type { OrderStatus } from './api'

/** 订单明细项（响应） */
export interface OrderItemResponse {
  id: number
  productId: number
  quantity: number
  unitPrice: number
  subtotal: number
}

/** 订单（响应） */
export interface Order {
  id: number
  orderNumber: string
  customerId: number
  totalAmount: number
  status: OrderStatus
  items: OrderItemResponse[]
  createdAt: string
}

/** 下单请求中的明细项 */
export interface OrderItemRequest {
  productId: number
  quantity: number
}

/** 下单表单 */
export interface CreateOrderForm {
  customerId: number
  items: OrderItemRequest[]
}
