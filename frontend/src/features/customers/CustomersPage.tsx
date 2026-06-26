import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { customerApi, type CreateCustomerPayload } from '@/shared/api/customerApi'
import { Badge } from '@/shared/components/Badge'
import { EmptyState } from '@/shared/components/EmptyState'
import { Spinner } from '@/shared/components/Spinner'

type FormValues = CreateCustomerPayload

const PRIORITY_LABELS: Record<number, { label: string; color: 'gray' | 'blue' | 'yellow' | 'red' }> = {
  1: { label: 'Baixa',    color: 'gray' },
  2: { label: 'Normal',   color: 'blue' },
  3: { label: 'Média',    color: 'blue' },
  4: { label: 'Alta',     color: 'yellow' },
  5: { label: 'Crítica',  color: 'red' },
}

export default function CustomersPage() {
  const [showForm, setShowForm] = useState(false)
  const qc = useQueryClient()

  const { data: customers, isLoading } = useQuery({
    queryKey: ['customers'],
    queryFn: customerApi.list,
  })

  const create = useMutation({
    mutationFn: customerApi.create,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['customers'] })
      setShowForm(false)
      reset()
    },
  })

  const { register, handleSubmit, reset } = useForm<FormValues>({
    defaultValues: { priority: 2 },
  })

  return (
    <div className="p-6 max-w-4xl mx-auto">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Clientes</h1>
          <p className="text-gray-500 text-sm mt-0.5">{customers?.length ?? 0} cadastrados</p>
        </div>
        <button className="btn-primary" onClick={() => setShowForm(!showForm)}>
          {showForm ? 'Cancelar' : '+ Novo Cliente'}
        </button>
      </div>

      {showForm && (
        <div className="card p-6 mb-6">
          <h2 className="font-semibold text-gray-800 mb-4">Novo Cliente</h2>
          <form onSubmit={handleSubmit((v) => create.mutate(v))} className="grid grid-cols-2 gap-4">
            <div className="col-span-2">
              <label className="label">Nome *</label>
              <input {...register('name', { required: true })} className="input" placeholder="João Silva" />
            </div>
            <div>
              <label className="label">E-mail *</label>
              <input {...register('email', { required: true })} type="email" className="input" />
            </div>
            <div>
              <label className="label">Telefone</label>
              <input {...register('phone')} className="input" placeholder="(11) 99999-9999" />
            </div>
            <div>
              <label className="label">Latitude *</label>
              <input {...register('latitude', { required: true, valueAsNumber: true })} className="input" placeholder="-23.5505" />
            </div>
            <div>
              <label className="label">Longitude *</label>
              <input {...register('longitude', { required: true, valueAsNumber: true })} className="input" placeholder="-46.6333" />
            </div>
            <div>
              <label className="label">Prioridade (1–5)</label>
              <select {...register('priority', { valueAsNumber: true })} className="input">
                <option value={1}>1 — Baixa</option>
                <option value={2}>2 — Normal</option>
                <option value={3}>3 — Média</option>
                <option value={4}>4 — Alta</option>
                <option value={5}>5 — Crítica</option>
              </select>
            </div>
            <div>
              <label className="label">Cidade</label>
              <input {...register('city')} className="input" placeholder="São Paulo" />
            </div>
            <div className="col-span-2 flex justify-end gap-3 mt-2">
              <button type="button" className="btn-secondary" onClick={() => { setShowForm(false); reset() }}>Cancelar</button>
              <button type="submit" disabled={create.isPending} className="btn-primary">
                {create.isPending ? <Spinner size="sm" /> : 'Salvar'}
              </button>
            </div>
          </form>
          {create.isError && (
            <p className="text-red-600 text-sm mt-3">Erro ao criar cliente. Verifique os dados.</p>
          )}
        </div>
      )}

      {isLoading ? (
        <div className="card p-8 flex justify-center"><Spinner size="lg" /></div>
      ) : customers?.length === 0 ? (
        <div className="card">
          <EmptyState icon="👤" title="Nenhum cliente cadastrado" description="Clique em '+ Novo Cliente' para começar" />
        </div>
      ) : (
        <div className="card divide-y divide-gray-100">
          {customers?.map((c) => {
            const prio = PRIORITY_LABELS[c.priority?.value ?? 2]
            return (
              <div key={c.id} className="px-5 py-4 flex items-center justify-between hover:bg-gray-50 transition-colors">
                <div>
                  <div className="font-medium text-gray-900">{c.name}</div>
                  <div className="text-sm text-gray-500 mt-0.5">
                    {c.email}
                    {c.location && (
                      <span className="ml-2 text-gray-400">
                        📍 {c.location.latitude.toFixed(4)}, {c.location.longitude.toFixed(4)}
                      </span>
                    )}
                  </div>
                </div>
                <Badge label={`Prioridade ${prio?.label}`} variant={prio?.color ?? 'gray'} />
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
