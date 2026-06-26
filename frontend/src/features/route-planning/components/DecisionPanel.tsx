import { clsx } from 'clsx'
import type { MapStopDTO, MapRouteDTO, DecisionReasonType } from '@/shared/types/domain'

const REASON_CONFIG: Record<DecisionReasonType, { label: string; color: string; icon: string }> = {
  NEAREST_NEIGHBOR:       { label: 'Vizinho Mais Próximo', color: 'text-blue-600',   icon: '📍' },
  PRIORITY_OVERRIDE:      { label: 'Prioridade Alta',      color: 'text-red-600',    icon: '🔴' },
  TIME_WINDOW_CONSTRAINT: { label: 'Janela de Tempo',      color: 'text-amber-600',  icon: '⏰' },
  HYBRID_SCORE:           { label: 'Score Híbrido',        color: 'text-purple-600', icon: '⚖️' },
  MANUAL_OVERRIDE:        { label: 'Manual',               color: 'text-gray-600',   icon: '✋' },
}

function formatTime(t?: string) {
  return t ? t.substring(0, 5) : '—'
}

interface Props {
  route: MapRouteDTO
  selectedStop?: MapStopDTO | null
  onSelectStop?: (stop: MapStopDTO) => void
}

export function DecisionPanel({ route, selectedStop, onSelectStop }: Props) {
  return (
    <div className="flex flex-col h-full">
      {/* Métricas globais */}
      <div className="p-4 bg-gray-50 border-b grid grid-cols-2 gap-3 text-sm">
        <Metric label="Distância total" value={`${route.totalDistanceKm.toFixed(1)} km`} />
        <Metric label="Tempo estimado" value={`${route.totalEstimatedTimeMinutes} min`} />
        <Metric label="Custo estimado" value={`R$ ${route.estimatedCostBrl?.toFixed(2) ?? '—'}`} />
        <Metric label="Carga" value={`${route.capacityUsagePercent.toFixed(0)}%`} />
        {route.stopsWithTimeViolation > 0 && (
          <div className="col-span-2 bg-red-50 border border-red-200 rounded-lg px-3 py-2 text-red-700 text-xs">
            ⚠️ {route.stopsWithTimeViolation} parada(s) com violação de janela de tempo
          </div>
        )}
        {route.feasible && route.stopsWithTimeViolation === 0 && (
          <div className="col-span-2 bg-green-50 border border-green-200 rounded-lg px-3 py-2 text-green-700 text-xs">
            ✅ Rota totalmente viável
          </div>
        )}
      </div>

      {/* Lista de decisões */}
      <div className="flex-1 overflow-y-auto">
        <div className="px-4 pt-3 pb-1">
          <p className="text-xs font-semibold text-gray-400 uppercase tracking-wide">
            Decisões — {route.stops.length} paradas
          </p>
        </div>
        <ul className="divide-y divide-gray-100">
          {route.stops.map((stop) => {
            const reason = REASON_CONFIG[stop.decisionReasonType] ?? REASON_CONFIG.NEAREST_NEIGHBOR
            const isSelected = selectedStop?.position === stop.position
            return (
              <li
                key={stop.position}
                onClick={() => onSelectStop?.(stop)}
                className={clsx(
                  'px-4 py-3 cursor-pointer transition-colors',
                  isSelected ? 'bg-brand-50 border-l-2 border-brand-500' : 'hover:bg-gray-50',
                )}
              >
                <div className="flex items-start gap-3">
                  <div className={clsx(
                    'shrink-0 w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold text-white mt-0.5',
                    stop.withinTimeWindow ? 'bg-brand-600' : 'bg-red-500',
                  )}>
                    {stop.position}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="font-medium text-gray-900 text-sm truncate">{stop.customerName}</p>
                    <div className="flex items-center gap-2 mt-0.5">
                      <span className={clsx('text-xs', reason.color)}>
                        {reason.icon} {reason.label}
                      </span>
                      <span className="text-xs text-gray-400">·</span>
                      <span className="text-xs text-gray-500">{formatTime(stop.estimatedArrivalTime)}</span>
                      <span className="text-xs text-gray-400">+{stop.distanceFromPreviousKm.toFixed(1)} km</span>
                    </div>
                    {isSelected && (
                      <p className="text-xs text-gray-500 mt-1 italic">{stop.decisionReasonDetail}</p>
                    )}
                    {!stop.withinTimeWindow && (
                      <p className="text-xs text-red-500 mt-0.5">{stop.timeWindowWarning}</p>
                    )}
                  </div>
                </div>
              </li>
            )
          })}
        </ul>
      </div>
    </div>
  )
}

function Metric({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <p className="text-xs text-gray-500">{label}</p>
      <p className="font-semibold text-gray-900">{value}</p>
    </div>
  )
}
