import { useState, useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'
import { bookingApi } from '../api/bookingApi'
import { getStatusColor } from '../utils/formatters'
import toast from 'react-hot-toast'

export default function PNRCheck() {
  const [searchParams] = useSearchParams()
  const [pnr, setPnr] = useState(searchParams.get('pnr') || '')
  const [result, setResult] = useState(null)
  const [prediction, setPrediction] = useState(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => { if (pnr.length === 10) check() }, [])

  const check = async () => {
    if (pnr.length !== 10) { toast.error('PNR must be 10 digits'); return }
    setLoading(true); setResult(null); setPrediction(null)
    try {
      const res = await bookingApi.getPNR(pnr)
      const data = res.data.data
      setResult(data)
      if (data.status === 'WAITLISTED' && data.waitlistNumber) {
        bookingApi.predictWL({
          trainId: data.id, journeyDate: data.journeyDate,
          classType: data.classType, wlNumber: data.waitlistNumber
        }).then(r => setPrediction(r.data.data)).catch(() => {})
      }
    } catch { toast.error('PNR not found') }
    finally { setLoading(false) }
  }

  return (
    <div className="max-w-xl mx-auto px-4 py-12">
      <h1 className="text-3xl font-bold text-gray-800 dark:text-white mb-2">PNR Status</h1>
      <p className="text-gray-500 text-sm mb-8">Check your booking status in real time — no login needed.</p>

      <div className="flex gap-2 mb-8">
        <input value={pnr}
          onChange={e => setPnr(e.target.value.replace(/\D/g, '').slice(0, 10))}
          onKeyDown={e => e.key === 'Enter' && check()}
          placeholder="Enter 10-digit PNR"
          className="flex-1 input-field text-center font-mono tracking-widest text-lg" />
        <button onClick={check} disabled={loading} className="btn-primary px-6">
          {loading ? '⏳' : 'Check'}
        </button>
      </div>

      {result && (
        <div className={`rounded-2xl border-2 p-5 mb-4 ${getStatusColor(result.status)}`}>
          <div className="flex justify-between items-start mb-4">
            <div>
              <p className="text-xs opacity-60 uppercase tracking-widest">PNR</p>
              <p className="font-mono font-bold text-2xl">{result.pnr}</p>
            </div>
            <span className={`px-3 py-1 rounded-full text-sm font-bold border ${getStatusColor(result.status)}`}>
              {result.status === 'CONFIRMED' ? '✅' : result.status === 'WAITLISTED' ? '⏳' : '❌'} {result.status}
            </span>
          </div>

          <div className="grid grid-cols-2 gap-3 text-sm mb-4">
            {[
              ['Train',  result.trainName],
              ['Date',   result.journeyDate],
              ['From',   result.sourceStation],
              ['To',     result.destStation],
              ['Class',  result.classType],
              ...(result.waitlistNumber ? [['WL No.', `WL/${result.waitlistNumber}`]] : [])
            ].map(([l, v]) => (
              <div key={l}>
                <p className="opacity-60 text-xs">{l}</p>
                <p className="font-semibold">{v}</p>
              </div>
            ))}
          </div>

          {result.passengers?.length > 0 && (
            <div className="border-t border-current border-opacity-20 pt-3">
              <p className="text-xs opacity-60 mb-2 uppercase">Passengers</p>
              {result.passengers.map((p, i) => (
                <p key={i} className="text-sm">{i + 1}. {p.name} ({p.age}/{p.gender[0]}){p.seatNumber ? ` · Seat: ${p.seatNumber}` : ''}</p>
              ))}
            </div>
          )}
        </div>
      )}

      {prediction && (
        <div className="bg-purple-50 border border-purple-200 rounded-2xl p-5">
          <h3 className="font-bold text-purple-800 mb-3">🔮 AI WL Confirmation Prediction</h3>
          <div className="flex items-center gap-4 mb-3">
            <span className="text-4xl font-black text-purple-600">{prediction.percentage}</span>
            <p className="text-sm text-purple-700">{prediction.label}</p>
          </div>
          <div className="w-full bg-purple-200 rounded-full h-2.5">
            <div className="bg-purple-600 h-2.5 rounded-full transition-all duration-500"
              style={{ width: prediction.percentage }} />
          </div>
          <p className="text-xs text-purple-500 mt-2">Based on historical cancellation rates and days to journey.</p>
        </div>
      )}
    </div>
  )
}
