import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { benchmarkApi, type BenchmarkSummary, type BenchmarkEntry } from '@/shared/api/benchmarkApi'

const GRADE_COLORS: Record<string, string> = {
  A: 'bg-emerald-100 text-emerald-800',
  B: 'bg-blue-100 text-blue-800',
  C: 'bg-yellow-100 text-yellow-800',
  D: 'bg-orange-100 text-orange-800',
  F: 'bg-red-100 text-red-800',
}

const STRATEGY_LABELS: Record<string, string> = {
  SHORTEST_DISTANCE_V1:  'Nearest Neighbor',
  PRIORITY_FIRST_V1:     'Priority First',
  HYBRID_V1:             'Hybrid',
  CLARKE_WRIGHT_V1:      'Clarke-Wright',
  A_STAR_V1:             'A*',
  TWO_OPT_V1:            '2-Opt',
  SIMULATED_ANNEALING_V1:'Simulated Annealing',
  TABU_SEARCH_V1:        'Tabu Search',
  GENETIC_ALGORITHM_V1:  'Genetic Algorithm',
  ANT_COLONY_V1:         'Ant Colony Optimization',
}

function ScoreBar({ value }: { value: number }) {
  const pct = Math.round(value * 100)
  const color = pct >= 85 ? 'bg-emerald-500' : pct >= 70 ? 'bg-blue-500' : pct >= 55 ? 'bg-yellow-500' : 'bg-red-500'
  return (
    <div className="flex items-center gap-2">
      <div className="flex-1 bg-gray-100 rounded-full h-2">
        <div className={`h-2 rounded-full ${color}`} style={{ width: `${pct}%` }} />
      </div>
      <span className="text-xs text-gray-600 w-8 text-right">{pct}%</span>
    </div>
  )
}

