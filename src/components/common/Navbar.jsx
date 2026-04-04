import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../../store/authStore'
import toast from 'react-hot-toast'

export default function Navbar() {
  const [open, setOpen] = useState(false)
  const [dark, setDark] = useState(false)
  const { isAuthenticated, user, logout } = useAuthStore()
  const navigate = useNavigate()

  const handleLogout = () => { logout(); toast.success('Logged out'); navigate('/') }
  const toggleDark = () => { setDark(!dark); document.documentElement.classList.toggle('dark') }

  return (
    <nav className="bg-white dark:bg-gray-800 shadow-sm sticky top-0 z-50 border-b border-gray-100 dark:border-gray-700">
      <div className="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between">
        <Link to="/" className="flex items-center gap-2">
          <span className="text-2xl">🚂</span>
          <span className="text-xl font-extrabold text-blue-700 dark:text-blue-400">
            Rail<span className="text-orange-500">Yatra</span>
          </span>
        </Link>

        <div className="hidden md:flex items-center gap-6 text-sm font-medium">
          <Link to="/" className="text-gray-600 dark:text-gray-300 hover:text-blue-600 transition">Home</Link>
          <Link to="/pnr" className="text-gray-600 dark:text-gray-300 hover:text-blue-600 transition">PNR Status</Link>
          {isAuthenticated && <Link to="/my-bookings" className="text-gray-600 dark:text-gray-300 hover:text-blue-600 transition">My Bookings</Link>}
          {user?.role === 'ADMIN' && <Link to="/admin" className="text-purple-600 font-bold">Admin</Link>}
        </div>

        <div className="hidden md:flex items-center gap-3">
          <button onClick={toggleDark} className="p-2 rounded-full bg-gray-100 dark:bg-gray-700 text-sm">
            {dark ? '☀️' : '🌙'}
          </button>
          {isAuthenticated ? (
            <div className="flex items-center gap-3">
              <span className="text-sm text-gray-600 dark:text-gray-300">
                👋 {user?.name?.split(' ')[0]}{user?.isPremium && ' ⭐'}
              </span>
              <Link to="/profile" className="text-sm text-blue-600 hover:underline">Profile</Link>
              <button onClick={handleLogout} className="px-3 py-1.5 text-sm bg-red-500 text-white rounded-lg hover:bg-red-600 transition">Logout</button>
            </div>
          ) : (
            <div className="flex gap-2">
              <Link to="/login" className="px-4 py-2 text-sm border border-blue-600 text-blue-600 rounded-lg hover:bg-blue-50 transition">Login</Link>
              <Link to="/register" className="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition">Register</Link>
            </div>
          )}
        </div>

        <button className="md:hidden p-2 text-xl" onClick={() => setOpen(!open)}>{open ? '✕' : '☰'}</button>
      </div>

      {open && (
        <div className="md:hidden border-t border-gray-100 dark:border-gray-700 pb-4 px-4 space-y-1">
          {[['/', 'Home'], ['/pnr', 'PNR Status']].map(([to, label]) => (
            <Link key={to} to={to} onClick={() => setOpen(false)}
              className="block py-2 text-gray-700 dark:text-gray-300 hover:text-blue-600">{label}</Link>
          ))}
          {isAuthenticated
            ? <>
                <Link to="/my-bookings" onClick={() => setOpen(false)} className="block py-2 text-gray-700 dark:text-gray-300">My Bookings</Link>
                <Link to="/profile"    onClick={() => setOpen(false)} className="block py-2 text-gray-700 dark:text-gray-300">Profile</Link>
                <button onClick={() => { handleLogout(); setOpen(false) }} className="block py-2 text-red-600 w-full text-left">Logout</button>
              </>
            : <>
                <Link to="/login"    onClick={() => setOpen(false)} className="block py-2 text-blue-600">Login</Link>
                <Link to="/register" onClick={() => setOpen(false)} className="block py-2 bg-blue-600 text-white text-center rounded-lg">Register</Link>
              </>
          }
        </div>
      )}
    </nav>
  )
}
