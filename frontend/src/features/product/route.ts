import type { RouteRecordRaw } from 'vue-router'

const productRoutes: RouteRecordRaw[] = [
  {
    path: '/products',
    component: () => import('@/layouts/DefaultLayout.vue'),
    children: [
      {
        path: 'create',
        name: 'ProductCreate',
        component: () => import('./views/ProductCreateView.vue'),
        meta: { title: '商品上架' },
      },
      {
        path: '',
        name: 'ProductList',
        component: () => import('./views/ProductListView.vue'),
        meta: { title: '商品列表' },
      },
      {
        path: ':id',
        name: 'ProductDetail',
        component: () => import('./views/ProductDetailView.vue'),
        meta: { title: '商品详情' },
      },
    ],
  },
]

export default productRoutes
