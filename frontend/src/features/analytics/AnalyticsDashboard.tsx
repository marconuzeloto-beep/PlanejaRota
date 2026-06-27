import { useQuery } from '@tanstack/react-query'
import { analyticsApi } from '@/shared/api/analyticsApi'

function KpiCard({ label, value, sub }: { label: string; value: string; sub?: string }) {
  return (
    <div className="bg-white rounded-xl shadow p-5 flex flex-col gap-1">
      <span className="text-xs font-medium text-gray-500 uppercase tracking-wide">{label}</span>
      <span className="text-2xl font-bold text-gray-900">{value}</span>
      {sub && <span className="text-xs text-gray-400">{sub}</span>}
    </div>
  )
}

export default function AnalyticsDashboard() {
  const { data: summary, isLoading: loadingSummary } = useQuery({
    queryKey: ['analytics', 'summary'],
    queryFn: analyticsApi.summary,
  })

  const { data: daily, isLoading: loadingDaily } = useQuery({
    queryKey: ['analytics', 'daily'],
    queryFn: () => analyticsApi.daily(30),
  })

  return (
    <div className="p-6 space-y-8">
      <h1 className="text-2xl font-bold text-gray-900">Analytics Dashboard</h1>

      {/* KPI Cards */}
      <section>
        <h2 className="text-sm font-semibold text-gray-600 uppercase mb-3">Overview</h2>
        {loadingSummary ? (
          <div className="text-gray-400 text-sm">Loading summary...</div>
        ) : !summary ? (
          <div className="text-gray-400 text-sm">No data available.</div>
        ) : (
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <KpiCard label="Total Routes" value={summary.totalRoutes.toLocaleString()} sub={`Last ${summary.periodDays} days`} />
            <KpiCard label="Total Distance" value={`${summary.totalDistanceKm.toFixed(0)} km`} />
            <KpiCard label="Avg Distance" value={`${summary.avgDistanceKm.toFixed(1)} km`} sub="per route" />
            <KpiCard label="Success Rate" value={`${(summary.successRate * 100).toFixed(1)}%`} />
            <KpiCard label="Orders Delivered" value={summary.totalOrdersDelivered.toLocaleString()} />
            <KpiCard label="Avg Duration" value={`${summary.avgDurationMinutes.toFixed(0)} min`} sub="per route" />
            <KpiCard label="Avg Capacity Use" value={`${summary.avgCapacityUsagePct.toFixed(1)}%`} />
            <KpiCard label="Total Cost" value={`R$ ${summary.totalCostBRL.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`} />
          </div>
        )}
      </section>

      {/* Daily Metrics Table */}
      <section>
        <h2 className="text-sm font-semibold text-gray-600 uppercase mb-3">Daily Metrics (Last 30 Days)</h2>
        {loadingDaily ? (
          <div className="text-gray-400 text-sm">Loading daily data...</div>
        ) : !daily || daily.length === 0 ? (
          <div className="text-gray-400 text-sm">No daily data available.</div>
        ) : (
          <div className="bg-white rounded-xl shadow overflow-x-auto">
            <table className="min-w-full text-sm">
              <thead>
                <tr className="border-b bg-gray-50">
                  <th className="px-4 py-3 text-left font-semibold text-gray-600">Date</th>
                  <th className="px-4 py-3 text-right font-semibold text-gray-600">Routes</th>
                  <th className="px-4 py-3 text-right font-semibold text-gray-600">Distance (km)</th>
                  <th className="px-4 py-3 text-right font-semibold text-gray-600">Avg Score</th>
                  <th className="px-4 py-3 text-right font-semibold text-gray-600">Orders</th>
                </tr>
              </thead>
              <tbody>
                {daily.map((row) => (
                  <tr key={row.date} className="border-b last:border-0 hover:bg-gray-50">
                    <td className="px-4 py-2 text-gray-700">{row.date}</td>
                    <td className="px-4 py-2 text-right text-gray-700">{row.routeCount}</td>
                    <td className="px-4 py-2 text-right text-gray-700">{row.totalDistanceKm.toFixed(1)}</td>
                    <td className="px-4 py-2 text-right text-gray-700">
                      {row.avgScore != null ? row.avgScore.toFixed(2) : '—'}
                    </td>
                    <td className="px-4 py-2 text-right text-gray-700">{row.orderCount}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  )
}
