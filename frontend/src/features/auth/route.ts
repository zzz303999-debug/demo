import type { RouteRecordRaw } from 'vue-router'

const authRoutes: RouteRecordRaw[] = [
  {
    path: '/auth/login',
    name: 'Login',
    component: () => import('./views/LoginView.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/auth/register',
    name: 'Register',
    component: () => import('./views/RegisterView.vue'),
    meta: { title: '注册' },
  },
]

export default authRoutes
