import { useEffect, useState } from 'react'
import { useParams, useNavigate, Link } from 'react-router-dom'
import { bookingApi } from '../api/bookingApi'
import toast from 'react-hot-toast'

export default function BookingConfirmation() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [booking, setBooking] = useState(null)

  useEffect(() => {
    bookingApi.getById(id)
      .then(r => setBooking(r.data.data))
      .catch(() => navigate('/'))
  }, [id])

  const downloadTicket = async () => {
    try {
      const res = await bookingApi.downloadTicket(id)
      const url = URL.createObjectURL(new Blob([res.data], { type: 'application/pdf' }))
      const a = document.createElement('a')
      a.href = url; a.download = `RailYatra_${booking.pnr}.pdf`
      a.click(); URL.revokeObjectURL(url)
      toast.success('Ticket downloaded!')
    } catch { toast.error('Download failed') }
  }

  if (!booking) return <div className="flex justify-center items-center h-64 text-4xl animate-bounce">🚂</div>

  return (
    <div className="max-w-lg mx-auto px-4 py-12 text-center">
      <div className="text-6xl mb-3 animate-bounce">🎉</div>
      <h1 className="text-3xl font-bold text-green-600 mb-2">Booking Confirmed!</h1>
      <p className="text-gray-500 mb-8 text-sm">A confirmation email has been sent to your inbox.</p>

      <div className="card border-2 border-green-200 p-6 mb-6 text-left">
        {/* PNR */}
        <div className="text-center mb-5">
          <p className="text-xs text-gray-400 uppercase tracking-widest">PNR Number</p>
          <p className="font-mono font-bold text-3xl text-blue-600 tracking-widest mt-1">{booking.pnr}</p>
          <span className="inline-block mt-2 px-3 py-1 bg-green-100 text-green-700 rounded-full text-sm font-bold">
            ✅ {booking.status}
          </span>
        </div>

        {/* Details */}
        <div className="grid grid-cols-2 gap-3 text-sm mb-4">
          {[
            ['Train',  booking.trainName],
            ['Date',   booking.journeyDate],
            ['From',   booking.sourceStation],
            ['To',     booking.destStation],
            ['Class',  booking.classType],
            ['Paid',   `₹${booking.totalAmount}`]
          ].map(([l, v]) => (
            <div key={l}>
              <p className="text-gray-400 text-xs">{l}</p>
              <p className="font-semibold text-gray-800 dark:text-white">{v}</p>
            </div>
          ))}
        </div>

        {/* Passengers */}
        {booking.passengers?.length > 0 && (
          <div className="border-t pt-3">
            <p className="text-xs text-gray-400 mb-2 uppercase tracking-widest">Passengers</p>
            {booking.passengers.map((p, i) => (
              <div key={i} className="flex justify-between text-sm py-1">
                <span className="text-gray-700 dark:text-gray-300">{i + 1}. {p.name} ({p.age}/{p.gender[0]})</span>
                {p.seatNumber && <span className="font-mono text-blue-600 text-xs">{p.seatNumber}</span>}
              </div>
            ))}
          </div>
        )}
      </div>

      <div className="space-y-3">
        <button onClick={downloadTicket}
          className="w-full py-3 bg-blue-600 text-white font-bold rounded-2xl hover:bg-blue-700 transition">
          📄 Download E-Ticket (PDF)
        </button>
        <Link to="/my-bookings"
          className="block w-full py-3 border border-gray-300 text-gray-600 rounded-2xl hover:bg-gray-50 transition text-sm text-center">
          📋 View All Bookings
        </Link>
        <Link to="/" className="block text-sm text-blue-600 hover:underline">← Back to Home</Link>
      </div>
    </div>
  )
}
