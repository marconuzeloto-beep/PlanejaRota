import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { apiClient } from '@/shared/api/client'

type TargetSla = 'FAST' | 'BALANCED' | 'QUALITY'

interface AdvisorRequest {
  orderCount: number
  hasTimeWindows: boolean
  hasPriorityOrders: boolean
  targetSla: TargetSla
}

interface AdvisorAlternative {
  identifier: string
  name: string
  tradeoff: string
}

interface AdvisorResponse {
  recommendedIdentifier: string
  recommendedName: string
  reason: string
  alternatives: AdvisorAlternative[]
  estimatedTimeMs: number
  qualityGrade: string
}

async function fetchRecommendation(req: AdvisorRequest): Promise<AdvisorResponse> {
  const { data } = await apiClient.post<AdvisorResponse>('/advisor/recommend', req)
  return data
}

const COMPLEXITY_TABLE = [
  { orders: '1–20', complexity: 'Low', strategies: 'NEAREST_NEIGHBOR, GREEDY', notes: 'Sub-millisecond' },
  { orders: '21–100', complexity: 'Medium', strategies: 'SAVINGS, SWEEP', notes: '<50 ms' },
  { orders: '101–500', complexity: 'High', strategies: 'SAVINGS + 2-opt', notes: '<500 ms' },
  { orders: '500+', complexity: 'Very High', strategies: 'GENETIC (async)', notes: 'Seconds' },
]

export default function StrategyAdvisorPage() {
  const [form, setForm] = useState<AdvisorRequest>({
    orderCount: 50,
    hasTimeWindows: false,
    hasPriorityOrders: false,
    targetSla: 'BALANCED',
  })

  const mutation = useMutation({
    mutationFn: fetchRecommendation,
  })

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    mutation.mutate(form)
  }

  return (
    <div className="p-6 max-w-3xl mx-auto space-y-8">
      <h1 className="text-2xl font-bold text-gray-900">Strategy Advisor</h1>
      <p className="text-gray-500 text-sm -mt-6">
        Describe your scenario and get a recommended routing strategy.
      </p>

      {/* Form */}
      <form onSubmit={handleSubmit} className="bg-white rounded-xl shadow p-6 space-y-5">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Number of Orders
          </label>
          <input
            type="number"
            min={1}
            max={10000}
            value={form.orderCount}
            onChange={(e) => setForm((f) => ({ ...f, orderCount: Number(e.target.value) }))}
            className="border rounded-lg px-3 py-2 w-40 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            required
          />
        </div>

        <div className="flex gap-6">
          <label className="flex items-center gap-2 text-sm text-gray-700 cursor-pointer">
            <input
              type="checkbox"
              checked={form.hasTimeWindows}
              onChange={(e) => setForm((f) => ({ ...f, hasTimeWindows: e.target.checked }))}
              className="accent-blue-600 w-4 h-4"
            />
            Has Time Windows
          </label>
          <label className="flex items-center gap-2 text-sm text-gray-700 cursor-pointer">
            <input
              type="checkbox"
              checked={form.hasPriorityOrders}
              onChange={(e) => setForm((f) => ({ ...f, hasPriorityOrders: e.target.checked }))}
              className="accent-blue-600 w-4 h-4"
            />
            Has Priority Orders
          </label>
        </div>

        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Target SLA</label>
          <select
            value={form.targetSla}
            onChange={(e) => setForm((f) => ({ ...f, targetSla: e.target.value as TargetSla }))}
            className="border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="FAST">Fast (speed over quality)</option>
            <option value="BALANCED">Balanced</option>
            <option value="QUALITY">Quality (best routes, slower)</option>
          </select>
        </div>

        <button
          type="submit"
          disabled={mutation.isPending}
          className="bg-blue-600 text-white px-5 py-2 rounded-lg text-sm font-semibold hover:bg-blue-700 disabled:opacity-50 transition-colors"
        >
          {mutation.isPending ? 'Analyzing...' : 'Get Recommendation'}
        </button>

        {mutation.isError && (
          <div className="text-red-600 text-sm">Failed to get recommendation. Please try again.</div>
        )}
      </form>

      {/* Recommendation Result */}
      {mutation.data && (
        <div className="space-y-4">
          <div className="bg-blue-50 border border-blue-200 rounded-xl p-6">
            <div className="flex items-center justify-between mb-2">
              <h2 className="text-lg font-bold text-blue-900">Recommended Strategy</h2>
              <span className="bg-blue-600 text-white text-xs font-bold px-3 py-1 rounded-full">
                Grade: {mutation.data.qualityGrade}
              </span>
            </div>
            <div className="text-blue-800 font-semibold text-xl mb-1">
              {mutation.data.recommendedName}
            </div>
            <div className="text-xs text-blue-600 mb-3">{mutation.data.recommendedIdentifier}</div>
            <p className="text-blue-700 text-sm leading-relaxed">{mutation.data.reason}</p>
            <div className="mt-3 text-xs text-blue-500">
              Estimated execution time: ~{mutation.data.estimatedTimeMs} ms
            </div>
          </div>

          {mutation.data.alternatives.length > 0 && (
            <div className="bg-white rounded-xl shadow p-5">
              <h3 className="text-sm font-semibold text-gray-700 mb-3">Alternatives</h3>
              <ul className="space-y-2">
                {mutation.data.alternatives.map((alt) => (
                  <li key={alt.identifier} className="flex items-start gap-3 text-sm">
                    <span className="bg-gray-100 text-gray-700 font-mono text-xs px-2 py-1 rounded mt-0.5 whitespace-nowrap">
                      {alt.name}
                    </span>
                    <span className="text-gray-600">{alt.tradeoff}</span>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>
      )}

      {/* Complexity Reference Table */}
      <section>
        <h2 className="text-sm font-semibold text-gray-600 uppercase mb-3">Complexity Reference</h2>
        <div className="bg-white rounded-xl shadow overflow-hidden">
          <table className="min-w-full text-sm">
            <thead>
              <tr className="bg-gray-50 border-b">
                <th className="px-4 py-3 text-left font-semibold text-gray-600">Order Count</th>
                <th className="px-4 py-3 text-left font-semibold text-gray-600">Complexity</th>
                <th className="px-4 py-3 text-left font-semibold text-gray-600">Suggested Strategies</th>
                <th className="px-4 py-3 text-left font-semibold text-gray-600">Typical Time</th>
              </tr>
            </thead>
            <tbody>
              {COMPLEXITY_TABLE.map((row) => (
                <tr key={row.orders} className="border-b last:border-0 hover:bg-gray-50">
                  <td className="px-4 py-2 font-mono text-gray-700">{row.orders}</td>
                  <td className="px-4 py-2 text-gray-700">{row.complexity}</td>
                  <td className="px-4 py-2 text-gray-600">{row.strategies}</td>
                  <td className="px-4 py-2 text-gray-500">{row.notes}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  )
}
