import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { routeApi, type CreateRouteDto } from '../api/routeApi'

const ROUTES_KEY = ['routes'] as const

export function useRoutes() {
  return useQuery({
    queryKey: ROUTES_KEY,
    queryFn: routeApi.getAll,
  })
}

export function useRoute(id: string) {
  return useQuery({
    queryKey: [...ROUTES_KEY, id],
    queryFn: () => routeApi.getById(id),
    enabled: !!id,
  })
}

export function useCreateRoute() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (dto: CreateRouteDto) => routeApi.create(dto),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ROUTES_KEY }),
  })
}

export function useOptimizeRoute() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: (routeId: string) => routeApi.optimize(routeId),
    onSuccess: (_, routeId) => {
      queryClient.invalidateQueries({ queryKey: [...ROUTES_KEY, routeId] })
    },
  })
}
