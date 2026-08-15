/**
 * Rating shown as a torn ticket corner sitting on the poster — ties the "how good is it" signal
 * to the same ticket motif as the rest of the app, instead of a generic floating star pill.
 */
export default function RatingBadge({ rating, count, size = 'md' }) {
  if (rating == null) return null
  const sizeClasses = size === 'sm' ? 'text-[11px] px-2 py-1' : 'text-xs px-2.5 py-1.5'

  return (
    <div
      className={`absolute top-0 left-0 ${sizeClasses} font-mono font-bold text-void bg-marquee
                  flex items-center gap-1 rounded-br-xl shadow-glow-gold`}
      style={{ clipPath: 'polygon(0 0, 100% 0, 100% 70%, 70% 100%, 0 100%)' }}
      title={count ? `${rating.toFixed(1)} from ${count} review${count === 1 ? '' : 's'}` : undefined}
    >
      <svg viewBox="0 0 20 20" fill="currentColor" className="w-3 h-3">
        <path d="M10 1.5l2.6 5.6 6.1.7-4.5 4.2 1.2 6-5.4-3-5.4 3 1.2-6L1.3 7.8l6.1-.7L10 1.5z" />
      </svg>
      {rating.toFixed(1)}
    </div>
  )
}
