import { useState } from 'react'
import { useRoutes, useOptimizeRoute } from '../hooks/useRoute'
import { RouteMap } from './RouteMap'
import type { Route, RouteStop } from '@/shared/types/domain'

export default function RoutePlanningPage() {
  const { data: routes, isLoading } = useRoutes()
  const [selectedRoute, setSelectedRoute] = useState<Route | null>(null)
  const [selectedStop, setSelectedStop] = useState<RouteStop | null>(null)
  const optimizeRoute = useOptimizeRoute()

  if (isLoading) {
    return (
      <div className="flex h-screen items-center justify-center">
        <div className="text-gray-500">Carregando rotas...</div>
      </div>
    )
  }

  return (
    <div className="flex h-screen bg-gray-100">
      <aside className="w-80 bg-white shadow-md overflow-y-auto flex flex-col">
        <div className="p-4 border-b">
          <h1 className="text-xl font-bold text-brand-700">PlanejaRota</h1>
          <p className="text-sm text-gray-500">Planejamento Logístico</p>
        </div>

        <div className="p-4 flex-1">
          <h2 className="font-semibold text-gray-700 mb-3">Rotas</h2>
          {routes?.length === 0 && (
            <p className="text-sm text-gray-400">Nenhuma rota cadastrada.</p>
          )}
          <ul className="space-y-2">
            {routes?.map((route) => (
              <li key={route.id}>
                <button
                  onClick={() => setSelectedRoute(route)}
                  className={`w-full text-left p-3 rounded-lg border transition-colors ${
                    selectedRoute?.id === route.id
                      ? 'border-brand-500 bg-brand-50 text-brand-700'
                      : 'border-gray-200 hover:border-gray-300 hover:bg-gray-50'
                  }`}
                >
                  <div className="font-medium text-sm">{route.name}</div>
                  <div className="text-xs text-gray-500 mt-1">
                    {route.scheduledDate} · {route.stops.length} paradas
                  </div>
                  <span className={`inline-block mt-1 px-2 py-0.5 rounded text-xs font-medium ${statusColors[route.status]}`}>
                    {statusLabels[route.status]}
                  </span>
                </button>
              </li>
            ))}
          </ul>
        </div>

        {selectedRoute && (
          <div className="p-4 border-t">
            <button
              onClick={() => optimizeRoute.mutate(selectedRoute.id)}
              disabled={optimizeRoute.isPending || selectedRoute.status !== 'DRAFT'}
              className="w-full bg-brand-600 text-white py-2 px-4 rounded-lg text-sm font-medium
                         hover:bg-brand-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              {optimizeRoute.isPending ? 'Otimizando...' : 'Otimizar Rota'}
            </button>
            {selectedRoute.totalDistanceKm && (
              <p className="text-xs text-center text-gray-500 mt-2">
                Distância total: {selectedRoute.totalDistanceKm.toFixed(1)} km
              </p>
            )}
          </div>
        )}
      </aside>

      <main className="flex-1 relative">
        {selectedRoute ? (
          <RouteMap
            route={selectedRoute}
            onStopClick={setSelectedStop}
          />
        ) : (
          <div className="flex h-full items-center justify-center text-gray-400">
            <div className="text-center">
              <div className="text-5xl mb-4">🗺️</div>
              <p>Selecione uma rota para visualizar no mapa</p>
            </div>
          </div>
        )}

        {selectedStop && (
          <div className="absolute bottom-4 right-4 bg-white rounded-lg shadow-lg p-4 w-72 z-[1000]">
            <div className="flex justify-between items-start mb-2">
              <h3 className="font-semibold">Parada #{selectedStop.sequenceNumber}</h3>
              <button onClick={() => setSelectedStop(null)} className="text-gray-400 hover:text-gray-600">✕</button>
            </div>
            <p className="text-sm font-medium">{selectedStop.order.customer.name}</p>
            <p className="text-xs text-gray-500">{selectedStop.order.orderCode}</p>
            <p className="text-xs text-gray-500 mt-1">
              Peso: {selectedStop.order.weightKg} kg
            </p>
            {selectedStop.estimatedArrival && (
              <p className="text-xs text-gray-500">Previsão: {selectedStop.estimatedArrival}</p>
            )}
          </div>
        )}
      </main>
    </div>
  )
}

const statusColors: Record<Route['status'], string> = {
  DRAFT: 'bg-gray-100 text-gray-600',
  PLANNED: 'bg-blue-100 text-blue-700',
  IN_PROGRESS: 'bg-amber-100 text-amber-700',
  COMPLETED: 'bg-green-100 text-green-700',
  CANCELLED: 'bg-red-100 text-red-700',
}

const statusLabels: Record<Route['status'], string> = {
  DRAFT: 'Rascunho',
  PLANNED: 'Planejada',
  IN_PROGRESS: 'Em execução',
  COMPLETED: 'Concluída',
  CANCELLED: 'Cancelada',
}
