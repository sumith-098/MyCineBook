import { Link, Navigate, useLocation } from 'react-router-dom'
import TicketStub from '../components/TicketStub'

export default function BookingConfirmation() {
  const { state } = useLocation()
  const booking = state?.booking

  if (!booking) return <Navigate to="/my-bookings" replace />

  return (
    <div className="max-w-lg mx-auto px-4 py-16 text-center">
      <div className="w-16 h-16 rounded-full bg-marquee/10 text-marquee flex items-center justify-center mx-auto mb-6">
        <svg viewBox="0 0 24 24" className="w-8 h-8" fill="none" stroke="currentColor" strokeWidth="2.5">
          <path d="M5 13l4 4L19 7" />
        </svg>
      </div>
      <h1 className="font-display font-black text-3xl mb-2">You're all set! 🎬</h1>
      <p className="text-dim text-sm mb-10">Your ticket is confirmed. Check your email for the full receipt.</p>

      <TicketStub className="text-left">
        <p className="text-[11px] tracking-[0.2em] text-faint font-mono uppercase mb-1">Confirmed</p>
        <h3 className="font-display font-bold text-xl mb-4">{booking.movieTitle}</h3>
        <dl className="space-y-2 text-sm mb-5">
          {booking.showDate && <Row label="Date & Time" value={`${booking.showDate}, ${booking.showTime}`} />}
          {booking.seats && <Row label="Seats" value={Array.isArray(booking.seats) ? booking.seats.join(', ') : booking.seats} mono />}
          {(booking.totalAmount != null) && <Row label="Amount" value={`₹${booking.totalAmount}`} />}
        </dl>
        <div className="border-t border-hairline pt-4">
          <p className="text-[11px] text-faint uppercase tracking-wider mb-1">Booking Reference</p>
          <p className="font-mono font-bold text-xl text-marquee tracking-widest">
            {booking.bookingGroup || booking.bookingRef}
          </p>
        </div>
      </TicketStub>

      <div className="flex items-center justify-center gap-4 mt-10">
        <Link to="/my-bookings" className="text-sm font-semibold bg-marquee text-void px-6 py-3 rounded-full hover:bg-marquee-soft transition-colors">
          View My Bookings
        </Link>
        <Link to="/" className="text-sm font-medium text-dim hover:text-ivory transition-colors">Back home</Link>
      </div>
    </div>
  )
}

function Row({ label, value, mono }) {
  return (
    <div className="flex justify-between gap-4">
      <dt className="text-dim">{label}</dt>
      <dd className={`text-right text-ivory ${mono ? 'font-mono' : ''}`}>{value}</dd>
    </div>
  )
}
