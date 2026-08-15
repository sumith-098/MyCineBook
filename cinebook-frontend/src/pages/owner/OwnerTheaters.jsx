import { useEffect, useState } from 'react'
import { catalogApi } from '../../api/catalog'
import LoadingSpinner from '../../components/LoadingSpinner'

const EMPTY_FORM = { theaterName: '', location: '', totalSeats: '', phone: '', city: '', latitude: '', longitude: '' }

export default function OwnerTheaters() {
  const [theaters, setTheaters] = useState(null)
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState(EMPTY_FORM)
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const load = () => catalogApi.ownerTheaters().then(setTheaters).catch((e) => setError(e.message))
  useEffect(() => { load() }, [])

  const update = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }))

  const submit = async (e) => {
    e.preventDefault()
    setSubmitting(true)
    setError('')
    try {
      await catalogApi.addTheater({
        ...form,
        totalSeats: Number(form.totalSeats),
        latitude: form.latitude ? Number(form.latitude) : null,
        longitude: form.longitude ? Number(form.longitude) : null,
      })
      setForm(EMPTY_FORM)
      setShowForm(false)
      await load()
    } catch (e) {
      setError(e.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h2 className="font-display font-semibold text-xl">Your Theaters</h2>
        <button
          onClick={() => setShowForm((v) => !v)}
          className="text-sm font-semibold bg-marquee text-void px-4 py-2 rounded-full hover:bg-marquee-soft transition-colors"
        >
          {showForm ? 'Cancel' : '+ Add Theater'}
        </button>
      </div>

      {showForm && (
        <form onSubmit={submit} className="grid sm:grid-cols-2 gap-4 border border-hairline rounded-xl p-6 bg-surface mb-8">
          <Field label="Theater name" value={form.theaterName} onChange={update('theaterName')} required />
          <Field label="Location / address" value={form.location} onChange={update('location')} required />
          <Field label="Total seats" type="number" value={form.totalSeats} onChange={update('totalSeats')} required />
          <Field label="Phone" value={form.phone} onChange={update('phone')} />
          <Field label="City" value={form.city} onChange={update('city')} />
          <div />
          <Field label="Latitude (optional)" type="number" step="any" value={form.latitude} onChange={update('latitude')} />
          <Field label="Longitude (optional)" type="number" step="any" value={form.longitude} onChange={update('longitude')} />

          {error && <p className="text-velvet text-sm sm:col-span-2">{error}</p>}

          <button
            type="submit"
            disabled={submitting}
            className="sm:col-span-2 bg-marquee text-void font-bold py-3 rounded-full hover:bg-marquee-soft transition-colors disabled:opacity-40"
          >
            {submitting ? 'Adding…' : 'Add Theater'}
          </button>
        </form>
      )}

      {!theaters && <LoadingSpinner />}
      {theaters && theaters.length === 0 && !showForm && (
        <p className="text-dim text-sm">You haven't added a theater yet.</p>
      )}

      <div className="grid sm:grid-cols-2 gap-4">
        {theaters?.map((t) => (
          <div key={t.theaterId} className="border border-hairline rounded-xl p-5 bg-surface">
            <h3 className="font-display font-semibold text-lg">{t.theaterName}</h3>
            <p className="text-sm text-dim mt-1">{t.location}{t.city ? `, ${t.city}` : ''}</p>
            <p className="text-xs text-faint mt-2 font-mono">{t.totalSeats} seats · ID #{t.theaterId}</p>
          </div>
        ))}
      </div>
    </div>
  )
}

function Field({ label, type = 'text', value, onChange, required, step }) {
  return (
    <label className="flex flex-col gap-1.5">
      <span className="text-xs text-dim font-medium">{label}</span>
      <input
        type={type}
        step={step}
        value={value}
        onChange={onChange}
        required={required}
        className="bg-void border border-hairline rounded-lg px-3.5 py-2.5 text-sm outline-none
                   focus:border-marquee/60 transition-colors placeholder:text-faint"
      />
    </label>
  )
}
