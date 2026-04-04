import { useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useSearchStore } from '../store/searchStore'
import { useBookingStore } from '../store/bookingStore'
import { trainApi } from '../api/trainApi'
import { useAuthStore } from '../store/authStore'
import { formatDuration } from '../utils/formatters'
import toast from 'react-hot-toast'

const CLASS_COLORS = {
  SL: 'bg-green-100 text-green-800',
  '3A': 'bg-blue-100 text-blue-800',
  '2A': 'bg-purple-100 text-purple-800',
  '1A': 'bg-yellow-100 text-yellow-800'
}

export default function SearchResults() {
  const navigate = useNavigate()
  const { isAuthenticated } = useAuthStore()
  const { searchParams, searchResults, setSearchResults, isLoading, setLoading, error, setError } = useSearchStore()
  const { setSelectedTrain, setSelectedClass, setJourneyDate } = useBookingStore()

  useEffect(() => {
    if (!searchParams.from || !searchParams.to) { navigate('/'); return }
    doSearch()
  }, [])

  const doSearch = async () => {
    setLoading(true); setError(null)
    try {
      const res = await trainApi.search({
        from: searchParams.from, to: searchParams.to,
        date: searchParams.date,
        ...(searchParams.classType && { classType: searchParams.classType })
      })
      setSearchResults(res.data.data || [])
    } catch {
      setError('No trains found. Try different stations or date.')
      setSearchResults([])
    } finally { setLoading(false) }
  }

  const handleBook = (train, classCode) => {
    if (!isAuthenticated) { toast.error('Please login to book'); navigate('/login'); return }
    setSelectedTrain(train); setSelectedClass(classCode); setJourneyDate(searchParams.date)
    navigate('/booking')
  }

  const classEntries = [
    { key: 'sl',     code: 'SL' },
    { key: 'threeA', code: '3A' },
    { key: 'twoA',   code: '2A' },
    { key: 'oneA',   code: '1A' }
  ]

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      {/* Header */}
      <div className="flex flex-wrap items-center gap-2 mb-6">
        <h1 className="text-2xl font-bold text-gray-800 dark:text-white">
          {searchParams.from} → {searchParams.to}
        </h1>
        <span className="px-3 py-1 bg-blue-100 text-blue-700 rounded-full text-sm font-medium">
          📅 {searchParams.date}
        </span>
        {!isLoading && searchResults.length > 0 && (
          <span className="px-3 py-1 bg-green-100 text-green-700 rounded-full text-sm font-medium">
            {searchResults.length} train{searchResults.length > 1 ? 's' : ''} found
          </span>
        )}
        <button onClick={() => navigate('/')} className="ml-auto text-sm text-blue-600 hover:underline">
          ← Modify Search
        </button>
      </div>

      {/* Loading */}
      {isLoading && (
        <div className="text-center py-24">
          <div className="text-5xl animate-bounce inline-block">🚂</div>
          <p className="text-gray-500 mt-4">Searching trains...</p>
        </div>
      )}

      {/* Error */}
      {!isLoading && error && (
        <div className="text-center py-24">
          <div className="text-5xl mb-4">🛤️</div>
          <p className="text-gray-600 text-lg mb-4">{error}</p>
          <button onClick={() => navigate('/')} className="btn-primary">← Back to Search</button>
        </div>
      )}

      {/* Train cards */}
      <div className="space-y-4">
        {searchResults.map(train => (
          <div key={train.id} className="card overflow-hidden hover:shadow-md transition">
            {/* Train header */}
            <div className="flex flex-wrap items-center justify-between px-6 py-4 bg-gray-50 dark:bg-gray-750 border-b border-gray-100 dark:border-gray-700">
              <div>
                <p className="text-xs font-mono text-gray-400">#{train.trainNumber}</p>
                <h3 className="font-bold text-gray-800 dark:text-white">{train.trainName}</h3>
                <p className="text-xs text-gray-400">{train.daysOfRun}</p>
              </div>
              <div className="flex items-center gap-4 text-center mt-2 md:mt-0">
                <div>
                  <p className="font-mono font-bold text-xl text-gray-800 dark:text-white">{train.departureTime}</p>
                  <p className="text-xs text-gray-500">{train.sourceStation}</p>
                </div>
                <div className="text-center">
                  <p className="text-xs text-gray-400">{formatDuration(train.journeyMins)}</p>
                  <div className="flex items-center gap-1 my-0.5">
                    <div className="w-12 h-px bg-gray-300" /><span className="text-gray-300 text-xs">🚂</span><div className="w-12 h-px bg-gray-300" />
                  </div>
                </div>
                <div>
                  <p className="font-mono font-bold text-xl text-gray-800 dark:text-white">{train.arrivalTime}</p>
                  <p className="text-xs text-gray-500">{train.destStation}</p>
                </div>
              </div>
            </div>

            {/* Class availability */}
            <div className="px-6 py-4 grid grid-cols-2 md:grid-cols-4 gap-3">
              {classEntries.map(({ key, code }) => {
                const cls = train[key]
                if (!cls) return null
                const avail = cls.availableSeats > 0
                return (
                  <div key={code} className={`border rounded-xl p-3 text-center ${avail ? 'border-green-200 bg-green-50 dark:bg-green-900/20' : 'border-red-200 bg-red-50 dark:bg-red-900/20'}`}>
                    <span className={`inline-block px-2 py-0.5 rounded text-xs font-bold mb-1 ${CLASS_COLORS[code]}`}>{code}</span>
                    <p className="font-bold text-gray-800 dark:text-white">₹{cls.totalFare}</p>
                    <p className={`text-xs font-medium mt-0.5 ${avail ? 'text-green-600' : 'text-red-500'}`}>
                      {cls.availabilityStatus}
                    </p>
                    <button onClick={() => avail && handleBook(train, code)} disabled={!avail}
                      className={`mt-2 w-full py-1.5 text-xs font-bold rounded-lg transition ${avail ? 'bg-blue-600 text-white hover:bg-blue-700' : 'bg-gray-200 text-gray-400 cursor-not-allowed'}`}>
                      {avail ? 'Book Now' : 'N/A'}
                    </button>
                  </div>
                )
              })}
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
