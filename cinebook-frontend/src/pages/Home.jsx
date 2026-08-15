import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { catalogApi } from '../api/catalog'
import MovieCard from '../components/MovieCard'
import LoadingSpinner from '../components/LoadingSpinner'

export default function Home() {
  const [movies, setMovies] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    catalogApi.featuredMovies().then(setMovies).catch((e) => setError(e.message))
  }, [])

  return (
    <div>
      {/* ── Hero: the wordmark treated like an actual marquee sign, backed by a soft
          poster collage rather than a stock photo or gradient blob. ── */}
      <section className="relative overflow-hidden border-b border-hairline">
        <div className="absolute inset-0 -z-10">
          <div className="absolute inset-0 bg-gradient-to-b from-void via-void/95 to-void" />
          {movies?.slice(0, 6).map((m, i) => (
            <img
              key={m.movieId}
              src={m.posterUrl}
              alt=""
              aria-hidden
              className="hidden lg:block absolute w-40 aspect-[2/3] object-cover rounded-lg opacity-20 blur-[1px]"
              style={{
                top: `${(i % 2) * 40 + 8}%`,
                left: `${i * 17 + 2}%`,
                transform: `rotate(${i % 2 === 0 ? -6 : 5}deg)`,
              }}
            />
          ))}
        </div>

        <div className="max-w-7xl mx-auto px-4 sm:px-6 py-24 sm:py-32 text-center">
          <span className="inline-flex items-center gap-2 text-[11px] tracking-[0.25em] text-marquee font-mono uppercase mb-5">
            <span className="w-1.5 h-1.5 rounded-full bg-marquee animate-pulse" />
            Now booking
          </span>
          <h1 className="font-display font-black text-5xl sm:text-7xl leading-[0.95] tracking-tight">
            Book the movie.
            <br />
            <span className="text-marquee">Skip the queue.</span>
          </h1>
          <p className="mt-6 text-dim text-base sm:text-lg max-w-xl mx-auto">
            Browse what's showing near you, pick your seats, and walk in with your ticket already waiting.
          </p>
          <div className="mt-9 flex items-center justify-center gap-4">
            <Link
              to="/movies"
              className="bg-marquee text-void font-semibold px-7 py-3.5 rounded-full hover:bg-marquee-soft transition-colors shadow-glow-gold"
            >
              Browse Movies
            </Link>
            <Link
              to="/theaters"
              className="border border-hairline text-ivory font-medium px-7 py-3.5 rounded-full hover:border-marquee/50 hover:text-marquee transition-colors"
            >
              Find a Theater
            </Link>
          </div>
        </div>

        <div className="sprocket-divider" aria-hidden>
          {Array.from({ length: 24 }, (_, i) => <span key={i} />)}
        </div>
      </section>

      {/* ── Now showing ── */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 py-14">
        <div className="flex items-end justify-between mb-6">
          <h2 className="font-display font-bold text-2xl sm:text-3xl">Now Showing</h2>
          <Link to="/movies" className="text-sm text-marquee font-medium hover:text-marquee-soft">View all →</Link>
        </div>

        {error && <p className="text-velvet text-sm">{error}</p>}
        {!movies && !error && <LoadingSpinner label="Rolling the reel…" />}

        {movies && movies.length === 0 && (
          <p className="text-dim text-sm">No movies are showing right now — check back soon.</p>
        )}

        {movies && movies.length > 0 && (
          <div className="marquee-scroll flex gap-5 overflow-x-auto pb-3 -mx-1 px-1">
            {movies.map((movie) => <MovieCard key={movie.movieId} movie={movie} />)}
          </div>
        )}
      </section>

      {/* ── Why CineBook ── */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 pb-20">
        <div className="grid sm:grid-cols-3 gap-5">
          {[
            { title: 'Real seat maps', body: 'See the actual screen layout and pick exactly where you sit.', icon: 'seat' },
            { title: 'Instant confirmation', body: 'Your ticket and QR-ready booking reference land in your inbox right away.', icon: 'bolt' },
            { title: 'Easy cancellation', body: 'Plans change — cancel up to 2 hours before showtime, no questions asked.', icon: 'shield' },
          ].map((f) => (
            <div key={f.title} className="rounded-2xl border border-hairline bg-surface p-6">
              <div className="w-10 h-10 rounded-lg bg-marquee/10 text-marquee flex items-center justify-center mb-4">
                <Icon name={f.icon} />
              </div>
              <h3 className="font-display font-semibold text-lg mb-1.5">{f.title}</h3>
              <p className="text-sm text-dim leading-relaxed">{f.body}</p>
            </div>
          ))}
        </div>
      </section>
    </div>
  )
}

function Icon({ name }) {
  const paths = {
    seat: 'M6 10V6a2 2 0 012-2h8a2 2 0 012 2v4h1a1 1 0 011 1v3a2 2 0 01-2 2h-1v2h-2v-2H8v2H6v-2H5a2 2 0 01-2-2v-3a1 1 0 011-1h2z',
    bolt: 'M13 2L3 14h7l-1 8 10-12h-7l1-8z',
    shield: 'M12 2l8 3v6c0 5-3.4 9.4-8 11-4.6-1.6-8-6-8-11V5l8-3z',
  }
  return (
    <svg viewBox="0 0 24 24" className="w-5 h-5" fill="currentColor">
      <path d={paths[name]} />
    </svg>
  )
}
