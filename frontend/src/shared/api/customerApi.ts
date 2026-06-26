import { apiClient } from './client'
import type { Customer } from '@/shared/types/domain'

export interface CreateCustomerPayload {
  name: string
  email: string
  phone?: string
  latitude: number
  longitude: number
  priority: number
  street?: string
  number?: string
  complement?: string
  neighborhood?: string
  city?: string
  state?: string
  zipCode?: string
}

export const customerApi = {
  async list(): Promise<Customer[]> {
    const { data } = await apiClient.get<Customer[]>('/customers')
    return data
  },

  async create(payload: CreateCustomerPayload): Promise<{ customerId: string }> {
    const { data } = await apiClient.post('/customers', payload)
    return data
  },
}
