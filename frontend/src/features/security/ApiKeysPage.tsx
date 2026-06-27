import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { securityApi, type CreateApiKeyResponse } from '@/shared/api/securityApi'

function formatDate(d: string | null) {
  if (!d) return '—'
  return new Date(d).toLocaleString()
}

export default function ApiKeysPage() {
  const qc = useQueryClient()
  const [showCreate, setShowCreate] = useState(false)
  const [newName, setNewName] = useState('')
  const [expiresInDays, setExpiresInDays] = useState('')
  const [createdKey, setCreatedKey] = useState<CreateApiKeyResponse | null>(null)
  const [copied, setCopied] = useState(false)

  const { data: keys = [], isLoading: loadingKeys } = useQuery({
    queryKey: ['api-keys'],
    queryFn: securityApi.listKeys,
  })

  const { data: audit, isLoading: loadingAudit } = useQuery({
    queryKey: ['audit'],
    queryFn: () => securityApi.auditLog(0, 20),
  })

  const createMutation = useMutation({
    mutationFn: securityApi.createKey,
    onSuccess: (data) => {
      qc.invalidateQueries({ queryKey: ['api-keys'] })
      setCreatedKey(data)
      setShowCreate(false)
      setNewName('')
      setExpiresInDays('')
    },
  })

  const deleteMutation = useMutation({
    mutationFn: securityApi.deleteKey,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['api-keys'] }),
  })

  function handleCreate(e: React.FormEvent) {
    e.preventDefault()
    createMutation.mutate({
      name: newName,
      expiresInDays: expiresInDays ? Number(expiresInDays) : undefined,
    })
  }

  function handleCopy() {
    if (createdKey) {
      navigator.clipboard.writeText(createdKey.rawKey)
      setCopied(true)
      setTimeout(() => setCopied(false), 2000)
    }
  }

  return (
    <div className="p-6 space-y-8 max-w-5xl mx-auto">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">API Keys</h1>
        <button
          onClick={() => setShowCreate(true)}
          className="bg-blue-600 text-white px-4 py-2 rounded-lg text-sm font-semibold hover:bg-blue-700 transition-colors"
        >
          + Create Key
        </button>
      </div>

      {/* Keys Table */}
      <div className="bg-white rounded-xl shadow overflow-x-auto">
        {loadingKeys ? (
          <div className="p-6 text-sm text-gray-400">Loading API keys...</div>
        ) : keys.length === 0 ? (
          <div className="p-6 text-sm text-gray-400">No API keys yet.</div>
        ) : (
          <table className="min-w-full text-sm">
            <thead>
              <tr className="bg-gray-50 border-b">
                <th className="px-4 py-3 text-left font-semibold text-gray-600">Name</th>
                <th className="px-4 py-3 text-left font-semibold text-gray-600">Prefix</th>
                <th className="px-4 py-3 text-left font-semibold text-gray-600">Status</th>
                <th className="px-4 py-3 text-left font-semibold text-gray-600">Created</th>
                <th className="px-4 py-3 text-left font-semibold text-gray-600">Last Used</th>
                <th className="px-4 py-3 text-left font-semibold text-gray-600">Expires</th>
                <th className="px-4 py-3"></th>
              </tr>
            </thead>
            <tbody>
              {keys.map((k) => (
                <tr key={k.id} className="border-b last:border-0 hover:bg-gray-50">
                  <td className="px-4 py-2 font-medium text-gray-900">{k.name}</td>
                  <td className="px-4 py-2 font-mono text-gray-600 text-xs">{k.keyPrefix}…</td>
                  <td className="px-4 py-2">
                    <span
                      className={`text-xs font-medium px-2 py-0.5 rounded-full ${
                        k.active ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'
                      }`}
                    >
                      {k.active ? 'Active' : 'Revoked'}
                    </span>
                  </td>
                  <td className="px-4 py-2 text-gray-500 text-xs">{formatDate(k.createdAt)}</td>
                  <td className="px-4 py-2 text-gray-500 text-xs">{formatDate(k.lastUsedAt)}</td>
                  <td className="px-4 py-2 text-gray-500 text-xs">{formatDate(k.expiresAt)}</td>
                  <td className="px-4 py-2">
                    {k.active && (
                      <button
                        onClick={() => deleteMutation.mutate(k.id)}
                        disabled={deleteMutation.isPending}
                        className="text-red-600 hover:text-red-800 text-xs font-medium disabled:opacity-50"
                      >
                        Revoke
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Audit Log */}
      <section>
        <h2 className="text-sm font-semibold text-gray-600 uppercase mb-3">Audit Log</h2>
        <div className="bg-white rounded-xl shadow overflow-x-auto">
          {loadingAudit ? (
            <div className="p-6 text-sm text-gray-400">Loading audit log...</div>
          ) : !audit || audit.content.length === 0 ? (
            <div className="p-6 text-sm text-gray-400">No audit entries.</div>
          ) : (
            <table className="min-w-full text-sm">
              <thead>
                <tr className="bg-gray-50 border-b">
                  <th className="px-4 py-3 text-left font-semibold text-gray-600">Action</th>
                  <th className="px-4 py-3 text-left font-semibold text-gray-600">Entity</th>
                  <th className="px-4 py-3 text-left font-semibold text-gray-600">Details</th>
                  <th className="px-4 py-3 text-left font-semibold text-gray-600">IP</th>
                  <th className="px-4 py-3 text-left font-semibold text-gray-600">Date</th>
                </tr>
              </thead>
              <tbody>
                {audit.content.map((entry) => (
                  <tr key={entry.id} className="border-b last:border-0 hover:bg-gray-50">
                    <td className="px-4 py-2 font-medium text-gray-800">{entry.action}</td>
                    <td className="px-4 py-2 text-gray-600 text-xs">
                      {entry.entityType} <span className="text-gray-400">{entry.entityId.slice(0, 8)}…</span>
                    </td>
                    <td className="px-4 py-2 text-gray-500 text-xs max-w-xs truncate">{entry.details}</td>
                    <td className="px-4 py-2 text-gray-400 text-xs font-mono">{entry.ipAddress}</td>
                    <td className="px-4 py-2 text-gray-400 text-xs">{formatDate(entry.createdAt)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </section>

      {/* Create Modal */}
      {showCreate && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-2xl p-6 w-full max-w-md">
            <h2 className="text-lg font-bold text-gray-900 mb-4">Create API Key</h2>
            <form onSubmit={handleCreate} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Key Name</label>
                <input
                  type="text"
                  value={newName}
                  onChange={(e) => setNewName(e.target.value)}
                  placeholder="e.g. Production Integration"
                  className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Expires In (days) <span className="text-gray-400 font-normal">— optional</span>
                </label>
                <input
                  type="number"
                  min={1}
                  value={expiresInDays}
                  onChange={(e) => setExpiresInDays(e.target.value)}
                  placeholder="e.g. 90"
                  className="w-full border rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
              {createMutation.isError && (
                <div className="text-red-600 text-sm">Failed to create key. Please try again.</div>
              )}
              <div className="flex gap-3 pt-2">
                <button
                  type="submit"
                  disabled={createMutation.isPending}
                  className="flex-1 bg-blue-600 text-white py-2 rounded-lg text-sm font-semibold hover:bg-blue-700 disabled:opacity-50 transition-colors"
                >
                  {createMutation.isPending ? 'Creating...' : 'Create'}
                </button>
                <button
                  type="button"
                  onClick={() => setShowCreate(false)}
                  className="flex-1 border text-gray-700 py-2 rounded-lg text-sm font-semibold hover:bg-gray-50 transition-colors"
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Show Raw Key Modal */}
      {createdKey && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl shadow-2xl p-6 w-full max-w-lg">
            <h2 className="text-lg font-bold text-gray-900 mb-1">API Key Created</h2>
            <p className="text-sm text-amber-700 bg-amber-50 border border-amber-200 rounded-lg px-3 py-2 mb-4">
              Copy this key now — it will not be shown again.
            </p>
            <div className="bg-gray-100 rounded-lg px-4 py-3 font-mono text-sm text-gray-800 break-all mb-4 select-all">
              {createdKey.rawKey}
            </div>
            <div className="flex gap-3">
              <button
                onClick={handleCopy}
                className="flex-1 bg-blue-600 text-white py-2 rounded-lg text-sm font-semibold hover:bg-blue-700 transition-colors"
              >
                {copied ? 'Copied!' : 'Copy to Clipboard'}
              </button>
              <button
                onClick={() => setCreatedKey(null)}
                className="flex-1 border text-gray-700 py-2 rounded-lg text-sm font-semibold hover:bg-gray-50 transition-colors"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
