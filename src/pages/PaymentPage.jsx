import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { bookingApi } from '../api/bookingApi'
import api from '../api/axiosInstance'
import toast from 'react-hot-toast'

export default function PaymentPage() {
  const { bookingId } = useParams()
  const navigate = useNavigate()
  const [booking, setBooking] = useState(null)
  const [loading, setLoading] = useState(true)
  const [paying, setPaying] = useState(false)
  const [method, setMethod] = useState('UPI')

  useEffect(() => {
    bookingApi.getById(bookingId)
      .then(r => setBooking(r.data.data))
      .catch(() => { toast.error('Booking not found'); navigate('/') })
      .finally(() => setLoading(false))
  }, [bookingId])

  const handlePay = async () => {
    setPaying(true)
    try {
      // Step 1: Create order
      const orderRes = await api.post(
        `/payments/create-order?bookingId=${bookingId}`)
      const orderId = orderRes.data.data.orderId

      // Step 2: Simulate payment (no Razorpay needed)
      await new Promise(r => setTimeout(r, 2000)) // 2 sec loading effect

      // Step 3: Verify with simulated data
      await api.post('/payments/verify', {
        razorpay_order_id: orderId,
        razorpay_payment_id: 'pay_simulated_' + Date.now(),
        razorpay_signature: 'simulated'
      })

      toast.success('Payment successful! 🎉')
      navigate(`/confirmation/${bookingId}`)
    } catch (err) {
      toast.error(err.response?.data?.error || 'Payment failed')
      setPaying(false)
    }
  }

  if (loading) return (
    <div className="flex justify-center items-center h-64 text-4xl animate-bounce">🚂</div>
  )

  return (
    <div className="max-w-lg mx-auto px-4 py-12">
      <h1 className="text-2xl font-bold text-gray-800 dark:text-white mb-6">
        Complete Payment
      </h1>

      {/* Booking summary */}
      <div className="card p-6 mb-6">
        {[
          ['PNR', booking?.pnr, 'font-mono font-bold text-blue-600 text-lg'],
          ['Train', booking?.trainName, 'font-semibold'],
          ['Route', `${booking?.sourceStation} → ${booking?.destStation}`, ''],
          ['Date & Class', `${booking?.journeyDate} | ${booking?.classType}`, ''],
          ['Passengers', booking?.passengers?.length, ''],
        ].map(([l, v, cls]) => (
          <div key={l} className="flex justify-between py-2 border-b border-gray-50 last:border-0">
            <span className="text-gray-500 text-sm">{l}</span>
            <span className={`text-sm ${cls || 'text-gray-700'}`}>{v}</span>
          </div>
        ))}
        <div className="flex justify-between pt-3 mt-1">
          <span className="font-bold text-gray-800">Total</span>
          <span className="font-bold text-xl text-blue-600">₹{booking?.totalAmount}</span>
        </div>
      </div>

      {/* Payment method selector */}
      <div className="card p-5 mb-6">
        <p className="font-semibold text-gray-700 mb-3">Select Payment Method</p>
        <div className="grid grid-cols-2 gap-2">
          {['UPI', 'Debit Card', 'Credit Card', 'Net Banking'].map(m => (
            <button key={m} onClick={() => setMethod(m)}
              className={`py-3 rounded-xl border-2 text-sm font-medium transition ${
                method === m
                  ? 'border-blue-600 bg-blue-50 text-blue-700'
                  : 'border-gray-200 text-gray-600 hover:border-gray-300'
              }`}>
              {m === 'UPI' ? '📱 ' : m === 'Debit Card' ? '💳 ' : m === 'Credit Card' ? '💳 ' : '🏦 '}
              {m}
            </button>
          ))}
        </div>

        {/* UPI input */}
        {method === 'UPI' && (
          <div className="mt-4">
            <label className="block text-xs text-gray-500 mb-1">UPI ID</label>
            <input placeholder="yourname@upi" className="input-field" />
          </div>
        )}

        {/* Card input */}
        {(method === 'Debit Card' || method === 'Credit Card') && (
          <div className="mt-4 space-y-3">
            <input placeholder="Card Number: 4111 1111 1111 1111"
              className="input-field" maxLength={19} />
            <div className="grid grid-cols-2 gap-2">
              <input placeholder="MM/YY" className="input-field" />
              <input placeholder="CVV" className="input-field" maxLength={3} />
            </div>
            <input placeholder="Name on card" className="input-field" />
          </div>
        )}
      </div>

      {/* Security badges */}
      <div className="flex justify-center gap-4 text-xs text-gray-400 mb-6">
        <span>🔒 256-bit SSL</span>
        <span>✅ Secure Payment</span>
        <span>🛡️ PCI DSS</span>
      </div>

      <button onClick={handlePay} disabled={paying}
        className="w-full py-4 bg-green-600 hover:bg-green-700 text-white
                   font-bold text-lg rounded-2xl transition disabled:opacity-60">
        {paying
          ? '⏳ Processing Payment...'
          : `💳 Pay ₹${booking?.totalAmount}`}
      </button>

      <button onClick={() => navigate(-1)}
        className="w-full mt-3 py-3 border border-gray-300 text-gray-600
                   rounded-2xl hover:bg-gray-50 transition text-sm">
        ← Back
      </button>
    </div>
  )
}
