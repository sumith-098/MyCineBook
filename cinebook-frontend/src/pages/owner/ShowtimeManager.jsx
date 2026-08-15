import { useEffect, useState } from 'react'
import { catalogApi } from '../../api/catalog'

const EMPTY = { showDate: '', showTime: '', screen: '' }

export default function ShowtimeManager({ movieId }) {
  const [showtimes, setShowtimes] = useState(null)
  const [form, setForm] = useState(EMPTY)
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const load = () => catalogApi.ownerShowtimes(movieId).then(setShowtimes).catch((e) => setError(e.message))
  useEffect(() => { load() }, [movieId])

  const update = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }))

  const submit = async (e) => {
    e.preventDefault()
    setSubmitting(true)
    setError('')
    try {
      await catalogApi.addShowtime(movieId, form)
      setForm(EMPTY)
      await load()
    } catch (e) {
      setError(e.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="mt-3 border-t border-hairline pt-4">
      <p className="text-xs text-dim mb-3">Showtimes</p>
      {showtimes?.length === 0 && <p className="text-xs text-faint mb-3">No showtimes added yet.</p>}
      {showtimes && showtimes.length > 0 && (
        <div className="flex flex-wrap gap-2 mb-4">
          {showtimes.map((s) => (
            <span key={s.showtimeId} className="text-xs font-mono bg-void border border-hairline px-2.5 py-1.5 rounded-lg">
              {s.showDate} · {s.showTime} · {s.screen}
            </span>
          ))}
        </div>
      )}

      <form onSubmit={submit} className="flex flex-wrap items-end gap-2.5">
        <label className="flex flex-col gap-1">
          <span className="text-[11px] text-faint">Date</span>
          <input type="date" value={form.showDate} onChange={update('showDate')} required
            className="bg-void border border-hairline rounded-lg px-2.5 py-2 text-xs outline-none focus:border-marquee/60" />
        </label>
        <label className="flex flex-col gap-1">
          <span className="text-[11px] text-faint">Time</span>
          <input type="time" value={form.showTime} onChange={update('showTime')} required
            className="bg-void border border-hairline rounded-lg px-2.5 py-2 text-xs outline-none focus:border-marquee/60" />
        </label>
        <label className="flex flex-col gap-1">
          <span className="text-[11px] text-faint">Screen</span>
          <input type="text" placeholder="Screen 1" value={form.screen} onChange={update('screen')} required
            className="bg-void border border-hairline rounded-lg px-2.5 py-2 text-xs outline-none focus:border-marquee/60 w-28" />
        </label>
        <button
          type="submit"
          disabled={submitting}
          className="text-xs font-semibold bg-marquee text-void px-4 py-2 rounded-lg hover:bg-marquee-soft transition-colors disabled:opacity-40"
        >
          {submitting ? 'Adding…' : '+ Add'}
        </button>
      </form>
      {error && <p className="text-velvet text-xs mt-2">{error}</p>}
    </div>
  )
}
