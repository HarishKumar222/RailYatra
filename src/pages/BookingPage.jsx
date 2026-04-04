import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useBookingStore } from '../store/bookingStore'
import { bookingApi } from '../api/bookingApi'
import toast from 'react-hot-toast'

const BERTHS = ['LOWER','MIDDLE','UPPER','SIDE_LOWER','SIDE_UPPER']
const emptyPassenger = () => ({ name: '', age: '', gender: 'MALE', berthPref: 'LOWER' })

export default function BookingPage() {
  const navigate = useNavigate()
  const { selectedTrain, selectedClass, journeyDate, setCurrentBooking } = useBookingStore()
  const [passengers, setPassengers] = useState([emptyPassenger()])
  const [loading, setLoading] = useState(false)

  if (!selectedTrain || !selectedClass) { navigate('/'); return null }

  const classInfo = {
    SL: selectedTrain.sl, '3A': selectedTrain.threeA,
    '2A': selectedTrain.twoA, '1A': selectedTrain.oneA
  }[selectedClass]

  const update = (i, field, value) => {
    const arr = [...passengers]
    arr[i] = { ...arr[i], [field]: value }
    setPassengers(arr)
  }

  const handleBook = async () => {
    for (const p of passengers) {
      if (!p.name.trim()) { toast.error('Enter passenger name'); return }
      if (!p.age || parseInt(p.age) < 1 || parseInt(p.age) > 125) { toast.error('Enter valid age'); return }
    }
    setLoading(true)
    try {
      const res = await bookingApi.create({
        trainId: selectedTrain.id, journeyDate, classType: selectedClass,
        passengers: passengers.map(p => ({ ...p, age: parseInt(p.age) }))
      })
      setCurrentBooking(res.data.data)
      toast.success('Booking created! Proceeding to payment...')
      navigate(`/payment/${res.data.data.id}`)
    } catch (err) {
      toast.error(err.response?.data?.error || 'Booking failed')
    } finally { setLoading(false) }
  }

  const baseFare  = parseFloat(classInfo?.baseFare || 0)
  const totalFare = (baseFare * passengers.length + 15).toFixed(2)

  return (
    <div className="max-w-2xl mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-800 dark:text-white mb-6">Book Your Ticket</h1>

      {/* Train summary */}
      <div className="bg-blue-50 dark:bg-blue-900/20 rounded-2xl p-5 mb-6 border border-blue-100 dark:border-blue-800">
        <div className="grid grid-cols-2 gap-3 text-sm">
          {[
            ['Train', `${selectedTrain.trainName} (${selectedTrain.trainNumber})`],
            ['Route', `${selectedTrain.sourceStation} → ${selectedTrain.destStation}`],
            ['Date',  journeyDate],
            ['Class', selectedClass + (classInfo ? ` — ₹${classInfo.baseFare}/person` : '')]
          ].map(([l, v]) => (
            <div key={l}>
              <p className="text-xs text-gray-400">{l}</p>
              <p className="font-semibold text-gray-800 dark:text-white">{v}</p>
            </div>
          ))}
        </div>
      </div>

      {/* Passenger forms */}
      <div className="space-y-4 mb-4">
        {passengers.map((p, i) => (
          <div key={i} className="card p-5">
            <div className="flex justify-between items-center mb-4">
              <h3 className="font-semibold text-gray-700 dark:text-white">Passenger {i + 1}</h3>
              {i > 0 && (
                <button onClick={() => setPassengers(passengers.filter((_, j) => j !== i))}
                  className="text-red-500 text-sm hover:underline">Remove</button>
              )}
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div className="sm:col-span-2">
                <label className="block text-xs text-gray-500 mb-1">Full Name (as per ID)</label>
                <input value={p.name} onChange={e => update(i, 'name', e.target.value)}
                  placeholder="Full name" className="input-field" />
              </div>
              <div>
                <label className="block text-xs text-gray-500 mb-1">Age</label>
                <input type="number" min="1" max="125" value={p.age}
                  onChange={e => update(i, 'age', e.target.value)} className="input-field" />
              </div>
              <div>
                <label className="block text-xs text-gray-500 mb-1">Gender</label>
                <select value={p.gender} onChange={e => update(i, 'gender', e.target.value)} className="input-field">
                  <option value="MALE">Male</option>
                  <option value="FEMALE">Female</option>
                  <option value="TRANSGENDER">Other</option>
                </select>
              </div>
              <div className="sm:col-span-2">
                <label className="block text-xs text-gray-500 mb-1">Berth Preference</label>
                <select value={p.berthPref} onChange={e => update(i, 'berthPref', e.target.value)} className="input-field">
                  {BERTHS.map(b => <option key={b} value={b}>{b.replace('_', ' ')}</option>)}
                </select>
              </div>
            </div>
          </div>
        ))}
      </div>

      {passengers.length < 6 && (
        <button onClick={() => setPassengers([...passengers, emptyPassenger()])}
          className="w-full py-3 border-2 border-dashed border-blue-300 text-blue-600 rounded-2xl hover:bg-blue-50 text-sm font-medium transition mb-6">
          + Add Passenger (max 6)
        </button>
      )}

      {/* Fare summary */}
      <div className="card p-5 mb-6">
        <h3 className="font-bold text-gray-800 dark:text-white mb-3">Fare Summary</h3>
        <div className="space-y-2 text-sm">
          <div className="flex justify-between text-gray-500">
            <span>Base Fare × {passengers.length}</span>
            <span>₹{(baseFare * passengers.length).toFixed(2)}</span>
          </div>
          <div className="flex justify-between text-gray-500">
            <span>Convenience Fee</span><span>₹15.00</span>
          </div>
          <div className="flex justify-between font-bold text-lg text-gray-800 dark:text-white border-t pt-2 mt-2">
            <span>Total</span><span className="text-blue-600">₹{totalFare}</span>
          </div>
        </div>
      </div>

      <button onClick={handleBook} disabled={loading} className="btn-primary w-full py-4 text-base">
        {loading ? '⏳ Creating Booking...' : '💳 Proceed to Payment'}
      </button>
    </div>
  )
}
