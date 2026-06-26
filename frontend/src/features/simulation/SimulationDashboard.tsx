import { useState } from 'react'
import { useQuery, useMutation } from '@tanstack/react-query'
import { vehicleApi } from '@/shared/api/vehicleApi'
import { orderApi } from '@/shared/api/orderApi'
import { routeApi } from '@/shared/api/routeApi'
import { useAppStore } from '@/shared/store/appStore'
import { RouteMap } from '@/features/route-planning/components/RouteMap'
import { DecisionPanel } from '@/features/route-planning/components/DecisionPanel'
import { Badge } from '@/shared/components/Badge'
import { Spinner } from '@/shared/components/Spinner'
import { EmptyState } from '@/shared/components/EmptyState'
import { clsx } from 'clsx'
import type { ScenarioSummary, MapStopDTO, MapRouteDTO } from '@/shared/types/domain'

const STRATEGIES_OPTIONS = [
  { id: 'SHORTEST_DISTANCE_V1', label: 'Menor Distância' },
  { id: 'PRIORITY_FIRST_V1',   label: 'Prioridade' },
  { id: 'HYBRID_V1',           label: 'Híbrido' },
]

function scenarioToMapRouteDTO(s: ScenarioSummary, depotLat: number, depotLng: number): MapRouteDTO {
  return {
    routeId: s.strategyIdentifier,
    strategyIdentifier: s.strategyIdentifier,
    strategyType: s.strategyType,
    depotLat,
    depotLng,
    stops: s.decisionResult.orderedSteps.map((step) => ({
      position: step.position,
      orderId: step.orderId,
      customerId: step.customerId,
      customerName: step.customerName,
      lat: step.location.latitude,
      lng: step.location.longitude,
      distanceFromPreviousKm: step.distanceFromPreviousKm,
      cumulativeDistanceKm: step.cumulativeDistanceKm,
      estimatedArrivalTime: step.estimatedArrivalTime,
      withinTimeWindow: step.withinTimeWindow,
      timeWindowWarning: step.timeWindowWarning,
      decisionReasonType: step.reason.type,
      decisionReasonDetail: step.reason.detail,
    })),
    totalDistanceKm: s.metrics.totalDistanceKm,
    totalEstimatedTimeMinutes: s.metrics.totalEstimatedTimeMinutes,
    estimatedCostBrl: s.metrics.estimatedCostBRL,
    capacityUsagePercent: s.metrics.capacityUsagePercent,
    stopsWithTimeViolation: s.metrics.stopsWithTimeViolation,
    feasible: s.metrics.feasible,
  }
}

