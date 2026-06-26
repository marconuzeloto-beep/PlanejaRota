import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { AuthUser, MapRouteDTO, ScenarioComparison } from '@/shared/types/domain'

interface AppStore {
  // Auth
  user: AuthUser | null
  setUser: (user: AuthUser | null) => void
  logout: () => void

  // Planning state
  selectedRoute: MapRouteDTO | null
  setSelectedRoute: (route: MapRouteDTO | null) => void

  // Simulation state
  comparison: ScenarioComparison | null
  setComparison: (c: ScenarioComparison | null) => void
  selectedScenarioIdentifier: string | null
  selectScenario: (identifier: string) => void
}

export const useAppStore = create<AppStore>()(
  persist(
    (set) => ({
      user: null,
      setUser: (user) => set({ user }),
      logout: () => {
        localStorage.removeItem('access_token')
        set({ user: null, selectedRoute: null, comparison: null })
      },

      selectedRoute: null,
      setSelectedRoute: (route) => set({ selectedRoute: route }),

      comparison: null,
      setComparison: (comparison) => set({ comparison, selectedScenarioIdentifier: null }),
      selectedScenarioIdentifier: null,
      selectScenario: (identifier) => set({ selectedScenarioIdentifier: identifier }),
    }),
    {
      name: 'logicore-app',
      partialize: (s) => ({ user: s.user }),
    },
  ),
)

export const useIsAuthenticated = () => useAppStore((s) => !!s.user)
export const useCurrentUser = () => useAppStore((s) => s.user)
