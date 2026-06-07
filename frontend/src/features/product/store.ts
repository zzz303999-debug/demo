import { defineStore } from 'pinia'
import { ref } from 'vue'
import { productApi } from './api'
import type { Product, CreateProductForm, UpdateStockForm } from '@/shared/types/product'

export const useProductStore = defineStore('product', () => {
  const products = ref<Product[]>([])
  const currentProduct = ref<Product | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function create(form: CreateProductForm) {
    loading.value = true
    error.value = null
    try {
      const res = await productApi.create(form)
      products.value.unshift(res.data)
      return res.data
    } catch (e: any) {
      error.value = e.response?.data?.message ?? '上架失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  async function fetchProduct(id: number) {
    loading.value = true
    try {
      const res = await productApi.getById(id)
      currentProduct.value = res.data
      return res.data
    } catch (e: any) {
      error.value = e.response?.data?.message ?? '查询失败'
      return null
    } finally {
      loading.value = false
    }
  }

  async function search(params?: { name?: string; category?: string }) {
    loading.value = true
    try {
      const res = await productApi.search(params)
      products.value = res.data
      return res.data
    } catch (e: any) {
      error.value = e.response?.data?.message ?? '搜索失败'
      return []
    } finally {
      loading.value = false
    }
  }

  async function updateStock(id: number, form: UpdateStockForm) {
    loading.value = true
    try {
      const res = await productApi.updateStock(id, form)
      const idx = products.value.findIndex((p) => p.id === id)
      if (idx !== -1) products.value[idx] = res.data
      if (currentProduct.value?.id === id) currentProduct.value = res.data
      return res.data
    } catch (e: any) {
      error.value = e.response?.data?.message ?? '库存变更失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  return { products, currentProduct, loading, error, create, fetchProduct, search, updateStock }
})
