import type { RouteRecordRaw } from 'vue-router'

const customerRoutes: RouteRecordRaw[] = [
  {
    path: '/customer/:id',
    name: 'CustomerDetail',
    component: () => import('./views/CustomerDetailView.vue'),
    meta: { title: '客户信息' },
  },
]

export default customerRoutes
