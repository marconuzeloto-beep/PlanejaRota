import { MapContainer, TileLayer, Marker, Popup, Polyline } from 'react-leaflet'
import { divIcon, Icon } from 'leaflet'
import 'leaflet/dist/leaflet.css'
import type { MapRouteDTO, MapStopDTO } from '@/shared/types/domain'

interface RouteMapProps {
  route: MapRouteDTO
  selectedStop?: MapStopDTO | null
  onStopClick?: (stop: MapStopDTO) => void
}

const DEPOT_ICON = new Icon({
  iconUrl: 'data:image/svg+xml;base64,' + btoa(`
    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 32 40">
      <path fill="#1d4ed8" d="M16 0C9.4 0 4 5.4 4 12c0 9 12 28 12 28s12-19 12-28C28 5.4 22.6 0 16 0z"/>
      <circle cx="16" cy="12" r="5" fill="white"/>
    </svg>
  `),
  iconSize: [28, 36],
  iconAnchor: [14, 36],
})

function stopIcon(position: number, withinWindow: boolean, selected: boolean) {
  const bg = selected ? '#7c3aed' : withinWindow ? '#2563eb' : '#dc2626'
  const border = selected ? '#5b21b6' : withinWindow ? '#1d4ed8' : '#b91c1c'
  return divIcon({
    html: `<div style="
      background:${bg};color:white;border-radius:50%;
      width:30px;height:30px;
      display:flex;align-items:center;justify-content:center;
      font-weight:700;font-size:12px;
      border:2.5px solid ${border};
      box-shadow:0 2px 6px rgba(0,0,0,0.35);
      ${selected ? 'transform:scale(1.2);' : ''}
    ">${position}</div>`,
    className: '',
    iconSize: [30, 30],
    iconAnchor: [15, 15],
  })
}

function formatTime(t?: string) {
  if (!t) return '—'
  return t.substring(0, 5) // "HH:mm"
}

export function RouteMap({ route, selectedStop, onStopClick }: RouteMapProps) {
  const center: [number, number] = [route.depotLat, route.depotLng]
  const polyline: [number, number][] = [
    [route.depotLat, route.depotLng],
    ...route.stops.map((s) => [s.lat, s.lng] as [number, number]),
  ]

  return (
    <MapContainer center={center} zoom={12} style={{ height: '100%', width: '100%' }}>
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />

      {/* Depot */}
      <Marker position={center} icon={DEPOT_ICON}>
        <Popup>
          <strong>Ponto de partida (Depot)</strong><br />
          {route.depotLat.toFixed(5)}, {route.depotLng.toFixed(5)}
        </Popup>
      </Marker>

      {/* Route polyline */}
      <Polyline positions={polyline} color="#2563eb" weight={3} opacity={0.65} />

      {/* Stop markers */}
      {route.stops.map((stop) => {
        const isSelected = selectedStop?.position === stop.position
        return (
          <Marker
            key={stop.position}
            position={[stop.lat, stop.lng]}
            icon={stopIcon(stop.position, stop.withinTimeWindow, isSelected)}
            eventHandlers={{ click: () => onStopClick?.(stop) }}
          >
            <Popup>
              <div className="text-sm space-y-1 min-w-[180px]">
                <p className="font-semibold">#{stop.position} — {stop.customerName}</p>
                <p className="text-gray-500">Chegada: {formatTime(stop.estimatedArrivalTime)}</p>
                <p className="text-gray-500">+{stop.distanceFromPreviousKm.toFixed(1)} km</p>
                {!stop.withinTimeWindow && (
                  <p className="text-red-600 text-xs">{stop.timeWindowWarning}</p>
                )}
                <p className="text-xs text-gray-400 italic">{stop.decisionReasonDetail}</p>
              </div>
            </Popup>
          </Marker>
        )
      })}
    </MapContainer>
  )
}
