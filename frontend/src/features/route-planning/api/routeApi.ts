import { apiClient } from '@/shared/api/client'
import type { Route } from '@/shared/types/domain'

export interface CreateRouteDto {
  name: string
  vehicleId: string
  depotLatitude: number
  depotLongitude: number
  scheduledDate: string
  departureTime: string
}

export const routeApi = {
  async getAll(): Promise<Route[]> {
    const { data } = await apiClient.get<Route[]>('/routes')
    return data
  },

  async getById(id: string): Promise<Route> {
    const { data } = await apiClient.get<Route>(`/routes/${id}`)
    return data
  },

  async create(dto: CreateRouteDto): Promise<{ id: string }> {
    const { data } = await apiClient.post<{ id: string }>('/routes', dto)
    return data
  },

  async optimize(routeId: string): Promise<{ totalDistanceKm: number }> {
    const { data } = await apiClient.post(`/routes/${routeId}/optimize`)
    return data
  },

  async plan(routeId: string): Promise<void> {
    await apiClient.post(`/routes/${routeId}/plan`)
  },
}
