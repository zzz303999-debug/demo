<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Card, Descriptions, Form, Input, Button, Space, Spin, Empty, message } from 'ant-design-vue'
import { EditOutlined, CheckOutlined, CloseOutlined, ArrowLeftOutlined } from '@ant-design/icons-vue'
import { useCustomerStore } from '../store'
import { formatDateTime } from '@/shared/utils/format'
import type { RuleObject } from 'ant-design-vue/es/form/interface'
import type { FormInstance } from 'ant-design-vue/es/form'

const route = useRoute()
const router = useRouter()
const customerStore = useCustomerStore()

const loading = ref(true)
const editing = ref(false)
const formRef = ref<FormInstance>()

const formState = reactive({
  email: '',
  phone: '',
  address: '',
})

const rules: Record<string, RuleObject[]> = {
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
}

onMounted(async () => {
  const id = Number(route.params.id)
  const customer = await customerStore.fetchCustomer(id)
  if (customer) {
    formState.email = customer.email || ''
    formState.phone = customer.phone || ''
    formState.address = customer.address || ''
  }
  loading.value = false
})

function startEdit() {
  editing.value = true
}

function cancelEdit() {
  const c = customerStore.currentCustomer
  if (c) {
    formState.email = c.email || ''
    formState.phone = c.phone || ''
    formState.address = c.address || ''
  }
  editing.value = false
}

async function handleSave() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  const id = Number(route.params.id)
  try {
    await customerStore.updateProfile(id, {
      email: formState.email || undefined,
      phone: formState.phone || undefined,
      address: formState.address || undefined,
    })
    message.success('修改成功')
    editing.value = false
  } catch (e: any) {
    if (e.response?.status === 409) {
      message.warning(e.response.data.message)
    }
  }
}
</script>

<template>
  <div>
    <div class="page-header">
      <Space>
        <Button @click="router.push('/products')">
          <ArrowLeftOutlined />
          返回
        </Button>
        <h2>客户信息</h2>
      </Space>
      <Space v-if="!editing">
        <Button type="primary" @click="startEdit">
          <EditOutlined />
          修改信息
        </Button>
      </Space>
    </div>

    <Spin :spinning="loading">
      <Card v-if="customerStore.currentCustomer" style="max-width: 720px">
        <Descriptions :column="1" bordered>
          <Descriptions.Item label="ID">{{ customerStore.currentCustomer.id }}</Descriptions.Item>
          <Descriptions.Item label="用户名">{{ customerStore.currentCustomer.username }}</Descriptions.Item>

          <!-- 邮箱 -->
          <Descriptions.Item label="邮箱">
            <template v-if="editing">
              <Form ref="formRef" :model="formState" :rules="rules">
                <Form.Item name="email" style="margin: 0">
                  <Input v-model:value="formState.email" placeholder="请输入邮箱" />
                </Form.Item>
              </Form>
            </template>
            <template v-else>
              {{ customerStore.currentCustomer.email || '-' }}
            </template>
          </Descriptions.Item>

          <!-- 手机号 -->
          <Descriptions.Item label="手机号">
            <template v-if="editing">
              <Input v-model:value="formState.phone" placeholder="请输入手机号" />
            </template>
            <template v-else>
              {{ customerStore.currentCustomer.phone || '-' }}
            </template>
          </Descriptions.Item>

          <!-- 地址 -->
          <Descriptions.Item label="地址">
            <template v-if="editing">
              <Input v-model:value="formState.address" placeholder="请输入地址" />
            </template>
            <template v-else>
              {{ customerStore.currentCustomer.address || '-' }}
            </template>
          </Descriptions.Item>

          <Descriptions.Item label="注册时间">
            {{ formatDateTime(customerStore.currentCustomer.createdAt) }}
          </Descriptions.Item>
        </Descriptions>

        <div v-if="editing" style="margin-top: 16px; display: flex; gap: 8px">
          <Button type="primary" @click="handleSave" :loading="customerStore.loading">
            <CheckOutlined />
            保存
          </Button>
          <Button @click="cancelEdit">
            <CloseOutlined />
            取消
          </Button>
        </div>
      </Card>

      <Empty v-else-if="!loading" description="未找到客户信息" />
    </Spin>
  </div>
</template>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-header h2 {
  margin: 0;
}
</style>
