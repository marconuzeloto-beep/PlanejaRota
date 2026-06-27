import { apiClient } from './client'

export interface LiveExecution {
  id: string
  routeId: string
  status: string
  currentLat: number
  currentLng: number
  currentStopIndex: number
  completedStops: number
  estimatedRemainingMinutes: number
  deviationDetected: boolean
  startedAt: string
}

export interface GpsUpdate {
  lat: number
  lng: number
  timestamp: string
}

export const executionApi = {
  async active(): Promise<LiveExecution[]> {
    const { data } = await apiClient.get<LiveExecution[]>('/execution/active')
    return data
  },

  async start(routeId: string): Promise<LiveExecution> {
    const { data } = await apiClient.post<LiveExecution>(`/execution/${routeId}/start`)
    return data
  },

  async stop(routeId: string): Promise<void> {
    await apiClient.post(`/execution/${routeId}/stop`)
  },

  async sendGps(routeId: string, update: GpsUpdate): Promise<void> {
    await apiClient.post(`/execution/${routeId}/gps`, update)
  },
}
