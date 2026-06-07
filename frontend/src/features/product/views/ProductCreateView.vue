<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Form, Input, InputNumber, Select, Button, Card, message } from 'ant-design-vue'
import { useProductStore } from '../store'
import type { RuleObject } from 'ant-design-vue/es/form/interface'
import type { FormInstance } from 'ant-design-vue/es/form'

const router = useRouter()
const productStore = useProductStore()

const formRef = ref<FormInstance>()
const formState = reactive({
  name: '',
  price: null as number | null,
  stock: 0,
  description: '',
  category: '',
  author: '',
  isbn: '',
})

const rules: Record<string, RuleObject[]> = {
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { max: 200, message: '商品名称最长 200 字符', trigger: 'blur' },
  ],
  price: [
    { required: true, message: '请输入单价', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '单价必须大于 0', trigger: 'blur' },
  ],
  stock: [
    { type: 'number', min: 0, message: '库存不能为负数', trigger: 'blur' },
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
    await productStore.create({
      name: formState.name,
      price: formState.price!,
      stock: formState.stock,
      description: formState.description || undefined,
      category: formState.category || undefined,
      author: formState.author || undefined,
      isbn: formState.isbn || undefined,
    })
    message.success('商品上架成功')
    formRef.value?.resetFields()
    formState.stock = 0
  } catch {
    // 错误由拦截器处理
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div>
    <h2 style="margin-bottom: 24px">商品上架</h2>

    <Card style="max-width: 720px">
      <Form
        ref="formRef"
        :model="formState"
        :rules="rules"
        layout="vertical"
        @finish="handleSubmit"
      >
        <Form.Item label="商品名称" name="name">
          <Input
            v-model:value="formState.name"
            placeholder="请输入商品名称"
          />
        </Form.Item>

        <Form.Item label="单价" name="price">
          <InputNumber
            v-model:value="formState.price"
            :min="0.01"
            :precision="2"
            placeholder="请输入单价"
            addon-after="元"
            style="width: 100%"
          />
        </Form.Item>

        <Form.Item label="初始库存" name="stock">
          <InputNumber
            v-model:value="formState.stock"
            :min="0"
            placeholder="请输入初始库存"
            addon-after="件"
            style="width: 100%"
          />
        </Form.Item>

        <Form.Item label="商品描述" name="description">
          <Input.TextArea
            v-model:value="formState.description"
            placeholder="请输入商品描述（选填）"
            :rows="3"
          />
        </Form.Item>

        <Form.Item label="分类" name="category">
          <Input
            v-model:value="formState.category"
            placeholder="如：计算机、文学、经管（选填）"
          />
        </Form.Item>

        <Form.Item label="作者" name="author">
          <Input
            v-model:value="formState.author"
            placeholder="请输入作者（选填）"
          />
        </Form.Item>

        <Form.Item label="ISBN" name="isbn">
          <Input
            v-model:value="formState.isbn"
            placeholder="请输入 ISBN 编号（选填）"
          />
        </Form.Item>

        <Form.Item>
          <Button
            type="primary"
            html-type="submit"
            :loading="submitting"
            size="large"
          >
            上架
          </Button>
        </Form.Item>
      </Form>
    </Card>
  </div>
</template>