function RankingTable({ summaries }: { summaries: BenchmarkSummary[] }) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full text-sm">
        <thead>
          <tr className="border-b border-gray-200 text-left text-gray-500 text-xs uppercase tracking-wider">
            <th className="pb-3 pr-4">#</th>
            <th className="pb-3 pr-4">Algoritmo</th>
            <th className="pb-3 pr-4">Execuções</th>
            <th className="pb-3 pr-4">Score médio</th>
            <th className="pb-3 pr-4">Distância média</th>
            <th className="pb-3 pr-4">Tempo médio</th>
            <th className="pb-3 pr-4">Tempo máximo</th>
            <th className="pb-3">Viáveis</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-100">
          {summaries.map((s, i) => (
            <tr key={s.strategyIdentifier} className="hover:bg-gray-50 transition-colors">
              <td className="py-3 pr-4">
                <span className={`inline-flex items-center justify-center w-6 h-6 rounded-full text-xs font-bold ${
                  i === 0 ? 'bg-amber-400 text-white' :
                  i === 1 ? 'bg-gray-300 text-gray-700' :
                  i === 2 ? 'bg-orange-300 text-white' :
                  'bg-gray-100 text-gray-500'
                }`}>{i + 1}</span>
              </td>
              <td className="py-3 pr-4 font-medium text-gray-900">
                {STRATEGY_LABELS[s.strategyIdentifier] ?? s.strategyIdentifier}
                <div className="text-xs text-gray-400 font-normal">{s.strategyType.replace(/_/g, ' ')}</div>
              </td>
              <td className="py-3 pr-4 text-gray-600">{s.executionCount}</td>
              <td className="py-3 pr-4 w-32">
                <ScoreBar value={s.avgScore} />
              </td>
              <td className="py-3 pr-4 text-gray-600">{s.avgDistanceKm.toFixed(1)} km</td>
              <td className="py-3 pr-4 text-gray-600">{s.avgExecutionTimeMs.toFixed(0)} ms</td>
              <td className="py-3 pr-4 text-gray-600">{s.maxExecutionTimeMs.toFixed(0)} ms</td>
              <td className="py-3">
                <span className={`inline-block px-2 py-0.5 rounded text-xs font-medium ${
                  s.feasibleCount === s.executionCount ? 'bg-emerald-100 text-emerald-700' : 'bg-yellow-100 text-yellow-700'
                }`}>
                  {s.feasibleCount}/{s.executionCount}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

function HistoryTable({ entries }: { entries: BenchmarkEntry[] }) {
  return (
    <div className="overflow-x-auto">
      <table className="w-full text-sm">
        <thead>
          <tr className="border-b border-gray-200 text-left text-gray-500 text-xs uppercase tracking-wider">
            <th className="pb-3 pr-4">Algoritmo</th>
            <th className="pb-3 pr-4">Pedidos</th>
            <th className="pb-3 pr-4">Score</th>
            <th className="pb-3 pr-4">Distância</th>
            <th className="pb-3 pr-4">Tempo exec.</th>
            <th className="pb-3 pr-4">2-Opt</th>
            <th className="pb-3 pr-4">Viável</th>
            <th className="pb-3">Data</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-100">
          {entries.map(e => (
            <tr key={e.id} className="hover:bg-gray-50 transition-colors">
              <td className="py-2 pr-4 font-medium text-gray-900 text-xs">
                {STRATEGY_LABELS[e.strategyIdentifier] ?? e.strategyIdentifier}
              </td>
              <td className="py-2 pr-4 text-gray-600">{e.orderCount}</td>
              <td className="py-2 pr-4">
                {e.scoreGrade ? (
                  <span className={`inline-block px-2 py-0.5 rounded text-xs font-bold ${GRADE_COLORS[e.scoreGrade] ?? 'bg-gray-100'}`}>
                    {e.scoreGrade} {e.routeScore ? `(${(e.routeScore * 100).toFixed(0)}%)` : ''}
                  </span>
                ) : '—'}
              </td>
              <td className="py-2 pr-4 text-gray-600">{e.totalDistanceKm.toFixed(1)} km</td>
              <td className="py-2 pr-4 text-gray-600">{e.executionTimeMs} ms</td>
              <td className="py-2 pr-4">
                {e.twoOptApplied ? (
                  <span className="text-emerald-600 text-xs">-{e.twoOptImprovementKm.toFixed(2)} km</span>
                ) : <span className="text-gray-300 text-xs">—</span>}
              </td>
              <td className="py-2 pr-4">
                <span className={`text-xs ${e.feasible ? 'text-emerald-600' : 'text-red-500'}`}>
                  {e.feasible ? 'Sim' : 'Não'}
                </span>
              </td>
              <td className="py-2 text-gray-400 text-xs">
                {new Date(e.createdAt).toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' })}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

export default function BenchmarkDashboard() {
  const [tab, setTab] = useState<'ranking' | 'history'>('ranking')

  const { data: ranking, isLoading: rankingLoading } = useQuery({
    queryKey: ['benchmark-ranking'],
    queryFn: () => benchmarkApi.ranking(),
  })

  const { data: history, isLoading: historyLoading } = useQuery({
    queryKey: ['benchmark-history'],
    queryFn: () => benchmarkApi.history(100),
    enabled: tab === 'history',
  })

  const topStrategy = ranking?.[0]
  const fastestStrategy = ranking?.slice().sort((a, b) => a.avgExecutionTimeMs - b.avgExecutionTimeMs)[0]

  return (
    <div className="max-w-7xl mx-auto p-6 space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Benchmark Dashboard</h1>
        <p className="text-gray-500 mt-1">Comparação histórica de algoritmos de otimização</p>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="bg-white border border-gray-200 rounded-xl p-4">
          <div className="text-xs text-gray-500 uppercase tracking-wider mb-1">Algoritmos ativos</div>
          <div className="text-2xl font-bold text-gray-900">{ranking?.length ?? '—'}</div>
        </div>
        <div className="bg-white border border-gray-200 rounded-xl p-4">
          <div className="text-xs text-gray-500 uppercase tracking-wider mb-1">Melhor score</div>
          <div className="text-2xl font-bold text-emerald-600">
            {topStrategy ? `${(topStrategy.avgScore * 100).toFixed(0)}%` : '—'}
          </div>
          <div className="text-xs text-gray-400 truncate">
            {topStrategy ? (STRATEGY_LABELS[topStrategy.strategyIdentifier] ?? topStrategy.strategyIdentifier) : ''}
          </div>
        </div>
        <div className="bg-white border border-gray-200 rounded-xl p-4">
          <div className="text-xs text-gray-500 uppercase tracking-wider mb-1">Mais rápido</div>
          <div className="text-2xl font-bold text-blue-600">
            {fastestStrategy ? `${fastestStrategy.avgExecutionTimeMs.toFixed(0)}ms` : '—'}
          </div>
          <div className="text-xs text-gray-400 truncate">
            {fastestStrategy ? (STRATEGY_LABELS[fastestStrategy.strategyIdentifier] ?? fastestStrategy.strategyIdentifier) : ''}
          </div>
        </div>
        <div className="bg-white border border-gray-200 rounded-xl p-4">
          <div className="text-xs text-gray-500 uppercase tracking-wider mb-1">Total execuções</div>
          <div className="text-2xl font-bold text-gray-900">
            {ranking?.reduce((s, r) => s + r.executionCount, 0) ?? '—'}
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="bg-white border border-gray-200 rounded-xl overflow-hidden">
        <div className="border-b border-gray-200 flex">
          {(['ranking', 'history'] as const).map(t => (
            <button
              key={t}
              onClick={() => setTab(t)}
              className={`px-6 py-3 text-sm font-medium transition-colors ${
                tab === t
                  ? 'border-b-2 border-blue-600 text-blue-600 bg-blue-50'
                  : 'text-gray-500 hover:text-gray-700 hover:bg-gray-50'
              }`}
            >
              {t === 'ranking' ? 'Ranking de Algoritmos' : 'Histórico de Execuções'}
            </button>
          ))}
        </div>

        <div className="p-6">
          {tab === 'ranking' && (
            rankingLoading ? (
              <div className="text-center py-12 text-gray-400">Carregando ranking...</div>
            ) : !ranking || ranking.length === 0 ? (
              <div className="text-center py-12">
                <div className="text-4xl mb-3">🏁</div>
                <p className="text-gray-500 font-medium">Nenhum benchmark executado ainda</p>
                <p className="text-gray-400 text-sm mt-1">
                  Use POST /api/v1/benchmark/run para executar o primeiro benchmark
                </p>
              </div>
            ) : (
              <RankingTable summaries={ranking} />
            )
          )}

          {tab === 'history' && (
            historyLoading ? (
              <div className="text-center py-12 text-gray-400">Carregando histórico...</div>
            ) : !history || history.length === 0 ? (
              <div className="text-center py-12">
                <div className="text-4xl mb-3">📊</div>
                <p className="text-gray-500 font-medium">Sem histórico de execuções</p>
              </div>
            ) : (
              <HistoryTable entries={history} />
            )
          )}
        </div>
      </div>

      {/* Complexity Reference */}
      <div className="bg-white border border-gray-200 rounded-xl p-6">
        <h2 className="text-sm font-semibold text-gray-700 mb-4">Referência de Complexidade</h2>
        <div className="overflow-x-auto">
          <table className="w-full text-xs">
            <thead>
              <tr className="text-gray-400 border-b border-gray-100">
                <th className="pb-2 text-left pr-4">Algoritmo</th>
                <th className="pb-2 text-left pr-4">Complexidade</th>
                <th className="pb-2 text-left pr-4">Tipo</th>
                <th className="pb-2 text-left">Caso de uso ideal</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50 text-gray-600">
              {[
                ['Nearest Neighbor', 'O(n²)', 'Construtivo', 'Solução rápida, n < 100'],
                ['Clarke-Wright', 'O(n² log n)', 'Construtivo', 'Múltiplas rotas, alta ocupação'],
                ['A*', 'O(n²)', 'Construtivo', 'Instâncias com clusters naturais'],
                ['2-Opt', 'O(n² × k)', 'Melhoria local', 'Refinamento pós-construtivo'],
                ['Simulated Annealing', 'O(n × iter)', 'Metaheurística', 'Equilíbrio qualidade/tempo'],
                ['Tabu Search', 'O(n² × iter)', 'Metaheurística', 'Alta qualidade, n < 500'],
                ['Genetic Algorithm', 'O(pop × gen × n)', 'Evolucionário', 'Instâncias grandes, paralelo'],
                ['Ant Colony', 'O(ants × n² × iter)', 'Bioinspirado', 'Problemas com estrutura de cluster'],
              ].map(([name, complexity, type, useCase]) => (
                <tr key={name}>
                  <td className="py-2 pr-4 font-medium text-gray-700">{name}</td>
                  <td className="py-2 pr-4 font-mono text-purple-600">{complexity}</td>
                  <td className="py-2 pr-4">{type}</td>
                  <td className="py-2 text-gray-500">{useCase}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
