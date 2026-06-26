import { Navigate, Outlet } from 'react-router-dom'
import { useIsAuthenticated } from '@/shared/store/appStore'

export function AuthGuard() {
  const authenticated = useIsAuthenticated()
  if (!authenticated) return <Navigate to="/login" replace />
  return <Outlet />
}