export default function SimulationDashboard() {
  const { comparison, setComparison, selectedScenarioIdentifier, selectScenario } = useAppStore()

  const [vehicleId, setVehicleId] = useState('')
  const [selectedOrders, setSelectedOrders] = useState<Set<string>>(new Set())
  const [selectedStrategies, setSelectedStrategies] = useState<Set<string>>(
    new Set(['SHORTEST_DISTANCE_V1', 'PRIORITY_FIRST_V1', 'HYBRID_V1'])
  )
  const [depotLat, setDepotLat] = useState(-23.5505)
  const [depotLng, setDepotLng] = useState(-46.6333)
  const [selectedStop, setSelectedStop] = useState<MapStopDTO | null>(null)
  const [viewMode, setViewMode] = useState<'compare' | 'map'>('compare')

  const { data: vehicles } = useQuery({ queryKey: ['vehicles'], queryFn: vehicleApi.list })
  const { data: orders }   = useQuery({ queryKey: ['orders'],   queryFn: orderApi.list })

  const simulate = useMutation({
    mutationFn: routeApi.simulate,
    onSuccess: (data) => {
      setComparison(data)
      selectScenario(data.recommendedStrategyIdentifier)
      setViewMode('compare')
    },
  })

  const toggleOrder = (id: string) => setSelectedOrders((prev) => {
    const n = new Set(prev)
    if (n.has(id)) n.delete(id); else n.add(id)
    return n
  })

  const toggleStrategy = (id: string) => setSelectedStrategies((prev) => {
    const n = new Set(prev)
    if (n.has(id)) n.delete(id); else n.add(id)
    return n
  })

  const selectedScenario = comparison?.scenarios.find(
    (s) => s.strategyIdentifier === selectedScenarioIdentifier
  )

  const selectedMapRoute = selectedScenario
    ? scenarioToMapRouteDTO(selectedScenario, depotLat, depotLng)
    : null

  const pendingOrders = orders?.filter((o) => o.status === 'PENDING') ?? []

  return (
    <div className="flex h-full">
      {/* Config sidebar */}
      <aside className="w-72 bg-white border-r shrink-0 flex flex-col overflow-y-auto">
        <div className="p-4 border-b">
          <h1 className="font-bold text-gray-900 text-lg">Simulação</h1>
          <p className="text-gray-500 text-xs mt-0.5">Compare estratégias de otimização</p>
        </div>

        <div className="p-4 space-y-5 flex-1">
          {/* Veículo */}
          <div>
            <label className="label">Veículo</label>
            <select
              value={vehicleId}
              onChange={(e) => setVehicleId(e.target.value)}
              className="input"
            >
              <option value="">Selecione...</option>
              {vehicles?.filter((v) => v.status === 'AVAILABLE').map((v) => (
                <option key={v.id} value={v.id}>
                  {v.licensePlate} — {v.model}
                </option>
              ))}
            </select>
          </div>

          {/* Depot */}
          <div className="grid grid-cols-2 gap-2">
            <div>
              <label className="label">Depot Lat</label>
              <input
                type="number" step="any" value={depotLat}
                onChange={(e) => setDepotLat(parseFloat(e.target.value))}
                className="input"
              />
            </div>
            <div>
              <label className="label">Depot Lng</label>
              <input
                type="number" step="any" value={depotLng}
                onChange={(e) => setDepotLng(parseFloat(e.target.value))}
                className="input"
              />
            </div>
          </div>

          {/* Estratégias */}
          <div>
            <label className="label">Estratégias a comparar</label>
            <div className="space-y-2">
              {STRATEGIES_OPTIONS.map((s) => (
                <label key={s.id} className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={selectedStrategies.has(s.id)}
                    onChange={() => toggleStrategy(s.id)}
                    className="text-brand-600 rounded"
                  />
                  <span className="text-sm text-gray-800">{s.label}</span>
                </label>
              ))}
            </div>
          </div>

          {/* Pedidos */}
          <div>
            <label className="label">Pedidos ({selectedOrders.size} selecionados)</label>
            {pendingOrders.length === 0 ? (
              <p className="text-xs text-gray-400">Nenhum pedido pendente</p>
            ) : (
              <div className="border border-gray-200 rounded-lg max-h-48 overflow-y-auto divide-y divide-gray-100">
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
            onClick={() => simulate.mutate({
              vehicleId,
              orderIds: Array.from(selectedOrders),
              depotLat,
              depotLng,
              strategyIdentifiers: Array.from(selectedStrategies),
            })}
            disabled={simulate.isPending || !vehicleId || selectedOrders.size === 0 || selectedStrategies.size === 0}
            className="btn-primary w-full py-2.5"
          >
            {simulate.isPending ? <Spinner size="sm" /> : '🔬 Simular'}
          </button>

          {simulate.isError && (
            <p className="text-red-600 text-sm">Erro na simulação. Verifique os dados.</p>
          )}
        </div>
      </aside>

      {/* Main content */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {!comparison ? (
          <div className="flex-1 flex items-center justify-center">
            <EmptyState icon="🔬" title="Nenhuma simulação executada" description="Configure os parâmetros e clique em Simular" />
          </div>
        ) : (
          <>
            {/* Tabs */}
            <div className="bg-white border-b px-4 flex items-center gap-1 h-11 shrink-0">
              {['compare', 'map'].map((mode) => (
                <button
                  key={mode}
                  onClick={() => setViewMode(mode as 'compare' | 'map')}
                  className={clsx(
                    'px-3 py-1.5 text-sm rounded-md transition-colors',
                    viewMode === mode ? 'bg-brand-100 text-brand-700 font-medium' : 'text-gray-600 hover:bg-gray-100',
                  )}
                >
                  {mode === 'compare' ? '📊 Comparar' : '🗺️ Mapa'}
                </button>
              ))}
              <div className="ml-auto text-xs text-gray-400">
                {comparison.scenarios.length} cenários simulados
              </div>
            </div>

            {viewMode === 'compare' ? (
              <CompareView
                comparison={comparison}
                selectedIdentifier={selectedScenarioIdentifier}
                onSelect={selectScenario}
              />
            ) : (
              <div className="flex flex-1 overflow-hidden">
                <div className="flex-1">
                  {selectedMapRoute ? (
                    <RouteMap
                      route={selectedMapRoute}
                      selectedStop={selectedStop}
                      onStopClick={setSelectedStop}
                    />
                  ) : (
                    <div className="flex h-full items-center justify-center text-gray-400">
                      Selecione um cenário na aba Comparar
                    </div>
                  )}
                </div>
                {selectedMapRoute && (
                  <aside className="w-72 bg-white border-l overflow-y-auto shrink-0">
                    <div className="p-3 border-b">
                      <p className="text-xs font-semibold text-gray-500 uppercase tracking-wide">Decisões</p>
                      <p className="text-xs text-gray-400 mt-0.5">{selectedMapRoute.strategyType}</p>
                    </div>
                    <DecisionPanel
                      route={selectedMapRoute}
                      selectedStop={selectedStop}
                      onSelectStop={setSelectedStop}
                    />
                  </aside>
                )}
              </div>
            )}
          </>
        )}
      </div>
    </div>
  )
}

function CompareView({
  comparison,
  selectedIdentifier,
  onSelect,
}: {
  comparison: NonNullable<import('@/shared/types/domain').ScenarioComparison>
  selectedIdentifier: string | null
  onSelect: (id: string) => void
}) {
  const STRATEGY_LABELS: Record<string, string> = {
    SHORTEST_DISTANCE_V1: 'Menor Distância',
    PRIORITY_FIRST_V1:    'Prioridade Primeiro',
    HYBRID_V1:            'Híbrido',
  }

  return (
    <div className="flex-1 overflow-auto p-6">
      {/* Recomendação */}
      <div className="bg-brand-50 border border-brand-200 rounded-xl p-4 mb-6">
        <p className="text-xs font-semibold text-brand-600 uppercase tracking-wide mb-1">Recomendação do sistema</p>
        <p className="font-semibold text-brand-900">
          {STRATEGY_LABELS[comparison.recommendedStrategyIdentifier] ?? comparison.recommendedStrategyIdentifier}
        </p>
        <p className="text-sm text-brand-700 mt-1">{comparison.recommendationReason}</p>
      </div>

      {/* Tabela comparativa */}
      <div className="card overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 border-b">
            <tr>
              <th className="text-left px-4 py-3 text-gray-500 font-medium">Estratégia</th>
              <th className="text-right px-4 py-3 text-gray-500 font-medium">Distância</th>
              <th className="text-right px-4 py-3 text-gray-500 font-medium">Tempo</th>
              <th className="text-right px-4 py-3 text-gray-500 font-medium">Custo</th>
              <th className="text-right px-4 py-3 text-gray-500 font-medium">Violações</th>
              <th className="text-center px-4 py-3 text-gray-500 font-medium">Status</th>
              <th className="px-4 py-3"></th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {comparison.scenarios.map((s: import('@/shared/types/domain').ScenarioSummary) => {
              const isSelected = s.strategyIdentifier === selectedIdentifier
              const isRecommended = s.strategyIdentifier === comparison.recommendedStrategyIdentifier
              return (
                <tr
                  key={s.strategyIdentifier}
                  className={clsx(
                    'transition-colors',
                    isSelected ? 'bg-brand-50' : 'hover:bg-gray-50',
                  )}
                >
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-2">
                      <span className="font-medium text-gray-900">
                        {STRATEGY_LABELS[s.strategyIdentifier] ?? s.strategyIdentifier}
                      </span>
                      {isRecommended && <Badge label="Recomendado" variant="blue" />}
                    </div>
                    <p className="text-xs text-gray-400 mt-0.5">{s.strategyDescription}</p>
                  </td>
                  <td className="px-4 py-3 text-right font-mono">
                    {s.metrics.totalDistanceKm.toFixed(1)} km
                  </td>
                  <td className="px-4 py-3 text-right font-mono">
                    {s.metrics.totalEstimatedTimeMinutes} min
                  </td>
                  <td className="px-4 py-3 text-right font-mono">
                    R$ {s.metrics.estimatedCostBRL?.toFixed(2) ?? '—'}
                  </td>
                  <td className="px-4 py-3 text-right">
                    {s.metrics.stopsWithTimeViolation > 0 ? (
                      <span className="text-red-600 font-medium">{s.metrics.stopsWithTimeViolation}</span>
                    ) : (
                      <span className="text-green-600">0</span>
                    )}
                  </td>
                  <td className="px-4 py-3 text-center">
                    <Badge
                      label={s.metrics.feasible ? 'Viável' : 'Inviável'}
                      variant={s.metrics.feasible ? 'green' : 'red'}
                    />
                  </td>
                  <td className="px-4 py-3">
                    <button
                      onClick={() => onSelect(s.strategyIdentifier)}
                      className={clsx(
                        'text-xs px-3 py-1 rounded-md transition-colors',
                        isSelected
                          ? 'bg-brand-600 text-white'
                          : 'bg-gray-100 text-gray-600 hover:bg-brand-100 hover:text-brand-700',
                      )}
                    >
                      {isSelected ? 'Selecionado' : 'Selecionar'}
                    </button>
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>

      {/* Mini-cards de decisão por cenário */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4 mt-6">
        {comparison.scenarios.map((s: import('@/shared/types/domain').ScenarioSummary) => (
          <ScenarioCard
            key={s.strategyIdentifier}
            scenario={s}
            isSelected={s.strategyIdentifier === selectedIdentifier}
            isRecommended={s.strategyIdentifier === comparison.recommendedStrategyIdentifier}
            onSelect={() => onSelect(s.strategyIdentifier)}
          />
        ))}
      </div>
    </div>
  )
}

function ScenarioCard({
  scenario, isSelected, isRecommended, onSelect,
}: {
  scenario: ScenarioSummary
  isSelected: boolean
  isRecommended: boolean
  onSelect: () => void
}) {
  const STRATEGY_LABELS: Record<string, string> = {
    SHORTEST_DISTANCE_V1: 'Menor Distância',
    PRIORITY_FIRST_V1:    'Prioridade Primeiro',
    HYBRID_V1:            'Híbrido',
  }

  return (
    <div
      onClick={onSelect}
      className={clsx(
        'card p-4 cursor-pointer transition-all',
        isSelected ? 'ring-2 ring-brand-500 shadow-md' : 'hover:shadow-md',
        isRecommended && 'border-brand-300',
      )}
    >
      <div className="flex items-center justify-between mb-3">
        <p className="font-semibold text-gray-900 text-sm">
          {STRATEGY_LABELS[scenario.strategyIdentifier] ?? scenario.strategyIdentifier}
        </p>
        {isRecommended && <Badge label="★ Melhor" variant="blue" />}
      </div>

      <div className="space-y-1.5 text-sm">
        <div className="flex justify-between">
          <span className="text-gray-500">Distância</span>
          <span className="font-mono font-medium">{scenario.metrics.totalDistanceKm.toFixed(1)} km</span>
        </div>
        <div className="flex justify-between">
          <span className="text-gray-500">Tempo</span>
          <span className="font-mono font-medium">{scenario.metrics.totalEstimatedTimeMinutes} min</span>
        </div>
        <div className="flex justify-between">
          <span className="text-gray-500">Carga</span>
          <span className="font-mono font-medium">{scenario.metrics.capacityUsagePercent.toFixed(0)}%</span>
        </div>
      </div>

      {/* Top 3 stops preview */}
      <div className="mt-3 pt-3 border-t space-y-1">
        {scenario.decisionResult.orderedSteps.slice(0, 3).map((step) => (
          <div key={step.position} className="flex items-center gap-2 text-xs text-gray-500">
            <span className="bg-brand-100 text-brand-700 rounded-full w-4 h-4 flex items-center justify-center shrink-0 font-medium">
              {step.position}
            </span>
            <span className="truncate">{step.customerName}</span>
            <span className="ml-auto shrink-0">{step.distanceFromPreviousKm.toFixed(1)} km</span>
          </div>
        ))}
        {scenario.decisionResult.orderedSteps.length > 3 && (
          <p className="text-xs text-gray-400 text-center">
            +{scenario.decisionResult.orderedSteps.length - 3} paradas
          </p>
        )}
      </div>
    </div>
  )
}
