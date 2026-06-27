import { apiClient } from './client'

export interface BenchmarkEntry {
  id: string
  strategyIdentifier: string
  strategyType: string
  orderCount: number
  executionTimeMs: number
  totalDistanceKm: number
  totalTimeMinutes: number
  capacityUsagePct: number
  stopsWithViolations: number
  feasible: boolean
  routeScore: number | null
  scoreGrade: string | null
  twoOptApplied: boolean
  twoOptImprovementKm: number
  memoryBytes: number | null
  createdAt: string
}

export interface BenchmarkSummary {
  strategyIdentifier: string
  strategyType: string
  executionCount: number
  avgExecutionTimeMs: number
  maxExecutionTimeMs: number
  avgDistanceKm: number
  avgScore: number
  avgCapacityUsagePct: number
  feasibleCount: number
}

export interface RunBenchmarkPayload {
  vehicleId: string
  orderIds: string[]
  depotLat: number
  depotLng: number
  strategyIdentifiers?: string[]
  maxWeightKg?: number
  avgSpeedKmh?: number
}

export interface RunBenchmarkResult {
  entries: BenchmarkEntry[]
  recommendedStrategy: string
  bestDistanceKm: number
  fastestStrategyMs: number
}

export const benchmarkApi = {
  async run(payload: RunBenchmarkPayload): Promise<RunBenchmarkResult> {
    const { data } = await apiClient.post<RunBenchmarkResult>('/benchmark/run', payload)
    return data
  },

  async history(limit = 50): Promise<BenchmarkEntry[]> {
    const { data } = await apiClient.get<BenchmarkEntry[]>(`/benchmark/history?limit=${limit}`)
    return data
  },

  async ranking(): Promise<BenchmarkSummary[]> {
    const { data } = await apiClient.get<BenchmarkSummary[]>('/benchmark/ranking')
    return data
  },

  async historyByStrategy(strategyId: string, limit = 20): Promise<BenchmarkEntry[]> {
    const { data } = await apiClient.get<BenchmarkEntry[]>(`/benchmark/history/${strategyId}?limit=${limit}`)
    return data
  },
}
