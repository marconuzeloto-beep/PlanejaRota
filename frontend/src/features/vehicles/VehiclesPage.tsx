import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useForm } from 'react-hook-form'
import { vehicleApi, type CreateVehiclePayload } from '@/shared/api/vehicleApi'
import { Badge } from '@/shared/components/Badge'
import { EmptyState } from '@/shared/components/EmptyState'
import { Spinner } from '@/shared/components/Spinner'

const STATUS_CONFIG = {
  AVAILABLE:   { label: 'Disponível',  color: 'green'  as const },
  IN_USE:      { label: 'Em uso',      color: 'yellow' as const },
  MAINTENANCE: { label: 'Manutenção',  color: 'red'    as const },
  INACTIVE:    { label: 'Inativo',     color: 'gray'   as const },
}

const VEHICLE_TYPES = ['MOTORCYCLE', 'CAR', 'VAN', 'TRUCK_SMALL', 'TRUCK_LARGE']
const TYPE_LABELS: Record<string, string> = {
  MOTORCYCLE: 'Moto', CAR: 'Carro', VAN: 'Van',
  TRUCK_SMALL: 'Caminhão Pequeno', TRUCK_LARGE: 'Caminhão Grande',
}

export default function VehiclesPage() {
  const [showForm, setShowForm] = useState(false)
  const qc = useQueryClient()

  const { data: vehicles, isLoading } = useQuery({
    queryKey: ['vehicles'],
    queryFn: vehicleApi.list,
  })

  const create = useMutation({
    mutationFn: vehicleApi.create,
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['vehicles'] }); setShowForm(false); reset() },
  })

  const { register, handleSubmit, reset } = useForm<CreateVehiclePayload>({
    defaultValues: { type: 'VAN', capacityKg: 500 },
  })

  return (
    <div className="p-6 max-w-4xl mx-auto">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Veículos</h1>
          <p className="text-gray-500 text-sm mt-0.5">{vehicles?.length ?? 0} cadastrados</p>
        </div>
        <button className="btn-primary" onClick={() => setShowForm(!showForm)}>
          {showForm ? 'Cancelar' : '+ Novo Veículo'}
        </button>
      </div>

      {showForm && (
        <div className="card p-6 mb-6">
          <h2 className="font-semibold text-gray-800 mb-4">Novo Veículo</h2>
          <form onSubmit={handleSubmit((v) => create.mutate(v))} className="grid grid-cols-2 gap-4">
            <div>
              <label className="label">Placa *</label>
              <input {...register('licensePlate', { required: true })} className="input" placeholder="ABC-1234" />
            </div>
            <div>
              <label className="label">Modelo *</label>
              <input {...register('model', { required: true })} className="input" placeholder="Fiat Fiorino" />
            </div>
            <div>
              <label className="label">Tipo</label>
              <select {...register('type')} className="input">
                {VEHICLE_TYPES.map((t) => <option key={t} value={t}>{TYPE_LABELS[t]}</option>)}
              </select>
            </div>
            <div>
              <label className="label">Capacidade (kg) *</label>
              <input {...register('capacityKg', { required: true, valueAsNumber: true })} type="number" className="input" />
            </div>
            <div>
              <label className="label">Custo por km (R$)</label>
              <input {...register('costPerKmBrl', { valueAsNumber: true })} type="number" step="0.01" className="input" placeholder="1.50" />
            </div>
            <div>
              <label className="label">Observações</label>
              <input {...register('notes')} className="input" />
            </div>
            <div className="col-span-2 flex justify-end gap-3 mt-2">
              <button type="button" className="btn-secondary" onClick={() => { setShowForm(false); reset() }}>Cancelar</button>
              <button type="submit" disabled={create.isPending} className="btn-primary">
                {create.isPending ? <Spinner size="sm" /> : 'Salvar'}
              </button>
            </div>
          </form>
        </div>
      )}

      {isLoading ? (
        <div className="card p-8 flex justify-center"><Spinner size="lg" /></div>
      ) : vehicles?.length === 0 ? (
        <div className="card"><EmptyState icon="🚚" title="Nenhum veículo cadastrado" description="Clique em '+ Novo Veículo' para começar" /></div>
      ) : (
        <div className="card divide-y divide-gray-100">
          {vehicles?.map((v) => {
            const sc = STATUS_CONFIG[v.status] ?? STATUS_CONFIG.INACTIVE
            return (
              <div key={v.id} className="px-5 py-4 flex items-center justify-between hover:bg-gray-50">
                <div>
                  <div className="font-medium text-gray-900">{v.licensePlate} — {v.model}</div>
                  <div className="text-sm text-gray-500 mt-0.5">
                    {TYPE_LABELS[v.type]} · {v.capacity?.kilograms} kg
                    {v.costPerKm && ` · R$ ${v.costPerKm.amount}/km`}
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
