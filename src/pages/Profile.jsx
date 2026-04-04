import { useState, useEffect } from 'react'
import { useAuthStore } from '../store/authStore'
import api from '../api/axiosInstance'
import toast from 'react-hot-toast'

const emptyForm = { name: '', age: '', gender: 'MALE' }

export default function Profile() {
  const { user } = useAuthStore()
  const [profiles, setProfiles] = useState([])
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState(emptyForm)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    api.get('/profiles').then(r => setProfiles(r.data.data || [])).catch(() => {})
  }, [])

  const saveProfile = async () => {
    if (!form.name.trim() || !form.age) { toast.error('Fill all fields'); return }
    setSaving(true)
    try {
      const res = await api.post('/profiles', { ...form, age: parseInt(form.age) })
      setProfiles([...profiles, res.data.data])
      setShowForm(false); setForm(emptyForm)
      toast.success('Passenger profile saved!')
    } catch (err) { toast.error(err.response?.data?.error || 'Save failed') }
    finally { setSaving(false) }
  }

  const deleteProfile = async (id) => {
    try {
      await api.delete(`/profiles/${id}`)
      setProfiles(profiles.filter(p => p.id !== id))
      toast.success('Profile removed')
    } catch { toast.error('Delete failed') }
  }

  const maxProfiles = user?.isPremium ? 10 : 3

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-800 dark:text-white mb-6">My Profile</h1>

      {/* User card */}
      <div className="card p-6 mb-6">
        <div className="flex items-center gap-4">
          <div className="w-14 h-14 bg-blue-100 dark:bg-blue-900 rounded-full flex items-center justify-center text-2xl font-bold text-blue-600 dark:text-blue-400">
            {user?.name?.[0]?.toUpperCase()}
          </div>
          <div>
            <h2 className="text-xl font-bold text-gray-800 dark:text-white">
              {user?.name}
              {user?.isPremium && <span className="ml-2 text-yellow-500 text-base">⭐ Premium</span>}
            </h2>
            <p className="text-gray-500 text-sm">{user?.email}</p>
            <span className="text-xs px-2 py-0.5 bg-blue-100 text-blue-700 rounded-full">{user?.role}</span>
          </div>
        </div>

        {!user?.isPremium && (
          <div className="mt-4 p-4 bg-yellow-50 border border-yellow-200 rounded-xl">
            <p className="text-sm font-bold text-yellow-700">⭐ Upgrade to Premium — ₹99/month</p>
            <ul className="text-xs text-yellow-600 mt-1 space-y-0.5">
              <li>• 10 saved passenger profiles (free: 3)</li>
              <li>• Priority Tatkal queue</li>
              <li>• Zero convenience fee</li>
              <li>• 1-click fast booking</li>
            </ul>
            <button className="mt-2 px-4 py-1.5 bg-yellow-500 hover:bg-yellow-600 text-white text-sm font-bold rounded-lg transition">
              Upgrade Now
            </button>
          </div>
        )}
      </div>

      {/* Saved passengers */}
      <div className="card p-6">
        <div className="flex justify-between items-center mb-4">
          <h3 className="font-bold text-gray-800 dark:text-white">
            Saved Passengers
            <span className="ml-2 text-sm font-normal text-gray-400">({profiles.length}/{maxProfiles})</span>
          </h3>
          {profiles.length < maxProfiles && (
            <button onClick={() => setShowForm(true)} className="btn-primary py-1.5 px-3 text-sm">+ Add</button>
          )}
        </div>

        {profiles.length === 0 && !showForm && (
          <p className="text-gray-400 text-sm text-center py-6">
            No saved passengers yet. Add them for lightning-fast booking!
          </p>
        )}

        <div className="space-y-2 mb-4">
          {profiles.map(p => (
            <div key={p.id} className="flex justify-between items-center px-4 py-3 bg-gray-50 dark:bg-gray-700 rounded-xl">
              <div>
                <span className="font-medium text-gray-800 dark:text-white">{p.name}</span>
                <span className="ml-2 text-sm text-gray-500">{p.age} yrs · {p.gender}</span>
                {p.isDefault && <span className="ml-2 text-xs text-blue-600 font-bold">DEFAULT</span>}
              </div>
              <button onClick={() => deleteProfile(p.id)} className="text-red-400 hover:text-red-600 transition">✕</button>
            </div>
          ))}
        </div>

        {showForm && (
          <div className="border-2 border-blue-100 dark:border-blue-800 rounded-xl p-4 space-y-3">
            <h4 className="font-semibold text-gray-700 dark:text-white text-sm">Add Passenger</h4>
            <input value={form.name} onChange={e => setForm({ ...form, name: e.target.value })}
              placeholder="Full Name" className="input-field text-sm" />
            <div className="grid grid-cols-2 gap-2">
              <input type="number" value={form.age} onChange={e => setForm({ ...form, age: e.target.value })}
                placeholder="Age" className="input-field text-sm" min="1" max="125" />
              <select value={form.gender} onChange={e => setForm({ ...form, gender: e.target.value })}
                className="input-field text-sm">
                <option value="MALE">Male</option>
                <option value="FEMALE">Female</option>
                <option value="TRANSGENDER">Other</option>
              </select>
            </div>
            <div className="flex gap-2">
              <button onClick={saveProfile} disabled={saving} className="btn-primary flex-1 py-2 text-sm">
                {saving ? 'Saving...' : 'Save'}
              </button>
              <button onClick={() => { setShowForm(false); setForm(emptyForm) }}
                className="px-4 py-2 border border-gray-300 text-gray-500 rounded-xl text-sm hover:bg-gray-50 transition">
                Cancel
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
