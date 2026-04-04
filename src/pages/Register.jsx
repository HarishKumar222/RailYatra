import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { authApi } from '../api/authApi'
import { useAuthStore } from '../store/authStore'
import toast from 'react-hot-toast'

export default function Register() {
  const navigate = useNavigate()
  const { login } = useAuthStore()
  const [form, setForm] = useState({ name: '', email: '', password: '', phone: '' })
  const [loading, setLoading] = useState(false)

  const handle = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      const res = await authApi.register(form)
      const { accessToken, ...user } = res.data.data
      login(user, accessToken)
      toast.success(`Welcome to RailYatra, ${user.name}! 🎉`)
      navigate('/')
    } catch (err) {
      toast.error(err.response?.data?.error || 'Registration failed')
    } finally { setLoading(false) }
  }

  const fields = [
    { key: 'name',     label: 'Full Name',        type: 'text',     ph: 'Harish Kumar' },
    { key: 'email',    label: 'Email',             type: 'email',    ph: 'you@email.com' },
    { key: 'phone',    label: 'Phone (optional)',   type: 'tel',      ph: '9876543210' },
    { key: 'password', label: 'Password',           type: 'password', ph: '••••••••' }
  ]

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center px-4 py-12">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-md p-8">
        <div className="text-center mb-8">
          <div className="text-4xl mb-2">🚂</div>
          <h1 className="text-2xl font-bold text-gray-800">Create Account</h1>
          <p className="text-gray-500 text-sm">Join RailYatra — smarter booking awaits</p>
        </div>
        <form onSubmit={handle} className="space-y-4">
          {fields.map(f => (
            <div key={f.key}>
              <label className="block text-sm font-medium text-gray-700 mb-1">{f.label}</label>
              <input type={f.type} value={form[f.key]}
                onChange={e => setForm({ ...form, [f.key]: e.target.value })}
                className="input-field" placeholder={f.ph}
                required={f.key !== 'phone'} />
            </div>
          ))}
          <button type="submit" disabled={loading} className="btn-primary w-full py-3">
            {loading ? '⏳ Creating...' : '🚀 Create Account'}
          </button>
        </form>
        <p className="text-center text-sm text-gray-500 mt-4">
          Have an account? <Link to="/login" className="text-blue-600 font-medium hover:underline">Sign in</Link>
        </p>
      </div>
    </div>
  )
}
