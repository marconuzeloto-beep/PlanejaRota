import { MapContainer, TileLayer, Marker, Popup, Polyline, useMap } from 'react-leaflet'
import { Icon, divIcon } from 'leaflet'
import 'leaflet/dist/leaflet.css'
import type { Route, GeoCoordinate, RouteStop } from '@/shared/types/domain'

interface RouteMapProps {
  route: Route
  onStopClick?: (stop: RouteStop) => void
}

const DEPOT_ICON = new Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-blue.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
})

function stopIcon(sequenceNumber: number, status: RouteStop['status']) {
  const colorMap: Record<RouteStop['status'], string> = {
    PENDING: '#6b7280',
    IN_TRANSIT: '#f59e0b',
    COMPLETED: '#10b981',
    FAILED: '#ef4444',
    SKIPPED: '#9ca3af',
  }
  return divIcon({
    html: `<div style="
      background:${colorMap[status]};
      color:white;
      border-radius:50%;
      width:28px;height:28px;
      display:flex;align-items:center;justify-content:center;
      font-weight:bold;font-size:12px;
      border:2px solid white;
      box-shadow:0 2px 4px rgba(0,0,0,0.3)
    ">${sequenceNumber}</div>`,
    className: '',
    iconSize: [28, 28],
    iconAnchor: [14, 14],
  })
}

function RoutePolyline({ depot, stops }: { depot: GeoCoordinate; stops: RouteStop[] }) {
  const positions: [number, number][] = [
    [depot.latitude, depot.longitude],
    ...stops
      .sort((a, b) => a.sequenceNumber - b.sequenceNumber)
      .map((s) => [s.location.latitude, s.location.longitude] as [number, number]),
    [depot.latitude, depot.longitude],
  ]
  return <Polyline positions={positions} color="#2563eb" weight={3} opacity={0.7} dashArray="8 4" />
}

export function RouteMap({ route, onStopClick }: RouteMapProps) {
  const center: [number, number] = [
    route.depotLocation.latitude,
    route.depotLocation.longitude,
  ]

  return (
    <MapContainer
      center={center}
      zoom={12}
      style={{ height: '100%', width: '100%', borderRadius: '0.5rem' }}
    >
      <TileLayer
        attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
        url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
      />

      <Marker position={center} icon={DEPOT_ICON}>
        <Popup>
          <strong>Ponto de partida</strong>
          <br />
          {route.name}
        </Popup>
      </Marker>

      {route.stops.map((stop) => (
        <Marker
          key={stop.id}
          position={[stop.location.latitude, stop.location.longitude]}
          icon={stopIcon(stop.sequenceNumber, stop.status)}
          eventHandlers={{ click: () => onStopClick?.(stop) }}
        >
          <Popup>
            <strong>#{stop.sequenceNumber} — {stop.order.customer.name}</strong>
            <br />
            {stop.order.orderCode}
            <br />
            {stop.estimatedArrival && `Previsão: ${stop.estimatedArrival}`}
          </Popup>
        </Marker>
      ))}

      {route.stops.length > 0 && (
        <RoutePolyline depot={route.depotLocation} stops={route.stops} />
      )}
    </MapContainer>
  )
}
