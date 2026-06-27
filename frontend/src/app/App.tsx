import { lazy, Suspense } from 'react'
import { createBrowserRouter, RouterProvider, Navigate } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { AuthGuard } from './AuthGuard'
import { AppLayout } from './AppLayout'
import { PageLoader } from '@/shared/components/Spinner'

const LoginPage         = lazy(() => import('@/features/auth/LoginPage'))
const DashboardPage     = lazy(() => import('@/features/dashboard/DashboardPage'))
const CustomersPage     = lazy(() => import('@/features/customers/CustomersPage'))
const VehiclesPage      = lazy(() => import('@/features/vehicles/VehiclesPage'))
const OrdersPage        = lazy(() => import('@/features/orders/OrdersPage'))
const RoutePlanningPage = lazy(() => import('@/features/route-planning/components/RoutePlanningPage'))
const SimulationDashboard  = lazy(() => import('@/features/simulation/SimulationDashboard'))
const BenchmarkDashboard   = lazy(() => import('@/features/benchmark/BenchmarkDashboard'))
const AnalyticsDashboard   = lazy(() => import('@/features/analytics/AnalyticsDashboard'))
const LiveMap              = lazy(() => import('@/features/execution/LiveMap'))
const StrategyAdvisorPage  = lazy(() => import('@/features/advisor/StrategyAdvisorPage'))
const ApiKeysPage          = lazy(() => import('@/features/security/ApiKeysPage'))
const BenchmarkEvolution   = lazy(() => import('@/features/benchmark/BenchmarkEvolution'))

const queryClient = new QueryClient({
  defaultOptions: { queries: { staleTime: 60_000, retry: 1 } },
})

const router = createBrowserRouter([
  {
    path: '/login',
    element: (
      <Suspense fallback={<PageLoader />}>
        <LoginPage />
      </Suspense>
    ),
  },
  {
    element: <AuthGuard />,
    children: [
      {
        element: <AppLayout />,
        children: [
          {
            path: '/',
            element: (
              <Suspense fallback={<PageLoader />}>
                <DashboardPage />
              </Suspense>
            ),
          },
          {
            path: '/simulate',
            element: (
              <Suspense fallback={<PageLoader />}>
                <SimulationDashboard />
              </Suspense>
            ),
          },
          {
            path: '/plan',
            element: (
              <Suspense fallback={<PageLoader />}>
                <RoutePlanningPage />
              </Suspense>
            ),
          },
          {
            path: '/customers',
            element: (
              <Suspense fallback={<PageLoader />}>
                <CustomersPage />
              </Suspense>
            ),
          },
          {
            path: '/vehicles',
            element: (
              <Suspense fallback={<PageLoader />}>
                <VehiclesPage />
              </Suspense>
            ),
          },
          {
            path: '/orders',
            element: (
              <Suspense fallback={<PageLoader />}>
                <OrdersPage />
              </Suspense>
            ),
          },
          {
            path: '/benchmark',
            element: (
              <Suspense fallback={<PageLoader />}>
                <BenchmarkDashboard />
              </Suspense>
            ),
          },
          {
            path: '/analytics',
            element: (
              <Suspense fallback={<PageLoader />}>
                <AnalyticsDashboard />
              </Suspense>
            ),
          },
          {
            path: '/live',
            element: (
              <Suspense fallback={<PageLoader />}>
                <LiveMap />
              </Suspense>
            ),
          },
          {
            path: '/advisor',
            element: (
              <Suspense fallback={<PageLoader />}>
                <StrategyAdvisorPage />
              </Suspense>
            ),
          },
          {
            path: '/api-keys',
            element: (
              <Suspense fallback={<PageLoader />}>
                <ApiKeysPage />
              </Suspense>
            ),
          },
          {
            path: '/benchmark/evolution',
            element: (
              <Suspense fallback={<PageLoader />}>
                <BenchmarkEvolution />
              </Suspense>
            ),
          },
        ],
      },
    ],
  },
  { path: '*', element: <Navigate to="/" replace /> },
])

export function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  )
}
