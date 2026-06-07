<script setup lang="ts">
import { h, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Layout, Menu, Dropdown, Avatar, theme } from 'ant-design-vue'
import {
  PlusOutlined,
  UnorderedListOutlined,
  UserOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  AppstoreOutlined,
  IdcardOutlined,
} from '@ant-design/icons-vue'
import { useAuthStore } from '@/features/auth/store'
import type { MenuProps } from 'ant-design-vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const { token: themeToken } = theme.useToken()

const collapsed = ref(false)
const selectedKeys = ref<string[]>([route.path])

// 进入详情页时高亮父级菜单
watch(() => route.path, (path) => {
  if (/^\/products\/\d+$/.test(path)) {
    selectedKeys.value = ['/products']
  } else if (/^\/customers\/\d+$/.test(path)) {
    selectedKeys.value = [`/customers/${authStore.userId}`]
  } else {
    selectedKeys.value = [path]
  }
}, { immediate: true })

// 导航菜单
function buildMenuItems() {
  return [
    {
      key: '/products',
      icon: () => h(AppstoreOutlined),
      label: '商品管理',
      children: [
        { key: '/products/create', icon: () => h(PlusOutlined), label: '商品上架' },
        { key: '/products', icon: () => h(UnorderedListOutlined), label: '商品列表' },
      ],
    },
    {
      key: `/customers/${authStore.userId ?? 0}`,
      icon: () => h(IdcardOutlined),
      label: '客户管理',
    },
  ]
}

const menuItems = ref(buildMenuItems())
watch(() => authStore.userId, () => {
  menuItems.value = buildMenuItems()
})

function handleMenuClick({ key }: { key: string }) {
  selectedKeys.value = [key]
  router.push(key)
}

// 个人中心下拉
const userDropdownItems: MenuProps['items'] = [
  {
    key: 'logout',
    icon: () => h(LogoutOutlined),
    label: '退出登录',
  },
]

function handleUserMenuClick({ key }: { key: string }) {
  if (key === 'logout') {
    authStore.logout()
    router.push('/auth/login')
  }
}
</script>

<template>
  <Layout style="min-height: 100vh">
    <!-- 左侧导航 -->
    <Layout.Sider
      v-model:collapsed="collapsed"
      collapsible
      breakpoint="lg"
      :trigger="null"
      :width="220"
      :style="{ background: themeToken.colorBgContainer }"
    >
      <div class="logo">
        <span v-show="!collapsed" class="logo-text">网上书店管理系统</span>
      </div>

      <Menu
        :selectedKeys="selectedKeys"
        mode="inline"
        :items="menuItems"
        @click="handleMenuClick"
      />
    </Layout.Sider>

    <Layout>
      <!-- 顶部栏 -->
      <Layout.Header
        :style="{
          background: themeToken.colorBgContainer,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          padding: '0 24px',
        }"
      >
        <component
          :is="collapsed ? MenuUnfoldOutlined : MenuFoldOutlined"
          class="trigger"
          @click="() => (collapsed = !collapsed)"
        />

        <!-- 个人中心 -->
        <Dropdown :trigger="['click']">
          <div class="user-area">
            <Avatar size="small">
              <template #icon>
                <UserOutlined />
              </template>
            </Avatar>
            <span class="user-name">{{ authStore.userName || '管理员' }}</span>
          </div>
          <template #overlay>
            <Menu :items="userDropdownItems" @click="handleUserMenuClick" />
          </template>
        </Dropdown>
      </Layout.Header>

      <!-- 内容区 -->
      <Layout.Content
        :style="{
          margin: '16px',
          padding: '24px',
          background: themeToken.colorBgContainer,
          borderRadius: '8px',
          minHeight: '280px',
          overflow: 'auto',
        }"
      >
        <RouterView />
      </Layout.Content>
    </Layout>
  </Layout>
</template>

<style scoped>
.logo {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 16px;
  border-bottom: 1px solid var(--ant-color-border-secondary);
}

.logo-text {
  white-space: nowrap;
  font-size: 15px;
  font-weight: 600;
  color: var(--ant-color-text);
}

.trigger {
  font-size: 18px;
  cursor: pointer;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.user-name {
  font-size: 14px;
}
</style>
