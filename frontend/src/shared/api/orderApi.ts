import { apiClient } from './client'
import type { DeliveryOrder } from '@/shared/types/domain'

export interface CreateOrderPayload {
  customerId: string
  orderCode: string
  description?: string
  weightKg: number
  declaredValueBrl?: number
  deliveryDate: string
  notes?: string
}

export const orderApi = {
  async list(): Promise<DeliveryOrder[]> {
    const { data } = await apiClient.get<DeliveryOrder[]>('/orders')
    return data
  },

  async create(payload: CreateOrderPayload): Promise<{ orderId: string }> {
    const { data } = await apiClient.post('/orders', payload)
    return data
  },
}
