<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Table, Input, Button, Select, Space, Tag, Card, Empty } from 'ant-design-vue'
import { SearchOutlined, PlusOutlined } from '@ant-design/icons-vue'
import { useProductStore } from '../store'
import { formatDateTime } from '@/shared/utils/format'

const router = useRouter()
const productStore = useProductStore()

const searchType = ref<'name' | 'category'>('name')
const searchKeyword = ref('')
const emptySearch = ref(false)

const pagination = reactive({
  pageSize: 10,
  showTotal: (total: number) => `共 ${total} 件商品`,
})

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '商品名称', dataIndex: 'name', key: 'name', ellipsis: true, minWidth: 160 },
  { title: '分类', dataIndex: 'category', key: 'category', width: 100, responsive: ['md'] },
  { title: '作者', dataIndex: 'author', key: 'author', width: 120, ellipsis: true, responsive: ['lg'] },
  { title: '单价', dataIndex: 'price', key: 'price', width: 100 },
  { title: '库存', dataIndex: 'stock', key: 'stock', width: 80 },
  { title: 'ISBN', dataIndex: 'isbn', key: 'isbn', width: 150, ellipsis: true, responsive: ['lg'] },
  { title: '上架时间', dataIndex: 'createdAt', key: 'createdAt', width: 170, responsive: ['md'] },
  { title: '操作', key: 'action', width: 80 },
]

async function handleSearch() {
  if (!searchKeyword.value.trim()) return

  emptySearch.value = false
  const params = searchType.value === 'name'
    ? { name: searchKeyword.value.trim() }
    : { category: searchKeyword.value.trim() }

  const result = await productStore.search(params)
  if (result && result.length === 0) {
    emptySearch.value = true
  }
}

function handleCreate() {
  router.push('/products/create')
}

onMounted(() => {
  productStore.products = []
})
</script>

<template>
  <div>
    <div class="page-header">
      <h2>商品列表</h2>
      <Button type="primary" @click="handleCreate">
        <PlusOutlined />
        商品上架
      </Button>
    </div>

    <Card style="margin-bottom: 16px">
      <Space>
        <Select
          v-model:value="searchType"
          style="width: 120px"
          :options="[
            { value: 'name', label: '按名称' },
            { value: 'category', label: '按分类' },
          ]"
        />
        <Input.Search
          v-model:value="searchKeyword"
          placeholder="请输入关键词搜索"
          class="search-input"
          @search="handleSearch"
        >
          <template #enterButton>
            <Button type="primary">
              <SearchOutlined />
              搜索
            </Button>
          </template>
        </Input.Search>
      </Space>
      <span v-if="emptySearch" style="margin-left: 16px; color: var(--ant-color-text-secondary)">
        未找到匹配的商品
      </span>
    </Card>

    <Card>
      <Table
        :columns="columns"
        :data-source="productStore.products"
        :loading="productStore.loading"
        :pagination="pagination"
        row-key="id"
        size="middle"
        @row-click="(record: any) => router.push(`/products/${record.id}`)"
        :row-class-name="() => 'clickable-row'"
        :scroll="{ x: 800 }"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'price'">
            ¥{{ record.price?.toFixed?.(2) ?? Number(record.price).toFixed(2) }}
          </template>
          <template v-else-if="column.key === 'category'">
            <Tag v-if="record.category" color="blue">{{ record.category }}</Tag>
          </template>
          <template v-else-if="column.key === 'createdAt'">
            {{ formatDateTime(record.createdAt) }}
          </template>
          <template v-else-if="column.key === 'action'">
            <router-link :to="`/products/${record.id}`">详情</router-link>
          </template>
        </template>
      </Table>

      <Empty
        v-if="!productStore.loading && productStore.products.length === 0"
        description="暂无商品数据，请先上架商品"
      />
    </Card>
  </div>
</template>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 8px;
}

.page-header h2 {
  margin: 0;
}

.search-input {
  width: 360px;
  max-width: 100%;
}

:deep(.clickable-row) {
  cursor: pointer;
}
</style>
