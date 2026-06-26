import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { customerApi } from '@/shared/api/customerApi'
import { vehicleApi } from '@/shared/api/vehicleApi'
import { orderApi } from '@/shared/api/orderApi'
import { useCurrentUser } from '@/shared/store/appStore'

function StatCard({ icon, label, value, to }: {
  icon: string; label: string; value?: number; to: string
}) {
  return (
    <Link to={to} className="card p-5 hover:shadow-md transition-shadow flex items-center gap-4">
      <div className="text-3xl">{icon}</div>
      <div>
        <p className="text-2xl font-bold text-gray-900">{value ?? '—'}</p>
        <p className="text-sm text-gray-500">{label}</p>
      </div>
    </Link>
  )
}

export default function DashboardPage() {
  const user = useCurrentUser()
  const { data: customers } = useQuery({ queryKey: ['customers'], queryFn: customerApi.list })
  const { data: vehicles }  = useQuery({ queryKey: ['vehicles'],  queryFn: vehicleApi.list })
  const { data: orders }    = useQuery({ queryKey: ['orders'],    queryFn: orderApi.list })

  const availableVehicles = vehicles?.filter((v) => v.status === 'AVAILABLE').length
  const pendingOrders     = orders?.filter((o) => o.status === 'PENDING').length

  return (
    <div className="p-6 max-w-5xl mx-auto">
      <div className="mb-8">
        <h1 className="text-2xl font-bold text-gray-900">Bom dia! 👋</h1>
        <p className="text-gray-500 text-sm mt-1">
          Você está conectado como <span className="font-medium">{user?.role}</span>
        </p>
      </div>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <StatCard icon="👤" label="Clientes"         value={customers?.length} to="/customers" />
        <StatCard icon="🚚" label="Veículos"          value={vehicles?.length}  to="/vehicles" />
        <StatCard icon="📦" label="Pedidos pendentes" value={pendingOrders}     to="/orders" />
        <StatCard icon="✅" label="Disponíveis"       value={availableVehicles} to="/vehicles" />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <ActionCard
          icon="🔬"
          title="Simular Rotas"
          desc="Compare múltiplas estratégias de otimização e escolha a melhor para sua operação."
          to="/simulate"
          cta="Ir para Simulação"
        />
        <ActionCard
          icon="🗺️"
          title="Planejar Rota"
          desc="Gere uma rota otimizada e visualize cada decisão do algoritmo no mapa."
          to="/plan"
          cta="Ir para Planejamento"
        />
      </div>
    </div>
  )
}

function ActionCard({ icon, title, desc, to, cta }: {
  icon: string; title: string; desc: string; to: string; cta: string
}) {
  return (
    <div className="card p-6">
      <div className="text-3xl mb-3">{icon}</div>
      <h2 className="font-semibold text-gray-900 text-lg">{title}</h2>
      <p className="text-gray-500 text-sm mt-1 mb-4">{desc}</p>
      <Link to={to} className="btn-primary inline-flex">{cta} →</Link>
    </div>
  )
}
