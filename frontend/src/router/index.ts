import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import authRoutes from '@/features/auth/route'
import customerRoutes from '@/features/customer/route'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    redirect: '/auth/login',
  },
  ...authRoutes,
  ...customerRoutes,
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from) => {
  console.log(`[router] ${from.path} → ${to.path}`)
})

export default router
