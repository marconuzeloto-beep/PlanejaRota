import { apiClient } from './client'
import type { Vehicle } from '@/shared/types/domain'

export interface CreateVehiclePayload {
  licensePlate: string
  model: string
  type: string
  capacityKg: number
  costPerKmBrl?: number
  notes?: string
}

export const vehicleApi = {
  async list(): Promise<Vehicle[]> {
    const { data } = await apiClient.get<Vehicle[]>('/vehicles')
    return data
  },

  async create(payload: CreateVehiclePayload): Promise<{ vehicleId: string }> {
    const { data } = await apiClient.post('/vehicles', payload)
    return data
  },
}
