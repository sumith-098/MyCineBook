import { Link } from 'react-router-dom'
import RatingBadge from './RatingBadge'

export default function MovieCard({ movie }) {
  return (
    <Link
      to={`/movies/${movie.movieId}`}
      className="group flex-shrink-0 w-44 sm:w-52 focus-visible:outline-none"
    >
      <div className="relative aspect-[2/3] rounded-xl overflow-hidden bg-surface-2 border border-hairline
                       transition-transform duration-300 group-hover:-translate-y-1.5 group-hover:shadow-glow-gold">
        {movie.posterUrl ? (
          <img
            src={movie.posterUrl}
            alt={movie.title}
            loading="lazy"
            className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center text-faint font-display text-sm px-4 text-center">
            {movie.title}
          </div>
        )}
        <RatingBadge rating={movie.avgRating} count={movie.reviewCount} />
        <div className="absolute inset-x-0 bottom-0 h-20 bg-gradient-to-t from-void/90 to-transparent" />
      </div>
      <div className="mt-3">
        <h3 className="font-display font-semibold text-ivory leading-snug truncate group-hover:text-marquee transition-colors">
          {movie.title}
        </h3>
        <p className="text-xs text-dim mt-0.5 truncate">
          {[movie.genre, movie.duration].filter(Boolean).join(' · ')}
        </p>
      </div>
    </Link>
  )
}
