import { useEffect, useState } from 'react'
import { bookingApi } from '../api/booking'
import TicketStub from '../components/TicketStub'
import LoadingSpinner from '../components/LoadingSpinner'
import ReviewForm from '../components/ReviewForm'

const STATUS_STYLES = {
  CONFIRMED: 'text-seat-open',
  WATCHED: 'text-marquee',
  CANCELLED: 'text-velvet',
}

export default function MyBookings() {
  const [bookings, setBookings] = useState(null)
  const [error, setError] = useState('')
  const [busyId, setBusyId] = useState(null)
  const [reviewingId, setReviewingId] = useState(null)

  const load = () => bookingApi.myBookings().then(setBookings).catch((e) => setError(e.message))

  useEffect(() => { load() }, [])

  const cancel = async (bookingId) => {
    if (!confirm('Cancel this booking? This can only be done up to 2 hours before showtime.')) return
    setBusyId(bookingId)
    try {
      await bookingApi.cancelBooking(bookingId)
      await load()
    } catch (e) {
      setError(e.message)
    } finally {
      setBusyId(null)
    }
  }

  return (
    <div className="max-w-3xl mx-auto px-4 sm:px-6 py-12">
      <h1 className="font-display font-bold text-3xl mb-8">My Bookings</h1>

      {error && <p className="text-velvet text-sm mb-4">{error}</p>}
      {!bookings && !error && <LoadingSpinner />}
      {bookings && bookings.length === 0 && (
        <p className="text-dim text-sm">You haven't booked anything yet — your tickets will show up here.</p>
      )}

      <div className="flex flex-col gap-6">
        {bookings?.map((b) => (
          <div key={b.bookingId}>
            <TicketStub>
              <div className="flex items-start justify-between gap-4 mb-3">
                <h3 className="font-display font-bold text-lg leading-snug">{b.movieTitle}</h3>
                <span className={`text-xs font-mono font-semibold uppercase ${STATUS_STYLES[b.status] || 'text-dim'}`}>
                  {b.status}
                </span>
              </div>
              <dl className="space-y-1.5 text-sm mb-4">
                <Row label="Theater" value={`${b.theaterName} — ${b.location}`} />
                <Row label="Date & Time" value={`${b.showDate}, ${b.showTime}`} mono />
                <Row label="Screen" value={`${b.screen}`} />
                <Row label="Seats" value={b.seats?.join(', ')} mono />
                <Row label="Amount" value={`₹${b.totalAmount}`} />
              </dl>
              <div className="flex items-center justify-between border-t border-hairline pt-3">
                <span className="font-mono text-xs text-faint tracking-wider">{b.bookingGroup}</span>
                <div className="flex gap-3">
                  {b.status === 'CONFIRMED' && (
                    <button
                      onClick={() => cancel(b.bookingId)}
                      disabled={busyId === b.bookingId}
                      className="text-xs font-semibold text-velvet hover:text-velvet-deep transition-colors disabled:opacity-40"
                    >
                      {busyId === b.bookingId ? 'Cancelling…' : 'Cancel'}
                    </button>
                  )}
                  {b.status === 'WATCHED' && !b.hasReview && (
                    <button
                      onClick={() => setReviewingId(reviewingId === b.bookingId ? null : b.bookingId)}
                      className="text-xs font-semibold text-marquee hover:text-marquee-soft transition-colors"
                    >
                      {reviewingId === b.bookingId ? 'Cancel review' : 'Write a review'}
                    </button>
                  )}
                  {b.hasReview && <span className="text-xs text-dim">Reviewed ✓</span>}
                </div>
              </div>
            </TicketStub>
            {reviewingId === b.bookingId && (
              <ReviewForm bookingId={b.bookingId} onDone={() => { setReviewingId(null); load() }} />
            )}
          </div>
        ))}
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
