import { useState } from 'react'
import { useQuery, useMutation } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { vehicleApi } from '@/shared/api/vehicleApi'
import { orderApi } from '@/shared/api/orderApi'
import { routeApi } from '@/shared/api/routeApi'
import { useAppStore } from '@/shared/store/appStore'
import { RouteMap } from './RouteMap'
import { DecisionPanel } from './DecisionPanel'
import { Spinner } from '@/shared/components/Spinner'
import type { MapStopDTO } from '@/shared/types/domain'

const STRATEGIES = [
  { id: 'SHORTEST_DISTANCE_V1', label: 'Menor Distância', desc: 'Vizinho mais próximo' },
  { id: 'PRIORITY_FIRST_V1',   label: 'Prioridade',       desc: 'Pedidos críticos primeiro' },
  { id: 'HYBRID_V1',           label: 'Híbrido',          desc: 'Score distância + prioridade' },
]

interface FormValues {
  vehicleId: string
  depotLat: number
  depotLng: number
  plannedDate: string
  strategyIdentifier: string
  avgSpeedKmh: number
  stopDurationMinutes: number
}

export default function RoutePlanningPage() {
  const { selectedRoute, setSelectedRoute } = useAppStore()
  const [selectedStop, setSelectedStop] = useState<MapStopDTO | null>(null)
  const [selectedOrders, setSelectedOrders] = useState<Set<string>>(new Set())

  const { data: vehicles } = useQuery({ queryKey: ['vehicles'], queryFn: vehicleApi.list })
  const { data: orders }   = useQuery({ queryKey: ['orders'],   queryFn: orderApi.list })

  const planRoute = useMutation({
    mutationFn: routeApi.plan,
    onSuccess: (data) => { setSelectedRoute(data); setSelectedStop(null) },
  })

  const { register, handleSubmit } = useForm<FormValues>({
    defaultValues: {
      strategyIdentifier: 'SHORTEST_DISTANCE_V1',
      depotLat: -23.5505,
      depotLng: -46.6333,
      plannedDate: new Date().toISOString().slice(0, 10),
      avgSpeedKmh: 40,
      stopDurationMinutes: 10,
    },
  })

  const toggleOrder = (id: string) => {
    setSelectedOrders((prev) => {
      const next = new Set(prev)
      if (next.has(id)) next.delete(id); else next.add(id)
      return next
    })
  }

  const onSubmit = (values: FormValues) => {
    if (selectedOrders.size === 0) return
    planRoute.mutate({
      ...values,
      orderIds: Array.from(selectedOrders),
    })
  }

  const pendingOrders = orders?.filter((o) => o.status === 'PENDING') ?? []

  return (
    <div className="flex h-full">
      {/* Left panel — form */}
      <aside className="w-80 bg-white border-r flex flex-col shrink-0 overflow-y-auto">
        <div className="p-4 border-b">
          <h1 className="font-bold text-gray-900 text-lg">Planejamento de Rota</h1>
          <p className="text-gray-500 text-xs mt-0.5">Configure e gere uma rota otimizada</p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="p-4 space-y-4 flex-1">
          {/* Veículo */}
          <div>
            <label className="label">Veículo</label>
            <select {...register('vehicleId', { required: true })} className="input">
              <option value="">Selecione...</option>
              {vehicles?.filter((v) => v.status === 'AVAILABLE').map((v) => (
                <option key={v.id} value={v.id}>
                  {v.licensePlate} — {v.model} ({v.capacity?.kilograms} kg)
                </option>
              ))}
            </select>
          </div>

          {/* Depot */}
          <div className="grid grid-cols-2 gap-2">
            <div>
              <label className="label">Depot Lat</label>
              <input {...register('depotLat', { valueAsNumber: true })} type="number" step="any" className="input" />
            </div>
            <div>
              <label className="label">Depot Lng</label>
              <input {...register('depotLng', { valueAsNumber: true })} type="number" step="any" className="input" />
            </div>
          </div>

          {/* Data */}
          <div>
            <label className="label">Data de entrega</label>
            <input {...register('plannedDate')} type="date" className="input" />
          </div>

          {/* Estratégia */}
          <div>
            <label className="label">Estratégia</label>
            <div className="space-y-2">
              {STRATEGIES.map((s) => (
                <label key={s.id} className="flex items-start gap-2 cursor-pointer">
                  <input
                    {...register('strategyIdentifier')}
                    type="radio"
                    value={s.id}
                    className="mt-0.5 text-brand-600"
                  />
                  <div>
                    <span className="text-sm font-medium text-gray-800">{s.label}</span>
                    <p className="text-xs text-gray-400">{s.desc}</p>
                  </div>
                </label>
              ))}
            </div>
          </div>

          {/* Params */}
          <div className="grid grid-cols-2 gap-2">
            <div>
              <label className="label">Velocidade (km/h)</label>
              <input {...register('avgSpeedKmh', { valueAsNumber: true })} type="number" className="input" />
            </div>
            <div>
              <label className="label">Parada (min)</label>
              <input {...register('stopDurationMinutes', { valueAsNumber: true })} type="number" className="input" />
            </div>
          </div>

          {/* Pedidos */}
          <div>
            <label className="label">
              Pedidos ({selectedOrders.size} selecionados)
            </label>
            {pendingOrders.length === 0 ? (
              <p className="text-xs text-gray-400">Nenhum pedido pendente</p>
            ) : (
              <div className="border border-gray-200 rounded-lg max-h-44 overflow-y-auto divide-y divide-gray-100">
                {pendingOrders.map((o) => (
                  <label key={o.id} className="flex items-center gap-2 px-3 py-2 cursor-pointer hover:bg-gray-50">
                    <input
                      type="checkbox"
                      checked={selectedOrders.has(o.id)}
                      onChange={() => toggleOrder(o.id)}
                      className="text-brand-600 rounded"
                    />
                    <div>
                      <p className="text-sm font-medium text-gray-800">{o.orderCode}</p>
                      <p className="text-xs text-gray-400">{o.weight?.kilograms} kg</p>
                    </div>
                  </label>
                ))}
              </div>
            )}
          </div>

          <button
            type="submit"
            disabled={planRoute.isPending || selectedOrders.size === 0}
            className="btn-primary w-full py-2.5"
          >
            {planRoute.isPending ? <Spinner size="sm" /> : '🗺️ Gerar Rota'}
          </button>

          {planRoute.isError && (
            <p className="text-red-600 text-sm">Erro ao planejar rota. Verifique os dados.</p>
          )}
        </form>
      </aside>

      {/* Map area */}
      <div className="flex-1 relative">
        {selectedRoute ? (
          <RouteMap route={selectedRoute} selectedStop={selectedStop} onStopClick={setSelectedStop} />
        ) : (
          <div className="flex h-full items-center justify-center bg-gray-100 text-gray-400">
            <div className="text-center">
              <div className="text-6xl mb-4">🗺️</div>
              <p className="font-medium">Configure e gere uma rota</p>
              <p className="text-sm mt-1">O mapa aparecerá aqui</p>
            </div>
          </div>
        )}
      </div>

      {/* Right panel — decisions */}
      {selectedRoute && (
        <aside className="w-72 bg-white border-l overflow-y-auto">
          <div className="p-3 border-b">
            <p className="text-xs font-semibold text-gray-500 uppercase tracking-wide">Explicabilidade</p>
            <p className="text-xs text-gray-400 mt-0.5">{selectedRoute.strategyType}</p>
          </div>
          <DecisionPanel
            route={selectedRoute}
            selectedStop={selectedStop}
            onSelectStop={setSelectedStop}
          />
        </aside>
      )}
    </div>
  )
}
