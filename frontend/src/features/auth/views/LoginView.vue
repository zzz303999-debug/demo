<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Form, Input, Button, Card, message } from 'ant-design-vue'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import { useAuthStore } from '../store'
import type { LoginRequest } from '../api'
import type { FormInstance } from 'ant-design-vue/es/form'
import type { RuleObject } from 'ant-design-vue/es/form/interface'

const router = useRouter()
const authStore = useAuthStore()

const formRef = ref<FormInstance>()
const formState = reactive<LoginRequest>({
  username: '',
  password: '',
})

const rules: Record<string, RuleObject[]> = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
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
    await authStore.login({ ...formState })
    message.success(`欢迎回来，${authStore.userName}`)
    router.push('/')
  } catch {
    // 错误由拦截器处理
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="login-container">
    <Card title="网上书店 · 登录" :bordered="false" style="width: 400px">
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
            placeholder="请输入用户名"
            size="large"
          >
            <template #prefix>
              <UserOutlined />
            </template>
          </Input>
        </Form.Item>

        <Form.Item label="密码" name="password">
          <Input.Password
            v-model:value="formState.password"
            placeholder="请输入密码"
            size="large"
          >
            <template #prefix>
              <LockOutlined />
            </template>
          </Input.Password>
        </Form.Item>

        <Form.Item>
          <Button
            type="primary"
            html-type="submit"
            :loading="submitting"
            block
            size="large"
          >
            登 录
          </Button>
        </Form.Item>

        <div class="form-footer">
          <router-link :to="{ name: 'Register' }">注册账号</router-link>
          <router-link :to="{ name: 'ForgotPassword' }">忘记密码</router-link>
        </div>
      </Form>
    </Card>
  </div>
</template>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f0f2f5;
}

.form-footer {
  display: flex;
  justify-content: space-between;
}
</style>
