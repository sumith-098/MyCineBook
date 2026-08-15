import { useEffect, useState } from 'react'
import { catalogApi } from '../api/catalog'
import LoadingSpinner from '../components/LoadingSpinner'

export default function Theaters() {
  const [theaters, setTheaters] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    catalogApi.theaters().then(setTheaters).catch((e) => setError(e.message))
  }, [])

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 py-12">
      <h1 className="font-display font-bold text-3xl mb-2">Theaters</h1>
      <p className="text-dim text-sm mb-8">Every screen currently listed on CineBook.</p>

      {error && <p className="text-velvet text-sm">{error}</p>}
      {!theaters && !error && <LoadingSpinner />}

      <div className="grid sm:grid-cols-2 gap-4">
        {theaters?.map((t) => (
          <div key={t.theaterId} className="border border-hairline rounded-xl p-5 bg-surface">
            <div className="flex items-start justify-between gap-3">
              <div>
                <h3 className="font-display font-semibold text-lg">{t.theaterName}</h3>
                <p className="text-sm text-dim mt-1">{t.location}{t.city ? `, ${t.city}` : ''}</p>
              </div>
              {t.movieCount != null && (
                <span className="text-xs font-mono text-marquee bg-marquee/10 px-2.5 py-1 rounded-full whitespace-nowrap">
                  {t.movieCount} movies
                </span>
              )}
            </div>
            {t.phone && <p className="text-xs text-faint mt-3 font-mono">{t.phone}</p>}
          </div>
        ))}
      </div>
    </div>
  )
}
