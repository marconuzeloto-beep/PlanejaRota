import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { benchmarkApi, type BenchmarkEntry, type BenchmarkSummary } from '@/shared/api/benchmarkApi'

function pct(arr: number[], p: number) {
  if (arr.length === 0) return 0
  const sorted = [...arr].sort((a, b) => a - b)
  const idx = Math.ceil((p / 100) * sorted.length) - 1
  return sorted[Math.max(0, idx)]
}

function stdDev(arr: number[]) {
  if (arr.length < 2) return 0
  const mean = arr.reduce((s, v) => s + v, 0) / arr.length
  return Math.sqrt(arr.reduce((s, v) => s + (v - mean) ** 2, 0) / arr.length)
}

function buildStats(entries: BenchmarkEntry[]) {
  const times = entries.map((e) => e.executionTimeMs)
  const improvements = entries.filter((e) => e.twoOptApplied).map((e) => e.twoOptImprovementKm)
  return {
    count: entries.length,
    min: Math.min(...times),
    max: Math.max(...times),
    avg: times.reduce((s, v) => s + v, 0) / (times.length || 1),
    p95: pct(times, 95),
    p99: pct(times, 99),
    stddev: stdDev(times),
    avgImprovement: improvements.length
      ? improvements.reduce((s, v) => s + v, 0) / improvements.length
      : null,
  }
}

