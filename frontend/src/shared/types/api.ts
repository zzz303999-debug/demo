/** 后端统一错误响应 */
export interface ApiError {
  timestamp: string
  status: number
  error: string
  message: string
}

/** 订单状态枚举 */
export type OrderStatus = 'PENDING' | 'PAID' | 'SHIPPED' | 'CANCELLED'

/** OrderStatus 中文映射 */
export const OrderStatusLabel: Record<OrderStatus, string> = {
  PENDING: '待处理',
  PAID: '已支付',
  SHIPPED: '已发货',
  CANCELLED: '已取消',
}
