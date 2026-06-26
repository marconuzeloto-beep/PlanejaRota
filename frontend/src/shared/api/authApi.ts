import { apiClient } from './client'
import type { AuthUser } from '@/shared/types/domain'

export const authApi = {
  async login(slug: string, email: string, password: string): Promise<AuthUser> {
    const { data } = await apiClient.post<AuthUser>('/auth/login', { slug, email, password })
    localStorage.setItem('access_token', data.accessToken)
    return data
  },

  async register(payload: {
    organizationName: string
    slug: string
    adminName: string
    adminEmail: string
    adminPassword: string
  }) {
    const { data } = await apiClient.post('/auth/register', payload)
    return data
  },
}
