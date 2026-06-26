import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { orderApi, type CreateOrderPayload } from '@/shared/api/orderApi'
import { customerApi } from '@/shared/api/customerApi'
import { Badge } from '@/shared/components/Badge'
import { EmptyState } from '@/shared/components/EmptyState'
import { Spinner } from '@/shared/components/Spinner'

const STATUS_CONFIG = {
  PENDING:   { label: 'Pendente',   color: 'gray'   as const },
  ASSIGNED:  { label: 'Atribuído',  color: 'blue'   as const },
  DELIVERED: { label: 'Entregue',   color: 'green'  as const },
  FAILED:    { label: 'Falhou',     color: 'red'    as const },
  CANCELLED: { label: 'Cancelado',  color: 'gray'   as const },
}

export default function OrdersPage() {
  const [showForm, setShowForm] = useState(false)
  const qc = useQueryClient()

  const { data: orders, isLoading } = useQuery({ queryKey: ['orders'], queryFn: orderApi.list })
  const { data: customers } = useQuery({ queryKey: ['customers'], queryFn: customerApi.list })

  const create = useMutation({
    mutationFn: orderApi.create,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['orders'] }); setShowForm(false); reset() },
  })

  const { register, handleSubmit, reset } = useForm<CreateOrderPayload>({
    defaultValues: { weightKg: 1, deliveryDate: new Date().toISOString().slice(0, 10) },
  })

  return (
    <div className="p-6 max-w-4xl mx-auto">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Pedidos</h1>
          <p className="text-gray-500 text-sm mt-0.5">{orders?.length ?? 0} cadastrados</p>
        </div>
        <button className="btn-primary" onClick={() => setShowForm(!showForm)}>
          {showForm ? 'Cancelar' : '+ Novo Pedido'}
        </button>
      </div>

      {showForm && (
        <div className="card p-6 mb-6">
          <h2 className="font-semibold text-gray-800 mb-4">Novo Pedido</h2>
          <form onSubmit={handleSubmit((v) => create.mutate(v))} className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Código do pedido *</label>
              <input {...register('orderCode', { required: true })} className="input" placeholder="PED-001" />
            </div>
            <div>
              <label className="label">Cliente *</label>
              <select {...register('customerId', { required: true })} className="input">
                <option value="">Selecione...</option>
                {customers?.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select>
            </div>
            <div>
              <label className="label">Peso (kg) *</label>
              <input {...register('weightKg', { required: true, valueAsNumber: true })} type="number" step="0.1" className="input" />
            </div>
            <div>
              <label className="label">Data de entrega *</label>
              <input {...register('deliveryDate', { required: true })} type="date" className="input" />
            </div>
            <div>
              <label className="label">Valor declarado (R$)</label>
              <input {...register('declaredValueBrl', { valueAsNumber: true })} type="number" step="0.01" className="input" />
            </div>
            <div>
              <label className="label">Descrição</label>
              <input {...register('description')} className="input" />
            </div>
            <div className="col-span-2 flex justify-end gap-3 mt-2">
              <button type="button" className="btn-secondary" onClick={() => { setShowForm(false); reset() }}>Cancelar</button>
              <button type="submit" disabled={create.isPending} className="btn-primary">
                {create.isPending ? <Spinner size="sm" /> : 'Salvar'}
              </button>
            </div>
          </form>
          {create.isError && <p className="text-red-600 text-sm mt-3">Erro ao criar pedido.</p>}
        </div>
      )}

      {isLoading ? (
        <div className="card p-8 flex justify-center"><Spinner size="lg" /></div>
      ) : orders?.length === 0 ? (
        <div className="card"><EmptyState icon="📦" title="Nenhum pedido cadastrado" /></div>
      ) : (
        <div className="card divide-y divide-gray-100">
          {orders?.map((o) => {
            const sc = STATUS_CONFIG[o.status] ?? STATUS_CONFIG.PENDING
            return (
              <div key={o.id} className="px-5 py-4 flex items-center justify-between hover:bg-gray-50">
                <div>
                  <div className="font-medium text-gray-900">{o.orderCode}</div>
                  <div className="text-sm text-gray-500 mt-0.5">
                    {o.description && `${o.description} · `}{o.weight?.kilograms} kg · {o.deliveryDate}
                  </div>
                </div>
                <Badge label={sc.label} variant={sc.color} />
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
