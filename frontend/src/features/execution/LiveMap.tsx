import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { MapContainer, TileLayer, Marker, Popup, Circle } from 'react-leaflet'
import { divIcon, Icon } from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { executionApi, type LiveExecution } from '@/shared/api/executionApi'

const DEPOT_ICON = new Icon({
  iconUrl:
    'data:image/svg+xml;base64,' +
    btoa(`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 32">
      <text y="28" font-size="28">⭐</text>
    </svg>`),
  iconSize: [28, 28],
  iconAnchor: [14, 14],
})

function currentPositionIcon() {
  return divIcon({
    html: `<div style="
      width:18px;height:18px;border-radius:50%;
      background:rgba(34,197,94,0.85);
      border:3px solid #16a34a;
      box-shadow:0 0 0 6px rgba(34,197,94,0.3);
      animation:pulse 1.5s infinite;
    "></div>
    <style>@keyframes pulse{0%,100%{box-shadow:0 0 0 6px rgba(34,197,94,0.3);}50%{box-shadow:0 0 0 12px rgba(34,197,94,0.05);}}</style>`,
    className: '',
    iconSize: [18, 18],
    iconAnchor: [9, 9],
  })
}

function stopIcon(index: number, completed: boolean) {
  const bg = completed ? '#9ca3af' : '#2563eb'
  return divIcon({
    html: `<div style="
      background:${bg};color:white;border-radius:50%;
      width:26px;height:26px;
      display:flex;align-items:center;justify-content:center;
      font-weight:700;font-size:11px;
      border:2px solid ${completed ? '#6b7280' : '#1d4ed8'};
      box-shadow:0 1px 4px rgba(0,0,0,0.3);
    ">${index + 1}</div>`,
    className: '',
    iconSize: [26, 26],
    iconAnchor: [13, 13],
  })
}

const DEFAULT_CENTER: [number, number] = [-23.5505, -46.6333]

export default function LiveMap() {
  const [selectedId, setSelectedId] = useState<string | null>(null)

  const { data: executions = [], isLoading } = useQuery({
    queryKey: ['execution', 'active'],
    queryFn: executionApi.active,
    refetchInterval: 5000,
  })

  const selected = executions.find((e) => e.id === selectedId) ?? executions[0] ?? null

  const mapCenter: [number, number] =
    selected ? [selected.currentLat, selected.currentLng] : DEFAULT_CENTER

  return (
    <div className="flex h-[calc(100vh-64px)]">
      {/* Left panel */}
      <aside className="w-80 flex-shrink-0 bg-white border-r overflow-y-auto flex flex-col">
        <div className="p-4 border-b">
          <h1 className="text-lg font-bold text-gray-900">Live Executions</h1>
          <p className="text-xs text-gray-500 mt-0.5">Updates every 5 seconds</p>
        </div>

        {isLoading && (
          <div className="p-4 text-sm text-gray-400">Loading active executions...</div>
        )}

        {!isLoading && executions.length === 0 && (
          <div className="p-4 text-sm text-gray-400">No active executions right now.</div>
        )}

        <ul className="divide-y flex-1">
          {executions.map((ex) => (
            <li
              key={ex.id}
              onClick={() => setSelectedId(ex.id)}
              className={`p-4 cursor-pointer hover:bg-blue-50 transition-colors ${
                selected?.id === ex.id ? 'bg-blue-50 border-l-4 border-blue-500' : ''
              }`}
            >
              <div className="flex items-center justify-between mb-1">
                <span className="font-semibold text-sm text-gray-900 truncate">
                  Route {ex.routeId.slice(0, 8)}…
                </span>
                <span
                  className={`text-xs px-2 py-0.5 rounded-full font-medium ${
                    ex.deviationDetected
                      ? 'bg-red-100 text-red-700'
                      : 'bg-green-100 text-green-700'
                  }`}
                >
                  {ex.deviationDetected ? 'Deviation!' : ex.status}
                </span>
              </div>
              <div className="text-xs text-gray-500 space-y-0.5">
                <div>Stops: {ex.completedStops} done / stop {ex.currentStopIndex}</div>
                <div>ETA: ~{ex.estimatedRemainingMinutes} min remaining</div>
              </div>
            </li>
          ))}
        </ul>
      </aside>

      {/* Map */}
      <div className="flex-1 relative">
        {selected?.deviationDetected && (
          <div className="absolute top-4 left-1/2 -translate-x-1/2 z-[1000] bg-red-600 text-white px-4 py-2 rounded-lg shadow-lg text-sm font-semibold flex items-center gap-2">
            ⚠ Deviation detected on route {selected.routeId.slice(0, 8)}
          </div>
        )}

        {selected && (
          <div className="absolute bottom-4 right-4 z-[1000] bg-white rounded-xl shadow-lg p-4 text-sm space-y-1 min-w-[200px]">
            <div className="font-semibold text-gray-800 mb-1">Selected Route</div>
            <div className="text-gray-600">Status: <span className="font-medium">{selected.status}</span></div>
            <div className="text-gray-600">Completed stops: <span className="font-medium">{selected.completedStops}</span></div>
            <div className="text-gray-600">ETA: <span className="font-medium">~{selected.estimatedRemainingMinutes} min</span></div>
            <div className="text-gray-600">Started: <span className="font-medium">{new Date(selected.startedAt).toLocaleTimeString()}</span></div>
          </div>
        )}

        <MapContainer
          center={mapCenter}
          zoom={13}
          className="w-full h-full"
          key={selected?.id ?? 'empty'}
        >
          <TileLayer
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
          />

          {selected && (
            <>
              {/* Current position */}
              <Marker
                position={[selected.currentLat, selected.currentLng]}
                icon={currentPositionIcon()}
              >
                <Popup>Current position</Popup>
              </Marker>

              {/* Pulsing circle around current position */}
              <Circle
                center={[selected.currentLat, selected.currentLng]}
                radius={80}
                pathOptions={{ color: '#22c55e', fillColor: '#22c55e', fillOpacity: 0.15, weight: 1 }}
              />
            </>
          )}
        </MapContainer>
      </div>
    </div>
  )
}
