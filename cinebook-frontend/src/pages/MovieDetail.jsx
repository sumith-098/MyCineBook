import { useEffect, useMemo, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { catalogApi } from '../api/catalog'
import { bookingApi } from '../api/booking'
import LoadingSpinner from '../components/LoadingSpinner'
import RatingBadge from '../components/RatingBadge'

export default function MovieDetail() {
  const { movieId } = useParams()
  const navigate = useNavigate()
  const [movie, setMovie] = useState(null)
  const [reviews, setReviews] = useState(null)
  const [error, setError] = useState('')
  const [selectedDate, setSelectedDate] = useState(null)
  const [selectedShowtime, setSelectedShowtime] = useState(null)

  useEffect(() => {
    setMovie(null)
    catalogApi.movieDetail(movieId).then((data) => {
      setMovie(data)
      const firstDate = data.showtimes?.[0]?.showDate
      setSelectedDate(firstDate || null)
    }).catch((e) => setError(e.message))
    bookingApi.movieReviews(movieId).then(setReviews).catch(() => setReviews(null))
  }, [movieId])

  const dates = useMemo(() => {
    if (!movie?.showtimes) return []
    return [...new Set(movie.showtimes.map((s) => s.showDate))]
  }, [movie])

  const showtimesForDate = useMemo(() => {
    if (!movie?.showtimes || !selectedDate) return []
    return movie.showtimes.filter((s) => s.showDate === selectedDate)
  }, [movie, selectedDate])

  if (error) return <div className="max-w-3xl mx-auto px-4 py-20 text-center text-velvet">{error}</div>
  if (!movie) return <LoadingSpinner label="Fetching showtimes…" />

  const goToSeats = () => {
    if (!selectedShowtime) return
    navigate(`/book/${selectedShowtime}?movieId=${movieId}`)
  }

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 py-12">
      <div className="grid md:grid-cols-[280px_1fr] gap-10">
        {/* Poster */}
        <div className="relative">
          <div className="aspect-[2/3] rounded-2xl overflow-hidden bg-surface-2 border border-hairline sticky top-24">
            {movie.posterUrl ? (
              <img src={movie.posterUrl} alt={movie.title} className="w-full h-full object-cover" />
            ) : (
              <div className="w-full h-full flex items-center justify-center text-faint font-display px-4 text-center">{movie.title}</div>
            )}
            <RatingBadge rating={movie.avgRating} count={movie.reviewCount} />
          </div>
        </div>

        {/* Details */}
        <div>
          <h1 className="font-display font-black text-3xl sm:text-4xl leading-tight">{movie.title}</h1>
          <p className="text-dim text-sm mt-2">
            {[movie.genre, movie.language, movie.duration].filter(Boolean).join('  ·  ')}
          </p>

          {movie.description && (
            <p className="text-ivory/85 text-sm leading-relaxed mt-5 max-w-2xl">{movie.description}</p>
          )}

          <div className="mt-6 flex items-center gap-2 text-sm text-dim">
            <svg viewBox="0 0 24 24" className="w-4 h-4 text-marquee" fill="currentColor"><path d="M12 2C8.1 2 5 5.1 5 9c0 5.2 7 13 7 13s7-7.8 7-13c0-3.9-3.1-7-7-7zm0 9.5A2.5 2.5 0 1112 6a2.5 2.5 0 010 5.5z" /></svg>
            {movie.theaterName} — {movie.location}
          </div>

          {(movie.priceMin || movie.priceMax) && (
            <p className="text-sm mt-2 text-dim">
              Tickets from <span className="text-marquee font-mono font-semibold">₹{movie.priceMin}</span>
              {movie.priceMax !== movie.priceMin && <> to ₹{movie.priceMax}</>}
            </p>
          )}

          {/* Date picker */}
          {dates.length > 0 ? (
            <div className="mt-9">
              <h2 className="font-display font-semibold text-lg mb-3">Pick a date</h2>
              <div className="flex gap-2.5 flex-wrap">
                {dates.map((d) => (
                  <button
                    key={d}
                    onClick={() => { setSelectedDate(d); setSelectedShowtime(null) }}
                    className={`px-4 py-2.5 rounded-xl text-sm font-mono border transition-colors
                      ${selectedDate === d
                        ? 'bg-marquee text-void border-marquee font-bold'
                        : 'border-hairline text-dim hover:border-marquee/50 hover:text-ivory'}`}
                  >
                    {formatDatePill(d)}
                  </button>
                ))}
              </div>

              <h2 className="font-display font-semibold text-lg mt-7 mb-3">Showtime</h2>
              <div className="flex gap-2.5 flex-wrap">
                {showtimesForDate.map((s) => (
                  <button
                    key={s.showtimeId}
                    onClick={() => setSelectedShowtime(s.showtimeId)}
                    className={`px-4 py-2.5 rounded-xl text-sm font-mono border transition-colors
                      ${selectedShowtime === s.showtimeId
                        ? 'bg-marquee text-void border-marquee font-bold'
                        : 'border-hairline text-dim hover:border-marquee/50 hover:text-ivory'}`}
                  >
                    {formatTime(s.showTime)} <span className="opacity-60">· {s.screen}</span>
                  </button>
                ))}
              </div>

              <button
                onClick={goToSeats}
                disabled={!selectedShowtime}
                className="mt-8 w-full sm:w-auto bg-marquee text-void font-bold px-8 py-3.5 rounded-full
                           hover:bg-marquee-soft transition-colors shadow-glow-gold
                           disabled:opacity-30 disabled:cursor-not-allowed disabled:shadow-none"
              >
                Select Seats →
              </button>
            </div>
          ) : (
            <p className="text-dim text-sm mt-9">No upcoming showtimes for this movie right now.</p>
          )}
        </div>
      </div>

      {/* Reviews */}
      <section className="mt-16 max-w-3xl">
        <h2 className="font-display font-bold text-2xl mb-5">
          Reviews {reviews?.totalReviews ? <span className="text-dim text-base font-sans">({reviews.totalReviews})</span> : null}
        </h2>
        {!reviews || reviews.reviews?.length === 0 ? (
          <p className="text-dim text-sm">No reviews yet — be the first after you watch it.</p>
        ) : (
          <div className="flex flex-col gap-4">
            {reviews.reviews.map((r) => (
              <div key={r.reviewId} className="border border-hairline rounded-xl p-5 bg-surface">
                <div className="flex items-center justify-between mb-1.5">
                  <span className="font-medium text-sm">{r.custName}</span>
                  <span className="text-marquee text-xs font-mono">★ {r.rating}/5</span>
                </div>
                {r.reviewText && <p className="text-sm text-dim leading-relaxed">{r.reviewText}</p>}
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  )
}

function formatDatePill(dateStr) {
  const d = new Date(dateStr + 'T00:00:00')
  return d.toLocaleDateString('en-US', { weekday: 'short', day: '2-digit', month: 'short' })
}

function formatTime(timeStr) {
  const [h, m] = timeStr.split(':')
  const hour = parseInt(h, 10)
  const suffix = hour >= 12 ? 'PM' : 'AM'
  const hour12 = hour % 12 === 0 ? 12 : hour % 12
  return `${hour12}:${m} ${suffix}`
}
