import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { RouterProvider, createBrowserRouter } from 'react-router-dom'
import { Suspense, lazy } from 'react'

const RoutePlanningPage = lazy(() => import('@/features/route-planning/components/RoutePlanningPage'))

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 60_000,
      retry: 1,
    },
  },
})

const router = createBrowserRouter([
  {
    path: '/',
    element: <div>Home — em construção</div>,
  },
  {
    path: '/routes',
    element: (
      <Suspense fallback={<div className="flex h-screen items-center justify-center">Carregando...</div>}>
        <RoutePlanningPage />
      </Suspense>
    ),
  },
])

export function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
    </QueryClientProvider>
  )
}
