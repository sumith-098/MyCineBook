import { useCallback, useEffect, useMemo, useState } from 'react'
import { useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { catalogApi } from '../api/catalog'
import { bookingApi } from '../api/booking'
import { paymentApi } from '../api/payment'
import { useAuth } from '../context/AuthContext'
import { useRazorpayScript } from '../hooks/useRazorpayScript'
import SeatGrid from '../components/SeatGrid'
import TicketStub from '../components/TicketStub'
import LoadingSpinner from '../components/LoadingSpinner'

export default function SeatPicker() {
  const { showtimeId } = useParams()
  const [searchParams] = useSearchParams()
  const movieId = searchParams.get('movieId')
  const navigate = useNavigate()
  const { isAuthenticated, user } = useAuth()
  const razorpayReady = useRazorpayScript()

  const [info, setInfo] = useState(null)
  const [bookedSeats, setBookedSeats] = useState([])
  const [selected, setSelected] = useState([])
  const [quote, setQuote] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([
      catalogApi.showtimeInfo(showtimeId),
      bookingApi.bookedSeats(showtimeId),
    ]).then(([showtimeInfo, taken]) => {
      setInfo(showtimeInfo)
      setBookedSeats(taken)
    }).catch((e) => setError(e.message))
  }, [showtimeId])

  useEffect(() => {
    if (selected.length === 0) { setQuote(null); return }
    const handle = setTimeout(() => {
      bookingApi.quote({ movieId: Number(movieId), showtimeId: Number(showtimeId), seats: selected })
        .then(setQuote)
        .catch((e) => setError(e.message))
    }, 200)
    return () => clearTimeout(handle)
  }, [selected, movieId, showtimeId])

  const toggleSeat = useCallback((seatId) => {
    setError('')
    setSelected((prev) => (prev.includes(seatId) ? prev.filter((s) => s !== seatId) : [...prev, seatId]))
  }, [])

  const requireLogin = () => {
    navigate('/login', { state: { from: { pathname: `/book/${showtimeId}?movieId=${movieId}` } } })
  }

  const bookWithRazorpay = async () => {
    if (!razorpayReady) { setError('Payment gateway still loading — try again in a moment.'); return }
    setSubmitting(true)
    setError('')
    try {
      const order = await paymentApi.createOrder({ movieId: Number(movieId), showtimeId: Number(showtimeId), seats: selected })
      const rzp = new window.Razorpay({
        key: order.keyId,
        amount: order.amountPaise,
        currency: order.currency,
        name: 'CineBook',
        description: info?.movieTitle,
        order_id: order.orderId,
        prefill: { email: order.email || user?.email, name: user?.name },
        theme: { color: '#f2b705' },
        handler: async (response) => {
          try {
            const result = await paymentApi.verify({
              razorpayOrderId: response.razorpay_order_id,
              razorpayPaymentId: response.razorpay_payment_id,
              razorpaySignature: response.razorpay_signature,
            })
            navigate('/booking-confirmed', { state: { booking: { ...result, seats: selected, movieTitle: info?.movieTitle, totalAmount: quote?.totalAmount } } })
          } catch (e) {
            setError(e.message)
          } finally {
            setSubmitting(false)
          }
        },
        modal: { ondismiss: () => setSubmitting(false) },
      })
      rzp.open()
    } catch (e) {
      setError(e.message)
      setSubmitting(false)
    }
  }

  const handleConfirm = () => {
    if (!isAuthenticated) { requireLogin(); return }
    bookWithRazorpay()
  }

  if (error && !info) return <div className="max-w-3xl mx-auto px-4 py-20 text-center text-velvet">{error}</div>
  if (!info) return <LoadingSpinner label="Loading the seat map…" />

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 py-10">
      <div className="mb-8">
        <h1 className="font-display font-bold text-2xl sm:text-3xl">{info.movieTitle}</h1>
        <p className="text-dim text-sm mt-1 font-mono">
          {info.showDate} · {info.showTime} · {info.screen}
        </p>
      </div>

      <div className="grid lg:grid-cols-[1fr_360px] gap-10 items-start">
        <div className="rounded-2xl border border-hairline bg-surface p-6 sm:p-8">
          <SeatGrid
            layout={info.layout}
            catPrices={info.catPrices}
            bookedSeats={bookedSeats}
            selected={selected}
            onToggle={toggleSeat}
          />
        </div>

        <div className="lg:sticky lg:top-24">
          <TicketStub>
            <p className="text-[11px] tracking-[0.2em] text-faint font-mono uppercase mb-1">Booking Summary</p>
            <h3 className="font-display font-bold text-lg mb-4 leading-snug">{info.movieTitle}</h3>

            <dl className="space-y-2 text-sm mb-5">
              <Row label="Date & Time" value={`${info.showDate}, ${info.showTime}`} mono />
              <Row label="Screen" value={info.screen} />
              <Row label="Seats" value={selected.length ? selected.sort().join(', ') : '—'} mono />
            </dl>

            {selected.length > 0 && (
              <div className="border-t border-hairline pt-4 mb-5">
                {quote?.seatDetails?.map((s) => (
                  <div key={s.seat} className="flex justify-between text-xs text-dim mb-1 font-mono">
                    <span>{s.seat} · {s.category}</span>
                    <span>₹{s.price}</span>
                  </div>
                ))}
              </div>
            )}

            <div className="flex justify-between items-baseline mb-6">
              <span className="text-sm text-dim">Total Amount</span>
              <span className="font-display font-bold text-2xl text-marquee">₹{quote?.totalAmount ?? '0'}</span>
            </div>

            <p className="text-[11px] text-faint uppercase tracking-wider mb-4 flex items-center gap-1.5">
              <svg viewBox="0 0 24 24" className="w-3.5 h-3.5" fill="none" stroke="currentColor" strokeWidth="2">
                <rect x="3" y="11" width="18" height="10" rx="2" /><path d="M7 11V7a5 5 0 0110 0v4" />
              </svg>
              Secured checkout via Razorpay
            </p>

            {error && <p className="text-velvet text-xs mb-3">{error}</p>}

            <button
              onClick={handleConfirm}
              disabled={selected.length === 0 || submitting}
              className="w-full bg-marquee text-void font-bold py-3.5 rounded-full hover:bg-marquee-soft
                         transition-colors shadow-glow-gold disabled:opacity-30 disabled:cursor-not-allowed disabled:shadow-none"
            >
              {submitting ? 'Processing…' : isAuthenticated ? 'Pay & Confirm Booking' : 'Log In to Book'}
            </button>
          </TicketStub>
        </div>
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