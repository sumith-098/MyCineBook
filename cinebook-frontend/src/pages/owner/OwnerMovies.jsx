import { useEffect, useRef, useState } from 'react'
import { catalogApi } from '../../api/catalog'
import LoadingSpinner from '../../components/LoadingSpinner'
import RatingBadge from '../../components/RatingBadge'
import ShowtimeManager from './ShowtimeManager'

const EMPTY_FORM = { title: '', duration: '', genre: '', language: 'Tamil', description: '', theaterId: '' }

export default function OwnerMovies() {
  const [movies, setMovies] = useState(null)
  const [theaters, setTheaters] = useState([])
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState(EMPTY_FORM)
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [expandedId, setExpandedId] = useState(null)
  const [uploadingId, setUploadingId] = useState(null)
  const fileInputRefs = useRef({})

  const load = () => catalogApi.ownerMovies().then(setMovies).catch((e) => setError(e.message))
  useEffect(() => {
    load()
    catalogApi.ownerTheaters().then(setTheaters).catch(() => setTheaters([]))
  }, [])

  const update = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }))

  const submit = async (e) => {
    e.preventDefault()
    setSubmitting(true)
    setError('')
    try {
      await catalogApi.addMovie({ ...form, theaterId: Number(form.theaterId) })
      setForm(EMPTY_FORM)
      setShowForm(false)
      await load()
    } catch (e) {
      setError(e.message)
    } finally {
      setSubmitting(false)
    }
  }

  const toggle = async (movieId) => {
    try {
      await catalogApi.toggleMovie(movieId)
      await load()
    } catch (e) {
      setError(e.message)
    }
  }

  const handlePosterPick = async (movieId, file) => {
    if (!file) return
    setUploadingId(movieId)
    setError('')
    try {
      await catalogApi.uploadPoster(movieId, file)
      await load()
    } catch (e) {
      setError(e.message)
    } finally {
      setUploadingId(null)
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2 className="font-display font-semibold text-xl">Your Movies</h2>
        <button
          onClick={() => setShowForm((v) => !v)}
          disabled={theaters.length === 0}
          className="text-sm font-semibold bg-marquee text-void px-4 py-2 rounded-full hover:bg-marquee-soft transition-colors disabled:opacity-30"
          title={theaters.length === 0 ? 'Add a theater first' : undefined}
        >
          {showForm ? 'Cancel' : '+ Add Movie'}
        </button>
      </div>

      {showForm && (
        <form onSubmit={submit} className="grid sm:grid-cols-2 gap-4 border border-hairline rounded-xl p-6 bg-surface mb-8">
          <Field label="Title" value={form.title} onChange={update('title')} required />
          <label className="flex flex-col gap-1.5">
            <span className="text-xs text-dim font-medium">Theater</span>
            <select
              value={form.theaterId}
              onChange={update('theaterId')}
              required
              className="bg-void border border-hairline rounded-lg px-3.5 py-2.5 text-sm outline-none focus:border-marquee/60"
            >
              <option value="">Select a theater…</option>
              {theaters.map((t) => <option key={t.theaterId} value={t.theaterId}>{t.theaterName}</option>)}
            </select>
          </label>
          <Field label="Duration (e.g. 2h 30m)" value={form.duration} onChange={update('duration')} />
          <Field label="Genre" value={form.genre} onChange={update('genre')} />
          <Field label="Language" value={form.language} onChange={update('language')} />
          <div />
          <label className="flex flex-col gap-1.5 sm:col-span-2">
            <span className="text-xs text-dim font-medium">Description</span>
            <textarea
              value={form.description}
              onChange={update('description')}
              rows={3}
              className="bg-void border border-hairline rounded-lg px-3.5 py-2.5 text-sm outline-none focus:border-marquee/60"
            />
          </label>

          {error && <p className="text-velvet text-sm sm:col-span-2">{error}</p>}

          <button
            type="submit"
            disabled={submitting}
            className="sm:col-span-2 bg-marquee text-void font-bold py-3 rounded-full hover:bg-marquee-soft transition-colors disabled:opacity-40"
          >
            {submitting ? 'Adding…' : 'Add Movie'}
          </button>
          <p className="text-xs text-faint sm:col-span-2">You can upload a poster photo and set ticket prices right after adding.</p>
        </form>
      )}

      {!movies && <LoadingSpinner />}
      {movies && movies.length === 0 && !showForm && <p className="text-dim text-sm">No movies added yet.</p>}

      <div className="flex flex-col gap-4">
        {movies?.map((m) => (
          <div key={m.movieId} className="border border-hairline rounded-xl p-5 bg-surface">
            <div className="flex gap-4">
              <div className="relative w-16 h-24 rounded-lg overflow-hidden bg-surface-2 flex-shrink-0">
                {m.posterUrl
                  ? <img src={m.posterUrl} alt={m.title} className="w-full h-full object-cover" />
                  : <div className="w-full h-full flex items-center justify-center text-faint text-[9px] text-center px-1">No photo</div>}
                <RatingBadge rating={m.avgRating} count={m.reviewCount} size="sm" />
              </div>

              <div className="flex-1 min-w-0">
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <h3 className="font-display font-semibold text-lg truncate">{m.title}</h3>
                    <p className="text-xs text-dim mt-0.5">{[m.genre, m.duration].filter(Boolean).join(' · ')} — {m.theaterName}</p>
                  </div>
                  <span className={`text-[10px] font-mono font-bold uppercase px-2 py-1 rounded-full whitespace-nowrap
                    ${m.isActive ? 'text-seat-open bg-seat-open/10' : 'text-faint bg-void'}`}>
                    {m.isActive ? 'Active' : 'Hidden'}
                  </span>
                </div>

                <div className="flex flex-wrap items-center gap-3 mt-3">
                  <input
                    ref={(el) => (fileInputRefs.current[m.movieId] = el)}
                    type="file"
                    accept="image/jpeg,image/png,image/webp"
                    className="hidden"
                    onChange={(e) => handlePosterPick(m.movieId, e.target.files[0])}
                  />
                  <button
                    onClick={() => fileInputRefs.current[m.movieId]?.click()}
                    disabled={uploadingId === m.movieId}
                    className="text-xs font-semibold text-marquee hover:text-marquee-soft transition-colors disabled:opacity-40"
                  >
                    {uploadingId === m.movieId ? 'Uploading…' : m.posterUrl ? 'Replace poster' : 'Upload poster'}
                  </button>
                  <button onClick={() => toggle(m.movieId)} className="text-xs font-semibold text-dim hover:text-ivory transition-colors">
                    {m.isActive ? 'Hide' : 'Show'} listing
                  </button>
                  <button
                    onClick={() => setExpandedId(expandedId === m.movieId ? null : m.movieId)}
                    className="text-xs font-semibold text-dim hover:text-ivory transition-colors"
                  >
                    {expandedId === m.movieId ? 'Hide showtimes' : 'Manage showtimes'}
                  </button>
                </div>
              </div>
            </div>

            {expandedId === m.movieId && <ShowtimeManager movieId={m.movieId} />}
          </div>
        ))}
      </div>

      {error && <p className="text-velvet text-sm mt-4">{error}</p>}
    </div>
  )
}

function Field({ label, value, onChange, required }) {
  return (
    <label className="flex flex-col gap-1.5">
      <span className="text-xs text-dim font-medium">{label}</span>
      <input
        value={value}
        onChange={onChange}
        required={required}
        className="bg-void border border-hairline rounded-lg px-3.5 py-2.5 text-sm outline-none
                   focus:border-marquee/60 transition-colors placeholder:text-faint"
      />
    </label>
  )
}
