import { useEffect, useState } from 'react'
import { bookingApi } from '../api/bookingApi'
import { Link } from 'react-router-dom'
import { getStatusColor } from '../utils/formatters'
import toast from 'react-hot-toast'

export default function MyBookings() {
  const [bookings, setBookings] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    bookingApi.getMyBookings()
      .then(r => setBookings(r.data.data || []))
      .catch(() => toast.error('Failed to load bookings'))
      .finally(() => setLoading(false))
  }, [])

  const handleCancel = async (id) => {
    if (!confirm('Cancel this booking? Cancellation charges may apply.')) return
    try {
      const res = await bookingApi.cancel(id)
      setBookings(bs => bs.map(b => b.id === id ? { ...b, status: res.data.data.status } : b))
      toast.success('Booking cancelled. Refund will be processed.')
    } catch (err) { toast.error(err.response?.data?.error || 'Cancellation failed') }
  }

  const downloadTicket = async (id, pnr) => {
    try {
      const res = await bookingApi.downloadTicket(id)
      const url = URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }))
      const a = document.createElement('a')
      a.href = url; a.download = `RailYatra_${pnr}.pdf`; a.click(); URL.revokeObjectURL(url)
      toast.success('Ticket downloaded!')
    } catch { toast.error('Download failed') }
  }

  if (loading) return <div className="flex justify-center items-center h-64 text-4xl animate-bounce">🚂</div>

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-800 dark:text-white mb-6">My Bookings</h1>

      {bookings.length === 0 ? (
        <div className="text-center py-24">
          <div className="text-5xl mb-4">🎟️</div>
          <p className="text-gray-500 mb-4">No bookings yet.</p>
          <Link to="/" className="btn-primary">Search Trains</Link>
        </div>
      ) : (
        <div className="space-y-4">
          {bookings.map(b => (
            <div key={b.id} className="card p-5">
              <div className="flex flex-wrap justify-between items-start gap-3">
                <div>
                  <div className="flex items-center gap-2 mb-1">
                    <span className="font-mono font-bold text-blue-600 text-lg">{b.pnr}</span>
                    <span className={`px-2 py-0.5 rounded-full text-xs font-bold ${getStatusColor(b.status)}`}>{b.status}</span>
                    {b.waitlistNumber && <span className="text-xs text-yellow-600 font-medium">WL/{b.waitlistNumber}</span>}
                  </div>
                  <h3 className="font-bold text-gray-800 dark:text-white">{b.trainName}</h3>
                  <p className="text-sm text-gray-500">{b.sourceStation} → {b.destStation}</p>
                  <p className="text-sm text-gray-500">📅 {b.journeyDate} | {b.classType}</p>
                  <p className="font-bold text-blue-600 mt-1">₹{b.totalAmount}</p>
                </div>
                <div className="flex flex-col gap-2 min-w-[130px]">
                  {(b.status === 'CONFIRMED' || b.status === 'WAITLISTED') && (
                    <button onClick={() => downloadTicket(b.id, b.pnr)}
                      className="px-3 py-2 bg-green-600 text-white text-xs font-bold rounded-lg hover:bg-green-700 transition text-center">
                      📄 Download Ticket
                    </button>
                  )}
                  {(b.status === 'CONFIRMED' || b.status === 'WAITLISTED') && (
                    <button onClick={() => handleCancel(b.id)}
                      className="px-3 py-2 border border-red-400 text-red-600 text-xs font-medium rounded-lg hover:bg-red-50 transition">
                      ❌ Cancel
                    </button>
                  )}
                </div>
              </div>

              {b.passengers?.length > 0 && (
                <div className="mt-3 pt-3 border-t border-gray-100 dark:border-gray-700">
                  <p className="text-xs text-gray-400 mb-1 uppercase">Passengers</p>
                  <div className="flex flex-wrap gap-2">
                    {b.passengers.map((p, i) => (
                      <span key={i} className="px-3 py-1 bg-gray-100 dark:bg-gray-700 rounded-full text-xs text-gray-600 dark:text-gray-300">
                        {p.name} ({p.age}/{p.gender[0]}){p.seatNumber ? ` · ${p.seatNumber}` : ''}
                      </span>
                    ))}
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
