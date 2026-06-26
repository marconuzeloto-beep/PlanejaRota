import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { authApi } from '@/shared/api/authApi'
import { useAppStore } from '@/shared/store/appStore'
import { Spinner } from '@/shared/components/Spinner'

interface FormValues {
  slug: string
  email: string
  password: string
}

export default function LoginPage() {
  const navigate = useNavigate()
  const setUser = useAppStore((s) => s.setUser)
  const [error, setError] = useState<string | null>(null)

  const { register, handleSubmit, formState: { isSubmitting } } = useForm<FormValues>()

  const onSubmit = async (values: FormValues) => {
    setError(null)
    try {
      const user = await authApi.login(values.slug, values.email, values.password)
      setUser(user)
      navigate('/', { replace: true })
    } catch {
      setError('Credenciais inválidas. Verifique slug, e-mail e senha.')
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-brand-900 to-brand-600 p-4">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md p-8">
        <div className="text-center mb-8">
          <div className="text-4xl mb-2">🚚</div>
          <h1 className="text-2xl font-bold text-gray-900">LogiCore</h1>
          <p className="text-gray-500 text-sm mt-1">Motor de Decisão Logística</p>
        </div>

        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <label className="label">Slug da organização</label>
            <input
              {...register('slug', { required: true })}
              className="input"
              placeholder="minha-empresa"
              autoComplete="organization"
            />
          </div>
          <div>
            <label className="label">E-mail</label>
            <input
              {...register('email', { required: true })}
              type="email"
              className="input"
              placeholder="admin@empresa.com"
              autoComplete="email"
            />
          </div>
          <div>
            <label className="label">Senha</label>
            <input
              {...register('password', { required: true })}
              type="password"
              className="input"
              placeholder="••••••••"
              autoComplete="current-password"
            />
          </div>

          {error && (
            <div className="bg-red-50 border border-red-200 rounded-lg px-4 py-3 text-sm text-red-700">
              {error}
            </div>
          )}

          <button
            type="submit"
            disabled={isSubmitting}
            className="btn-primary w-full py-3 text-base mt-2"
          >
            {isSubmitting ? <Spinner size="sm" /> : 'Entrar'}
          </button>
        </form>
      </div>
    </div>
  )
}
