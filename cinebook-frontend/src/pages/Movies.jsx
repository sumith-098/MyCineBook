import { useEffect, useState } from 'react'
import { catalogApi } from '../api/catalog'
import MovieCard from '../components/MovieCard'
import LoadingSpinner from '../components/LoadingSpinner'

export default function Movies() {
  const [query, setQuery] = useState('')
  const [movies, setMovies] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    const handle = setTimeout(() => {
      setError('')
      catalogApi.searchMovies(query).then(setMovies).catch((e) => setError(e.message))
    }, 300)
    return () => clearTimeout(handle)
  }, [query])

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 py-12">
      <h1 className="font-display font-bold text-3xl sm:text-4xl mb-2">Movies</h1>
      <p className="text-dim text-sm mb-8">Everything currently playing, across every theater.</p>

      <div className="relative max-w-md mb-10">
        <svg viewBox="0 0 24 24" className="w-4 h-4 absolute left-4 top-1/2 -translate-y-1/2 text-faint" fill="none" stroke="currentColor" strokeWidth="2">
          <circle cx="11" cy="11" r="7" />
          <path d="M21 21l-4.3-4.3" />
        </svg>
        <input
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Search by title or genre…"
          className="w-full bg-surface border border-hairline rounded-full pl-11 pr-4 py-3 text-sm
                     placeholder:text-faint focus:border-marquee/60 outline-none transition-colors"
        />
      </div>

      {error && <p className="text-velvet text-sm">{error}</p>}
      {!movies && !error && <LoadingSpinner />}

      {movies && movies.length === 0 && (
        <p className="text-dim text-sm">No movies match "{query}".</p>
      )}

      {movies && movies.length > 0 && (
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-x-5 gap-y-9">
          {movies.map((movie) => <MovieCard key={movie.movieId} movie={movie} />)}
        </div>
      )}
    </div>
  )
}
