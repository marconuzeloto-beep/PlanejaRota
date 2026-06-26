// Tipos TypeScript espelhando o domínio do backend.
// Mantidos em sincronia com os DTOs da API.

export interface GeoCoordinate {
  latitude: number
  longitude: number
}

export interface TimeWindow {
  openTime: string  // "HH:mm"
  closeTime: string
}

export interface Address {
  street: string
  number: string
  complement?: string
  neighborhood: string
  city: string
  state: string
  zipCode: string
  country: string
}

export type VehicleType = 'MOTORCYCLE' | 'CAR' | 'VAN' | 'TRUCK_SMALL' | 'TRUCK_LARGE'
export type VehicleStatus = 'AVAILABLE' | 'IN_USE' | 'MAINTENANCE' | 'INACTIVE'

export interface Vehicle {
  id: string
  licensePlate: string
  model: string
  type: VehicleType
  capacityKg: number
  status: VehicleStatus
  notes?: string
}

export type CustomerPriority = 1 | 2 | 3 | 4 | 5

export interface Customer {
  id: string
  name: string
  email?: string
  phone?: string
  address: Address
  location: GeoCoordinate
  preferredDeliveryWindow?: TimeWindow
  priority: CustomerPriority
}

export type OrderStatus = 'PENDING' | 'ASSIGNED' | 'DELIVERED' | 'FAILED' | 'CANCELLED'

export interface DeliveryOrder {
  id: string
  customer: Customer
  orderCode: string
  description?: string
  weightKg: number
  value?: number
  deliveryDate: string  // ISO date "YYYY-MM-DD"
  deliveryWindow?: TimeWindow
  status: OrderStatus
  notes?: string
}

export type RouteStatus = 'DRAFT' | 'PLANNED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'
export type StopStatus = 'PENDING' | 'IN_TRANSIT' | 'COMPLETED' | 'FAILED' | 'SKIPPED'

export interface RouteStop {
  id: string
  order: DeliveryOrder
  sequenceNumber: number
  status: StopStatus
  estimatedArrival?: string  // "HH:mm"
  actualArrival?: string
  failureReason?: string
  location: GeoCoordinate
}

export interface Route {
  id: string
  name: string
  vehicle: Vehicle
  depotLocation: GeoCoordinate
  scheduledDate: string
  departureTime: string
  status: RouteStatus
  stops: RouteStop[]
  totalDistanceKm?: number
}
