import { NavLink, Outlet } from 'react-router-dom'
import { useAppStore } from '@/shared/store/appStore'
import { clsx } from 'clsx'

const NAV = [
  { to: '/',           label: 'Dashboard',   icon: '📊' },
  { to: '/simulate',   label: 'Simulação',   icon: '🔬' },
  { to: '/plan',       label: 'Planejamento', icon: '🗺️' },
  { to: '/customers',  label: 'Clientes',    icon: '👤' },
  { to: '/vehicles',   label: 'Veículos',    icon: '🚚' },
  { to: '/orders',     label: 'Pedidos',     icon: '📦' },
]

export function AppLayout() {
  const { user, logout } = useAppStore()

  return (
    <div className="flex h-screen overflow-hidden bg-gray-50">
      {/* Sidebar */}
      <nav className="w-56 bg-brand-900 flex flex-col shrink-0">
        <div className="px-5 py-5 border-b border-brand-800">
          <div className="text-white font-bold text-lg leading-tight">LogiCore</div>
          <div className="text-brand-300 text-xs mt-0.5">Decisão Logística</div>
        </div>

        <ul className="flex-1 py-3 space-y-0.5 px-2">
          {NAV.map(({ to, label, icon }) => (
            <li key={to}>
              <NavLink
                to={to}
                end={to === '/'}
                className={({ isActive }) => clsx(
                  'flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors',
                  isActive
                    ? 'bg-brand-700 text-white font-medium'
                    : 'text-brand-200 hover:bg-brand-800 hover:text-white',
                )}
              >
                <span>{icon}</span>
                <span>{label}</span>
              </NavLink>
            </li>
          ))}
        </ul>

        <div className="px-4 py-4 border-t border-brand-800">
          <div className="text-brand-300 text-xs truncate mb-2">{user?.role}</div>
          <button
            onClick={logout}
            className="text-brand-300 hover:text-white text-xs transition-colors"
          >
            Sair →
          </button>
        </div>
      </nav>

      {/* Main */}
      <main className="flex-1 overflow-auto">
        <Outlet />
      </main>
    </div>
  )
}
