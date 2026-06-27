import { apiClient } from './client'

export interface AnalyticsSummary {
  totalRoutes: number
  totalDistanceKm: number
  totalOrdersDelivered: number
  avgDistanceKm: number
  avgDurationMinutes: number
  avgCapacityUsagePct: number
  successRate: number
  totalCostBRL: number
  periodDays: number
}

export interface DailyMetric {
  date: string
  routeCount: number
  totalDistanceKm: number
  avgScore: number
  orderCount: number
}

export interface VehicleMetric {
  vehicleId: string
  vehicleName: string
  routeCount: number
  totalDistanceKm: number
  avgCapacityPct: number
}

export const analyticsApi = {
  async summary(): Promise<AnalyticsSummary> {
    const { data } = await apiClient.get<AnalyticsSummary>('/analytics/summary')
    return data
  },

  async daily(days = 30): Promise<DailyMetric[]> {
    const { data } = await apiClient.get<DailyMetric[]>(`/analytics/daily?days=${days}`)
    return data
  },

  async vehicle(vehicleId: string): Promise<VehicleMetric> {
    const { data } = await apiClient.get<VehicleMetric>(`/analytics/vehicle/${vehicleId}`)
    return data
  },
}
