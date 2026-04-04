import { useEffect, useState } from 'react'
import api from '../../api/axiosInstance'
import toast from 'react-hot-toast'
import { getStatusColor } from '../../utils/formatters'

const TABS = [
  { id: 'overview', label: '📊 Overview' },
  { id: 'bookings', label: '🎟️ Bookings' },
  { id: 'trains',   label: '🚂 Trains'   }
]

export default function AdminDashboard() {
  const [stats, setStats] = useState(null)
  const [bookings, setBookings] = useState([])
  const [trains,   setTrains]   = useState([])
  const [tab, setTab] = useState('overview')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.allSettled([
      api.get('/admin/analytics'),
      api.get('/admin/bookings?size=15'),
      api.get('/admin/trains')
    ]).then(([a, b, t]) => {
      if (a.status === 'fulfilled') setStats(a.value.data.data)
      if (b.status === 'fulfilled') {
       const bData = b.value.data?.data
      setBookings(
       bData?.content || 
       (Array.isArray(bData) ? bData : [])
  )
}
      if (t.status === 'fulfilled') setTrains(t.value.data.data || [])
    }).finally(() => setLoading(false))
  }, [])


  if (loading) return <div className="flex justify-center items-center h-64 text-4xl animate-bounce">🚂</div>

  const statCards = [
    { label: 'Users',      value: stats?.totalUsers,         icon: '👤', color: 'bg-blue-50 text-blue-700' },
    { label: 'Trains',     value: stats?.totalTrains,        icon: '🚂', color: 'bg-green-50 text-green-700' },
    { label: 'Bookings',   value: stats?.totalBookings,      icon: '🎟️', color: 'bg-yellow-50 text-yellow-700' },
    { label: 'Confirmed',  value: stats?.confirmedBookings,  icon: '✅', color: 'bg-emerald-50 text-emerald-700' },
    { label: 'Revenue',    value: `₹${Number(stats?.totalRevenue || 0).toLocaleString('en-IN')}`, icon: '💰', color: 'bg-purple-50 text-purple-700' }
  ]

  return (
    <div className="max-w-6xl mx-auto px-4 py-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-800 dark:text-white">Admin Dashboard</h1>
          <p className="text-gray-400 text-sm">RailYatra Platform Control</p>
        </div>
        <span className="px-3 py-1 bg-purple-100 text-purple-700 rounded-full text-sm font-bold">🔐 ADMIN</span>
      </div>

      {/* Stat cards */}
      <div className="grid grid-cols-2 md:grid-cols-5 gap-3 mb-8">
        {statCards.map(s => (
          <div key={s.label} className={`${s.color} rounded-2xl p-4`}>
            <div className="text-2xl mb-1">{s.icon}</div>
            <div className="text-xl font-bold">{s.value ?? '—'}</div>
            <div className="text-xs font-medium opacity-70">{s.label}</div>
          </div>
        ))}
      </div>

      {/* Tabs */}
      <div className="flex gap-1 mb-6 border-b border-gray-200 dark:border-gray-700">
        {TABS.map(t => (
          <button key={t.id} onClick={() => setTab(t.id)}
            className={`px-4 py-2 text-sm font-medium border-b-2 -mb-px transition ${
              tab === t.id ? 'border-blue-600 text-blue-600' : 'border-transparent text-gray-500 hover:text-gray-700'}`}>
            {t.label}
          </button>
        ))}
      </div>

      {/* Overview */}
      {tab === 'overview' && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="card p-5">
            <h3 className="font-bold text-gray-700 dark:text-white mb-4">📈 Platform Metrics</h3>
            <div className="space-y-3">
              {[
                ['Conversion Rate',       `${Math.round((stats?.confirmedBookings / Math.max(1, stats?.totalBookings)) * 100)}%`],
                ['Avg. Revenue/Booking',  `₹${Math.round(stats?.totalRevenue / Math.max(1, stats?.confirmedBookings))}`],
                ['Active Trains',          stats?.totalTrains],
                ['Registered Users',       stats?.totalUsers]
              ].map(([l, v]) => (
                <div key={l} className="flex justify-between py-2 border-b border-gray-50 dark:border-gray-700 last:border-0">
                  <span className="text-sm text-gray-500">{l}</span>
                  <span className="font-bold text-gray-800 dark:text-white">{v}</span>
                </div>
              ))}
            </div>
          </div>
          <div className="card p-5">
            <h3 className="font-bold text-gray-700 dark:text-white mb-4">🔗 Quick Actions</h3>
            <div className="space-y-2">
              {[
                ['🎟️ View All Bookings', () => setTab('bookings')],
                ['🚂 Manage Trains',     () => setTab('trains')],
              ].map(([label, action]) => (
                <button key={label} onClick={action}
                  className="w-full text-left px-4 py-3 bg-gray-50 dark:bg-gray-700 rounded-xl hover:bg-blue-50 dark:hover:bg-blue-900/20 text-sm font-medium text-gray-700 dark:text-gray-300 transition">
                  {label}
                </button>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* Bookings table */}
      {tab === 'bookings' && (
        <div className="card overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-gray-50 dark:bg-gray-700">
                <tr>
                  {['PNR','Train','Route','Date','Class','Status','Amount'].map(h => (
                    <th key={h} className="px-4 py-3 text-left text-xs font-semibold text-gray-500 uppercase tracking-wider whitespace-nowrap">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 dark:divide-gray-700">
                {bookings.length === 0 ? (
                  <tr><td colSpan={7} className="text-center py-10 text-gray-400">No bookings yet</td></tr>
                ) : bookings.map(b => (
                  <tr key={b.id} className="hover:bg-gray-50 dark:hover:bg-gray-700/50">
                    <td className="px-4 py-3 font-mono text-blue-600 text-xs">{b.pnr}</td>
                    <td className="px-4 py-3 text-gray-800 dark:text-white font-medium whitespace-nowrap">{b.trainName}</td>
                    <td className="px-4 py-3 text-gray-500 text-xs whitespace-nowrap">{b.sourceStation} → {b.destStation}</td>
                    <td className="px-4 py-3 text-gray-500 whitespace-nowrap">{b.journeyDate}</td>
                    <td className="px-4 py-3"><span className="px-2 py-0.5 bg-blue-100 text-blue-700 rounded text-xs font-medium">{b.classType}</span></td>
                    <td className="px-4 py-3"><span className={`px-2 py-0.5 rounded text-xs font-medium ${getStatusColor(b.status)}`}>{b.status}</span></td>
                    <td className="px-4 py-3 font-medium text-gray-800 dark:text-white">₹{b.totalAmount}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Trains list */}
      {tab === 'trains' && (
        <div className="space-y-3">
          {trains.map(t => (
            <div key={t.id} className="card p-4 flex justify-between items-center">
              <div>
                <p className="font-bold text-gray-800 dark:text-white">
                  {t.trainName}
                  <span className="ml-2 text-xs text-gray-400 font-mono font-normal">#{t.trainNumber}</span>
                </p>
                <p className="text-sm text-gray-500">{t.sourceStation} → {t.destStation} · {t.daysOfRun}</p>
              </div>
              <div className="text-right text-sm">
                <p className="text-gray-500">{t.departureTime} – {t.arrivalTime}</p>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
