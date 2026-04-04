import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import { Toaster } from 'react-hot-toast'
import Navbar from './components/common/Navbar'
import Footer from './components/common/Footer'
import Home from './pages/Home'
import Login from './pages/Login'
import Register from './pages/Register'
import SearchResults from './pages/SearchResults'
import BookingPage from './pages/BookingPage'
import PaymentPage from './pages/PaymentPage'
import BookingConfirmation from './pages/BookingConfirmation'
import MyBookings from './pages/MyBookings'
import PNRCheck from './pages/PNRCheck'
import Profile from './pages/Profile'
import AdminDashboard from './pages/admin/AdminDashboard'
import { useAuthStore } from './store/authStore'

const ProtectedRoute = ({ children, adminOnly = false }) => {
  const { isAuthenticated, user } = useAuthStore()
  if (!isAuthenticated) return <Navigate to="/login" replace />
  if (adminOnly && user?.role !== 'ADMIN') return <Navigate to="/" replace />
  return children
}

export default function App() {
  return (
    <Router>
      <div className="min-h-screen bg-gray-50 dark:bg-gray-900 flex flex-col">
        <Navbar />
        <main className="flex-1">
          <Routes>
            <Route path="/"                   element={<Home />} />
            <Route path="/login"              element={<Login />} />
            <Route path="/register"           element={<Register />} />
            <Route path="/search"             element={<SearchResults />} />
            <Route path="/pnr"                element={<PNRCheck />} />
            <Route path="/booking"            element={<ProtectedRoute><BookingPage /></ProtectedRoute>} />
            <Route path="/payment/:bookingId" element={<ProtectedRoute><PaymentPage /></ProtectedRoute>} />
            <Route path="/confirmation/:id"   element={<ProtectedRoute><BookingConfirmation /></ProtectedRoute>} />
            <Route path="/my-bookings"        element={<ProtectedRoute><MyBookings /></ProtectedRoute>} />
            <Route path="/profile"            element={<ProtectedRoute><Profile /></ProtectedRoute>} />
            <Route path="/admin"              element={<ProtectedRoute adminOnly><AdminDashboard /></ProtectedRoute>} />
          </Routes>
        </main>
        <Footer />
        <Toaster position="top-right" toastOptions={{
           duration: 4000,
           success: { style: { background: '#16a34a', color: 'white' } },
           error:   { style: { background: '#dc2626', color: 'white' }, duration: 5000 }
          }} />
      </div>
    </Router>
  )
}
