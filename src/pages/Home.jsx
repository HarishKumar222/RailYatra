import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useSearchStore } from '../store/searchStore'
import { trainApi } from '../api/trainApi'
import toast from 'react-hot-toast'

export default function Home() {
  const navigate = useNavigate()
  const { searchParams, setSearchParams } = useSearchStore()
  const [stations, setStations] = useState([])
  const [fromSugg, setFromSugg] = useState([])
  const [toSugg,   setToSugg]   = useState([])

  useEffect(() => {
    trainApi.getStations().then(r => setStations(r.data.data || [])).catch(() => {})
  }, [])

  const filter = (q, setter) => {
    if (q.length < 2) { setter([]); return }
    setter(stations.filter(s => s.toLowerCase().includes(q.toLowerCase())).slice(0, 7))
  }

  const tomorrow = new Date(Date.now() + 86400000).toISOString().split('T')[0]
  const maxDate  = new Date(Date.now() + 90 * 86400000).toISOString().split('T')[0]

  const handleSearch = (e) => {
    e.preventDefault()
    if (!searchParams.from || !searchParams.to || !searchParams.date) { toast.error('Fill all fields'); return }
    if (searchParams.from === searchParams.to) { toast.error('Source and destination cannot be same'); return }
    navigate('/search')
  }

  const SuggList = ({ items, onSelect, clear }) =>
    items.length > 0 ? (
      <ul className="absolute z-30 w-full bg-white dark:bg-gray-700 border border-gray-200 dark:border-gray-600 rounded-xl mt-1 shadow-lg max-h-48 overflow-y-auto">
        {items.map(s => (
          <li key={s} onClick={() => { onSelect(s); clear() }}
            className="px-4 py-2 hover:bg-blue-50 dark:hover:bg-gray-600 cursor-pointer text-sm text-gray-800 dark:text-white">
            🚉 {s}
          </li>
        ))}
      </ul>
    ) : null

  return (
    <div className="min-h-screen">
      {/* Hero */}
      <div className="bg-gradient-to-br from-blue-700 via-blue-800 to-indigo-900 text-white py-16 px-4">
        <div className="max-w-4xl mx-auto text-center mb-10">
          <div className="text-5xl mb-3">🚂</div>
          <h1 className="text-4xl md:text-5xl font-extrabold mb-3">
            Book Trains <span className="text-yellow-400">Smarter</span>
          </h1>
          <p className="text-blue-200 mb-4">Real-time availability · WL predictor · 1-click booking · Better than IRCTC</p>
          <div className="flex flex-wrap justify-center gap-4 text-sm text-blue-300">
            {['⚡ No server crashes', '📊 WL AI predictor', '🎯 1-click booking', '🌙 Dark mode'].map(f => (
              <span key={f}>{f}</span>
            ))}
          </div>
        </div>

        {/* Search box */}
        <div className="max-w-3xl mx-auto">
          <form onSubmit={handleSearch} className="bg-white dark:bg-gray-800 rounded-2xl shadow-2xl p-6">
            <h2 className="text-gray-800 dark:text-white font-bold text-lg mb-4 text-center">🔍 Search Trains</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">

              {/* FROM */}
              <div className="relative">
                <label className="block text-xs font-semibold text-gray-400 uppercase mb-1">From</label>
                <input value={searchParams.from}
                  onChange={e => { setSearchParams({ from: e.target.value }); filter(e.target.value, setFromSugg) }}
                  placeholder="e.g. NEW DELHI" className="input-field" />
                <SuggList items={fromSugg} onSelect={v => setSearchParams({ from: v })} clear={() => setFromSugg([])} />
              </div>

              {/* TO */}
              <div className="relative">
                <label className="block text-xs font-semibold text-gray-400 uppercase mb-1">To</label>
                <input value={searchParams.to}
                  onChange={e => { setSearchParams({ to: e.target.value }); filter(e.target.value, setToSugg) }}
                  placeholder="e.g. MUMBAI CENTRAL" className="input-field" />
                <SuggList items={toSugg} onSelect={v => setSearchParams({ to: v })} clear={() => setToSugg([])} />
              </div>

              {/* DATE */}
              <div>
                <label className="block text-xs font-semibold text-gray-400 uppercase mb-1">Journey Date</label>
                <input type="date" min={tomorrow} max={maxDate} value={searchParams.date}
                  onChange={e => setSearchParams({ date: e.target.value })} className="input-field" />
              </div>

              {/* CLASS */}
              <div>
                <label className="block text-xs font-semibold text-gray-400 uppercase mb-1">Class (Optional)</label>
                <select value={searchParams.classType}
                  onChange={e => setSearchParams({ classType: e.target.value })} className="input-field">
                  <option value="">All Classes</option>
                  <option value="SL">Sleeper (SL)</option>
                  <option value="3A">AC 3 Tier (3A)</option>
                  <option value="2A">AC 2 Tier (2A)</option>
                  <option value="1A">AC First Class (1A)</option>
                </select>
              </div>
            </div>

            <button type="submit" className="w-full py-3 bg-blue-600 hover:bg-blue-700 text-white font-bold text-base rounded-xl transition">
              🔍 Search Trains
            </button>
          </form>
        </div>
      </div>

      {/* Features */}
      <div className="max-w-6xl mx-auto px-4 py-16">
        <h2 className="text-2xl font-bold text-center text-gray-800 dark:text-white mb-10">
          Why <span className="text-blue-600">RailYatra</span> beats IRCTC
        </h2>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {[
            ['⚡', 'No Server Crashes', 'Redis queue handles Tatkal rush without timeouts'],
            ['📊', 'WL Predictor', 'AI predicts if your waitlisted ticket will confirm'],
            ['🎯', '1-Click Booking', 'Saved passenger profiles, book in seconds'],
            ['📱', 'Mobile First', 'Responsive, touch-optimized beautiful UI'],
            ['🔔', 'Smart Alerts', 'Email on confirmation, WL upgrade, refund'],
            ['💳', 'Secure Payments', 'Razorpay with HMAC signature verification'],
            ['🌙', 'Dark Mode', 'Easy on the eyes, switch anytime'],
            ['🧾', 'PDF Tickets', 'Professional e-tickets instantly downloadable'],
          ].map(([icon, title, desc]) => (
            <div key={title} className="card p-4 hover:shadow-md transition">
              <div className="text-2xl mb-2">{icon}</div>
              <h3 className="font-bold text-gray-800 dark:text-white text-sm mb-1">{title}</h3>
              <p className="text-xs text-gray-500 dark:text-gray-400">{desc}</p>
            </div>
          ))}
        </div>
      </div>

      {/* Quick PNR */}
      <div className="bg-blue-50 dark:bg-gray-800 py-12 px-4">
        <div className="max-w-md mx-auto text-center">
          <h2 className="text-xl font-bold text-gray-800 dark:text-white mb-1">Quick PNR Check</h2>
          <p className="text-gray-500 text-sm mb-4">No login needed</p>
          <div className="flex gap-2">
            <input id="qpnr" type="text" maxLength={10} placeholder="Enter 10-digit PNR"
              className="flex-1 input-field text-center font-mono tracking-widest" />
            <button onClick={() => navigate(`/pnr?pnr=${document.getElementById('qpnr').value}`)}
              className="btn-primary whitespace-nowrap">Check</button>
          </div>
        </div>
      </div>
    </div>
  )
}