export default function BenchmarkEvolution() {
  const { data: ranking = [], isLoading: loadingRanking } = useQuery({
    queryKey: ['benchmark', 'ranking'],
    queryFn: benchmarkApi.ranking,
  })

  const strategies = ranking.map((r) => r.strategyIdentifier)
  const [selected, setSelected] = useState<string>('')

  const activeStrategy = selected || strategies[0] || ''

  const { data: history = [], isLoading: loadingHistory } = useQuery({
    queryKey: ['benchmark', 'history', activeStrategy],
    queryFn: () => benchmarkApi.historyByStrategy(activeStrategy, 50),
    enabled: !!activeStrategy,
  })

  const stats = buildStats(history)

  // Bar chart: last 20 entries by score
  const chartEntries = history.slice(-20)
  const maxScore = Math.max(...chartEntries.map((e) => e.routeScore ?? 0), 1)

  return (
    <div className="p-6 space-y-8">
      <h1 className="text-2xl font-bold text-gray-900">Benchmark Evolution</h1>

      {/* Strategy selector */}
      <div className="flex items-center gap-3">
        <label className="text-sm font-medium text-gray-700">Strategy:</label>
        {loadingRanking ? (
          <span className="text-sm text-gray-400">Loading...</span>
        ) : (
          <select
            value={activeStrategy}
            onChange={(e) => setSelected(e.target.value)}
            className="border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            {strategies.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>
        )}
      </div>

      {/* Score history chart */}
      {activeStrategy && (
        <section>
          <h2 className="text-sm font-semibold text-gray-600 uppercase mb-3">
            Score History — {activeStrategy}
          </h2>
          <div className="bg-white rounded-xl shadow p-5">
            {loadingHistory ? (
              <div className="text-sm text-gray-400">Loading history...</div>
            ) : chartEntries.length === 0 ? (
              <div className="text-sm text-gray-400">No history data for this strategy.</div>
            ) : (
              <div className="flex items-end gap-1 h-40">
                {chartEntries.map((entry, i) => {
                  const score = entry.routeScore ?? 0
                  const heightPct = (score / maxScore) * 100
                  return (
                    <div
                      key={entry.id}
                      className="flex-1 flex flex-col items-center gap-0.5 group relative"
                    >
                      <div className="absolute bottom-full mb-1 hidden group-hover:block bg-gray-800 text-white text-xs rounded px-2 py-1 whitespace-nowrap z-10">
                        Score: {score.toFixed(2)}<br />
                        {entry.executionTimeMs} ms<br />
                        {new Date(entry.createdAt).toLocaleDateString()}
                      </div>
                      <div
                        className="w-full rounded-t bg-blue-500 hover:bg-blue-600 transition-colors"
                        style={{ height: `${heightPct}%`, minHeight: '2px' }}
                      />
                    </div>
                  )
                })}
              </div>
            )}
            {chartEntries.length > 0 && (
              <div className="flex justify-between text-xs text-gray-400 mt-1">
                <span>Oldest</span>
                <span>Latest ({chartEntries.length} runs)</span>
              </div>
            )}
          </div>
        </section>
      )}

      {/* Per-strategy statistics */}
      <section>
        <h2 className="text-sm font-semibold text-gray-600 uppercase mb-3">Execution Time Statistics</h2>
        {activeStrategy && !loadingHistory && history.length > 0 ? (
          <div className="bg-white rounded-xl shadow overflow-x-auto">
            <table className="min-w-full text-sm">
              <thead>
                <tr className="bg-gray-50 border-b">
                  <th className="px-4 py-3 text-left font-semibold text-gray-600">Metric</th>
                  <th className="px-4 py-3 text-right font-semibold text-gray-600">Value</th>
                </tr>
              </thead>
              <tbody>
                {[
                  ['Runs', stats.count.toString()],
                  ['Min (ms)', stats.min.toFixed(1)],
                  ['Max (ms)', stats.max.toFixed(1)],
                  ['Avg (ms)', stats.avg.toFixed(1)],
                  ['p95 (ms)', stats.p95.toFixed(1)],
                  ['p99 (ms)', stats.p99.toFixed(1)],
                  ['Std Dev (ms)', stats.stddev.toFixed(1)],
                  ['Avg 2-opt Improvement (km)', stats.avgImprovement != null ? stats.avgImprovement.toFixed(3) : '—'],
                ].map(([label, val]) => (
                  <tr key={label} className="border-b last:border-0 hover:bg-gray-50">
                    <td className="px-4 py-2 text-gray-700">{label}</td>
                    <td className="px-4 py-2 text-right font-mono text-gray-900">{val}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          !loadingHistory && <div className="text-sm text-gray-400">Select a strategy to see statistics.</div>
        )}
      </section>

      {/* Ranking table */}
      <section>
        <h2 className="text-sm font-semibold text-gray-600 uppercase mb-3">All Strategies Ranking</h2>
        <div className="bg-white rounded-xl shadow overflow-x-auto">
          {loadingRanking ? (
            <div className="p-6 text-sm text-gray-400">Loading ranking...</div>
          ) : ranking.length === 0 ? (
            <div className="p-6 text-sm text-gray-400">No benchmark data yet.</div>
          ) : (
            <table className="min-w-full text-sm">
              <thead>
                <tr className="bg-gray-50 border-b">
                  <th className="px-4 py-3 text-left font-semibold text-gray-600">Strategy</th>
                  <th className="px-4 py-3 text-right font-semibold text-gray-600">Runs</th>
                  <th className="px-4 py-3 text-right font-semibold text-gray-600">Avg Time (ms)</th>
                  <th className="px-4 py-3 text-right font-semibold text-gray-600">Avg Distance (km)</th>
                  <th className="px-4 py-3 text-right font-semibold text-gray-600">Avg Score</th>
                  <th className="px-4 py-3 text-right font-semibold text-gray-600">Feasible</th>
                </tr>
              </thead>
              <tbody>
                {ranking.map((r: BenchmarkSummary) => (
                  <tr
                    key={r.strategyIdentifier}
                    onClick={() => setSelected(r.strategyIdentifier)}
                    className={`border-b last:border-0 cursor-pointer hover:bg-blue-50 ${
                      activeStrategy === r.strategyIdentifier ? 'bg-blue-50' : ''
                    }`}
                  >
                    <td className="px-4 py-2 font-medium text-gray-900">{r.strategyIdentifier}</td>
                    <td className="px-4 py-2 text-right text-gray-700">{r.executionCount}</td>
                    <td className="px-4 py-2 text-right text-gray-700">{r.avgExecutionTimeMs.toFixed(1)}</td>
                    <td className="px-4 py-2 text-right text-gray-700">{r.avgDistanceKm.toFixed(2)}</td>
                    <td className="px-4 py-2 text-right text-gray-700">{r.avgScore.toFixed(2)}</td>
                    <td className="px-4 py-2 text-right text-gray-700">{r.feasibleCount}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </section>
    </div>
  )
}
