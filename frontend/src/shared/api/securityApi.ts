import { apiClient } from './client'

export interface ApiKey {
  id: string
  name: string
  keyPrefix: string
  active: boolean
  createdAt: string
  lastUsedAt: string | null
  expiresAt: string | null
}

export interface CreateApiKeyRequest {
  name: string
  expiresInDays?: number
}

export interface CreateApiKeyResponse {
  id: string
  name: string
  rawKey: string
  expiresAt: string | null
}

export interface AuditEntry {
  id: string
  userId: string
  action: string
  entityType: string
  entityId: string
  details: string
  ipAddress: string
  createdAt: string
}

export interface PagedAuditResponse {
  content: AuditEntry[]
  totalElements: number
  totalPages: number
  number: number
}

export const securityApi = {
  async listKeys(): Promise<ApiKey[]> {
    const { data } = await apiClient.get<ApiKey[]>('/api-keys')
    return data
  },

  async createKey(req: CreateApiKeyRequest): Promise<CreateApiKeyResponse> {
    const { data } = await apiClient.post<CreateApiKeyResponse>('/api-keys', req)
    return data
  },

  async deleteKey(id: string): Promise<void> {
    await apiClient.delete(`/api-keys/${id}`)
  },

  async auditLog(page = 0, size = 20): Promise<PagedAuditResponse> {
    const { data } = await apiClient.get<PagedAuditResponse>(`/audit?page=${page}&size=${size}`)
    return data
  },
}
