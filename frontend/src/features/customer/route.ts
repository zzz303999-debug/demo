import type { RouteRecordRaw } from 'vue-router'

const customerRoutes: RouteRecordRaw[] = [
  {
    path: '/customers',
    component: () => import('@/layouts/DefaultLayout.vue'),
    children: [
      {
        path: ':id',
        name: 'CustomerDetail',
        component: () => import('./views/CustomerDetailView.vue'),
        meta: { title: '客户信息' },
      },
    ],
  },
]

export default customerRoutes
