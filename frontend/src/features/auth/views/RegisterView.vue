<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Form, Input, Button, Card, message } from 'ant-design-vue'
import { UserOutlined, LockOutlined, MailOutlined, PhoneOutlined, HomeOutlined } from '@ant-design/icons-vue'
import { useAuthStore } from '../store'
import type { RegisterRequest } from '../api'
import type { RuleObject } from 'ant-design-vue/es/form/interface'
import type { FormInstance } from 'ant-design-vue/es/form'

const router = useRouter()
const authStore = useAuthStore()

const formRef = ref<FormInstance>()
const formState = reactive<RegisterRequest>({
  username: '',
  password: '',
  email: '',
  phone: '',
  address: '',
})

const rules: Record<string, RuleObject[]> = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度 3-50 字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度 6-100 字符', trigger: 'blur' },
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
}

const submitting = ref(false)

async function handleSubmit() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    const customer = await authStore.register({ ...formState })
    message.success(`注册成功！欢迎 ${customer?.username}`)
    router.push({ name: 'CustomerDetail', params: { id: customer?.id } })
  } catch (e: any) {
    if (e.response?.status === 409) {
      message.warning(e.response.data.message)
    }
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="register-container">
    <Card title="客户注册" :bordered="false" style="width: 480px">
      <Form
        ref="formRef"
        :model="formState"
        :rules="rules"
        layout="vertical"
        @finish="handleSubmit"
      >
        <Form.Item label="用户名" name="username">
          <Input
            v-model:value="formState.username"
            placeholder="请输入用户名（3-50 字符）"
          >
            <template #prefix>
              <UserOutlined />
            </template>
          </Input>
        </Form.Item>

        <Form.Item label="密码" name="password">
          <Input.Password
            v-model:value="formState.password"
            placeholder="请输入密码（至少 6 位）"
          >
            <template #prefix>
              <LockOutlined />
            </template>
          </Input.Password>
        </Form.Item>

        <Form.Item label="邮箱" name="email">
          <Input
            v-model:value="formState.email"
            placeholder="请输入邮箱（选填）"
          >
            <template #prefix>
              <MailOutlined />
            </template>
          </Input>
        </Form.Item>

        <Form.Item label="手机号" name="phone">
          <Input
            v-model:value="formState.phone"
            placeholder="请输入手机号（选填）"
          >
            <template #prefix>
              <PhoneOutlined />
            </template>
          </Input>
        </Form.Item>

        <Form.Item label="地址" name="address">
          <Input
            v-model:value="formState.address"
            placeholder="请输入地址（选填）"
          >
            <template #prefix>
              <HomeOutlined />
            </template>
          </Input>
        </Form.Item>

        <Form.Item>
          <Button
            type="primary"
            html-type="submit"
            :loading="submitting"
            block
          >
            注册
          </Button>
        </Form.Item>

        <div style="text-align: center">
          已有账号？<router-link :to="{ name: 'Login' }">去登录</router-link>
        </div>
      </Form>
    </Card>
  </div>
</template>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f0f2f5;
}
</style>
