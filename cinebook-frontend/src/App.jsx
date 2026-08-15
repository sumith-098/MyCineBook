import { Route, Routes } from 'react-router-dom'
import Navbar from './components/Navbar'
import Footer from './components/Footer'
import ProtectedRoute from './components/ProtectedRoute'

import Home from './pages/Home'
import Movies from './pages/Movies'
import MovieDetail from './pages/MovieDetail'
import Theaters from './pages/Theaters'
import SeatPicker from './pages/SeatPicker'
import Login from './pages/Login'
import Register from './pages/Register'
import MyBookings from './pages/MyBookings'
import BookingConfirmation from './pages/BookingConfirmation'
import OwnerDashboard from './pages/owner/OwnerDashboard'
import AdminDashboard from './pages/admin/AdminDashboard'

export default function App() {
  return (
    <div className="min-h-screen flex flex-col">
      <Navbar />
      <main className="flex-1">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/movies" element={<Movies />} />
          <Route path="/movies/:movieId" element={<MovieDetail />} />
          <Route path="/theaters" element={<Theaters />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          <Route
            path="/book/:showtimeId"
            element={
              <ProtectedRoute allowRoles={['CUSTOMER']}>
                <SeatPicker />
              </ProtectedRoute>
            }
          />
          <Route
            path="/booking-confirmed"
            element={
              <ProtectedRoute allowRoles={['CUSTOMER']}>
                <BookingConfirmation />
              </ProtectedRoute>
            }
          />
          <Route
            path="/my-bookings"
            element={
              <ProtectedRoute allowRoles={['CUSTOMER']}>
                <MyBookings />
              </ProtectedRoute>
            }
          />
          <Route
            path="/owner"
            element={
              <ProtectedRoute allowRoles={['OWNER']}>
                <OwnerDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/admin"
            element={
              <ProtectedRoute allowRoles={['ADMIN']}>
                <AdminDashboard />
              </ProtectedRoute>
            }
          />

          <Route path="*" element={<NotFound />} />
        </Routes>
      </main>
      <Footer />
    </div>
  )
}

function NotFound() {
  return (
    <div className="max-w-md mx-auto px-4 py-32 text-center">
      <p className="font-display font-black text-6xl text-marquee mb-3">404</p>
      <p className="text-dim text-sm">This reel doesn't exist.</p>
    </div>
  )
}
