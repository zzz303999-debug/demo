<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Card, Descriptions, Spin, Empty } from 'ant-design-vue'
import { useCustomerStore } from '../store'

const route = useRoute()
const customerStore = useCustomerStore()
const loading = ref(true)

onMounted(async () => {
  const id = Number(route.params.id)
  await customerStore.fetchCustomer(id)
  loading.value = false
})
</script>

<template>
  <div class="detail-container">
    <Card title="客户信息" :bordered="false" style="max-width: 640px; margin: 0 auto">
      <Spin :spinning="loading">
        <template v-if="customerStore.currentCustomer">
          <Descriptions :column="1" bordered>
            <Descriptions.Item label="ID">{{ customerStore.currentCustomer.id }}</Descriptions.Item>
            <Descriptions.Item label="用户名">{{ customerStore.currentCustomer.username }}</Descriptions.Item>
            <Descriptions.Item label="邮箱">{{ customerStore.currentCustomer.email || '-' }}</Descriptions.Item>
            <Descriptions.Item label="手机号">{{ customerStore.currentCustomer.phone || '-' }}</Descriptions.Item>
            <Descriptions.Item label="地址">{{ customerStore.currentCustomer.address || '-' }}</Descriptions.Item>
            <Descriptions.Item label="注册时间">{{ customerStore.currentCustomer.createdAt }}</Descriptions.Item>
          </Descriptions>
        </template>
        <Empty v-else-if="!loading" description="未找到客户信息" />
      </Spin>
    </Card>
  </div>
</template>

<style scoped>
.detail-container {
  padding: 48px;
  background: #f0f2f5;
  min-height: 100vh;
}
</style>
