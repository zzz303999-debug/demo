<script setup lang="ts">
import { reactive, ref } from 'vue'
import { Form, Input, Button, Card, message, Result } from 'ant-design-vue'
import { MailOutlined } from '@ant-design/icons-vue'
import { forgotPassword } from '../api'
import type { FormInstance } from 'ant-design-vue/es/form'
import type { RuleObject } from 'ant-design-vue/es/form/interface'

const formRef = ref<FormInstance>()
const formState = reactive({
  email: '',
})
const submitted = ref(false)
const submitting = ref(false)

const rules: Record<string, RuleObject[]> = {
  email: [
    { required: true, message: '请输入注册邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  submitting.value = true
  try {
    await forgotPassword({ email: formState.email })
    submitted.value = true
    message.success('重置密码邮件已发送')
  } catch {
    // 错误由拦截器处理
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="forgot-container">
    <Card title="忘记密码" :bordered="false" style="width: 400px">
      <Result
        v-if="submitted"
        status="success"
        title="邮件已发送"
        sub-title="请检查邮箱并点击链接重置密码"
      >
        <template #extra>
          <Button type="primary" @click="$router.push({ name: 'Login' })">
            返回登录
          </Button>
        </template>
      </Result>

      <Form
        v-else
        ref="formRef"
        :model="formState"
        :rules="rules"
        layout="vertical"
        @finish="handleSubmit"
      >
        <Form.Item label="注册邮箱" name="email">
          <Input
            v-model:value="formState.email"
            placeholder="请输入注册时使用的邮箱"
            size="large"
          >
            <template #prefix>
              <MailOutlined />
            </template>
          </Input>
        </Form.Item>

        <Form.Item>
          <Button
            type="primary"
            html-type="submit"
            :loading="submitting"
            block
            size="large"
          >
            发送重置邮件
          </Button>
        </Form.Item>

        <div style="text-align: center">
          <router-link :to="{ name: 'Login' }">返回登录</router-link>
        </div>
      </Form>
    </Card>
  </div>
</template>

<style scoped>
.forgot-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f0f2f5;
}
</style>
