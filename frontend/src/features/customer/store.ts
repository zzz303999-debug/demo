import { defineStore } from 'pinia'
import { ref } from 'vue'
import { customerApi } from './api'
import type { Customer, UpdateCustomerForm } from '@/shared/types/customer'

export const useCustomerStore = defineStore('customer', () => {
  const currentCustomer = ref<Customer | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function fetchCustomer(id: number) {
    loading.value = true
    error.value = null
    try {
      const res = await customerApi.getById(id)
      currentCustomer.value = res.data
      return res.data
    } catch (e: any) {
      error.value = e.response?.data?.message ?? '查询失败'
      return null
    } finally {
      loading.value = false
    }
  }

  async function updateProfile(id: number, form: UpdateCustomerForm) {
    loading.value = true
    error.value = null
    try {
      const res = await customerApi.update(id, form)
      currentCustomer.value = res.data
      return res.data
    } catch (e: any) {
      error.value = e.response?.data?.message ?? '修改失败'
      throw e
    } finally {
      loading.value = false
    }
  }

  return { currentCustomer, loading, error, fetchCustomer, updateProfile }
})
