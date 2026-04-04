export const formatDuration = (mins) => {
  const h = Math.floor(mins / 60), m = mins % 60
  return m > 0 ? `${h}h ${m}m` : `${h}h`
}
export const formatCurrency = (n) =>
  new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(n)
export const getStatusColor = (s) => ({
  CONFIRMED:  'bg-green-100 text-green-800',
  WAITLISTED: 'bg-yellow-100 text-yellow-800',
  CANCELLED:  'bg-red-100 text-red-800',
  PENDING:    'bg-blue-100 text-blue-800'
}[s] || 'bg-gray-100 text-gray-700')
