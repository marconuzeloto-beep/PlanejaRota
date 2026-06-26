// Tipos espelhando os DTOs reais do backend LogiCore.

// ── Value Objects ─────────────────────────────────────────────────────────────

export interface GeoCoordinate {
  latitude: number
  longitude: number
}

export interface TimeWindow {
  openTime: string   // "HH:mm:ss"
  closeTime: string
}

export interface Address {
  street?: string
  number?: string
  complement?: string
  neighborhood?: string
  city?: string
  state?: string
  zipCode?: string
  country?: string
}

// ── Core Domain ───────────────────────────────────────────────────────────────

export type VehicleType = 'MOTORCYCLE' | 'CAR' | 'VAN' | 'TRUCK_SMALL' | 'TRUCK_LARGE'
export type VehicleStatus = 'AVAILABLE' | 'IN_USE' | 'MAINTENANCE' | 'INACTIVE'

export interface Vehicle {
  id: string
  licensePlate: string
  model: string
  type: VehicleType
  capacity: { kilograms: number }
  costPerKm?: { amount: number; currency: string }
  status: VehicleStatus
  notes?: string
}

export interface Customer {
  id: string
  organizationId: string
  name: string
  email?: string
  phone?: string
  address?: Address
  location: GeoCoordinate
  deliveryWindow?: TimeWindow
  priority: { value: number }
  active: boolean
}

export type OrderStatus = 'PENDING' | 'ASSIGNED' | 'DELIVERED' | 'FAILED' | 'CANCELLED'

export interface DeliveryOrder {
  id: string
  customerId: string
  orderCode: string
  description?: string
  weight: { kilograms: number }
  deliveryDate: string
  status: OrderStatus
  notes?: string
}

// ── Planning Engine / Map DTOs ────────────────────────────────────────────────

export type DecisionReasonType =
  | 'NEAREST_NEIGHBOR'
  | 'PRIORITY_OVERRIDE'
  | 'TIME_WINDOW_CONSTRAINT'
  | 'HYBRID_SCORE'
  | 'MANUAL_OVERRIDE'

export type StrategyType = 'SHORTEST_DISTANCE' | 'FASTEST_TIME' | 'PRIORITY_FIRST' | 'HYBRID'

export interface MapStopDTO {
  position: number
  orderId: string
  customerId: string
  customerName: string
  lat: number
  lng: number
  distanceFromPreviousKm: number
  cumulativeDistanceKm: number
  estimatedArrivalTime: string  // "HH:mm:ss"
  withinTimeWindow: boolean
  timeWindowWarning?: string
  decisionReasonType: DecisionReasonType
  decisionReasonDetail: string
}

export interface MapRouteDTO {
  routeId: string
  strategyIdentifier: string
  strategyType: StrategyType
  depotLat: number
  depotLng: number
  stops: MapStopDTO[]
  totalDistanceKm: number
  totalEstimatedTimeMinutes: number
  estimatedCostBrl: number
  capacityUsagePercent: number
  stopsWithTimeViolation: number
  feasible: boolean
}

// ── Simulation Engine ─────────────────────────────────────────────────────────

export interface RouteMetrics {
  totalDistanceKm: number
  totalEstimatedTimeMinutes: number
  estimatedCostBRL: number
  totalWeightKg: number
  capacityUsagePercent: number
  stopsWithTimeViolation: number
  feasible: boolean
}

export interface ScenarioSummary {
  strategyIdentifier: string
  strategyType: StrategyType
  strategyDescription: string
  metrics: RouteMetrics
  decisionResult: {
    strategyType: StrategyType
    orderedSteps: Array<{
      position: number
      orderId: string
      customerId: string
      customerName: string
      location: GeoCoordinate
      reason: { type: DecisionReasonType; detail: string }
      distanceFromPreviousKm: number
      cumulativeDistanceKm: number
      estimatedArrivalTime: string
      withinTimeWindow: boolean
      timeWindowWarning?: string
      priorityScore: number
    }>
    metrics: RouteMetrics
  }
}

export interface ScenarioComparison {
  scenarios: ScenarioSummary[]
  recommendedStrategyIdentifier: string
  recommendationReason: string
}

export interface ImpactReport {
  simulatedForOrderId: string
  additionalDistanceKm: number
  additionalTimeMinutes: number
  stillFeasible: boolean
}

// ── Auth ──────────────────────────────────────────────────────────────────────

export interface AuthUser {
  userId: string
  organizationId: string
  role: string
  accessToken: string
  refreshToken: string
}
