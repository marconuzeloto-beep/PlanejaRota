import { apiClient } from './client'
import type { MapRouteDTO, ScenarioComparison, ImpactReport } from '@/shared/types/domain'

export interface PlanRoutePayload {
  vehicleId: string
  orderIds: string[]
  depotLat: number
  depotLng: number
  plannedDate: string
  strategyIdentifier?: string
  strategyWeights?: Record<string, number>
  maxWeightKg?: number
  avgSpeedKmh?: number
  stopDurationMinutes?: number
  hardTimeWindows?: boolean
}

export interface SimulatePayload {
  vehicleId: string
  orderIds: string[]
  depotLat: number
  depotLng: number
  strategyIdentifiers?: string[]
  maxWeightKg?: number
  avgSpeedKmh?: number
}

export const routeApi = {
  async plan(payload: PlanRoutePayload): Promise<MapRouteDTO> {
    const { data } = await apiClient.post<MapRouteDTO>('/routes/plan', payload)
    return data
  },

  async simulate(payload: SimulatePayload): Promise<ScenarioComparison> {
    const { data } = await apiClient.post<ScenarioComparison>('/routes/simulate', payload)
    return data
  },

  async whatIf(routeId: string, orderId: string): Promise<ImpactReport> {
    const { data } = await apiClient.post<ImpactReport>(`/routes/${routeId}/what-if/${orderId}`)
    return data
  },
}
