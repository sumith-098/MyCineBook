import { useEffect, useState } from 'react'
import { catalogApi } from '../../api/catalog'
import SeatGrid from '../../components/SeatGrid'
import LoadingSpinner from '../../components/LoadingSpinner'

const DEFAULT_CATEGORY = { category: '', price: '', sortOrder: 0, color: '#f2b705' }
const DEFAULT_ROW = { label: '', seats: 10, category: '' }
const PALETTE = ['#f2b705', '#a81f3d', '#2e9e6c', '#3b82f6', '#8b5cf6']

export default function ScreenLayoutEditor() {
  const [theaters, setTheaters] = useState(null)
  const [theaterId, setTheaterId] = useState('')
  const [existingLayouts, setExistingLayouts] = useState([]);
  const [screenName, setScreenName] = useState('')
  const [categories, setCategories] = useState([{ ...DEFAULT_CATEGORY, category: 'Gold', color: PALETTE[0] }])
  const [rows, setRows] = useState([{ ...DEFAULT_ROW, label: 'A', category: 'Gold' }])
  const [warning, setWarning] = useState(null) // { activeBookingCount }
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    catalogApi.ownerTheaters().then((ts) => { setTheaters(ts); if (ts.length) setTheaterId(String(ts[0].theaterId)) })
      .catch((e) => setError(e.message))
  }, [])

  useEffect(() => {
    if (!theaterId) return
    catalogApi.screenLayouts(theaterId).then(setExistingLayouts).catch(() => setExistingLayouts([]))
  }, [theaterId])

  const loadExisting = (layout) => {
    setScreenName(layout.screenName)
    setCategories(layout.categories.map((c) => ({ ...c, price: String(c.price) })))
    setRows(layout.layout?.rows || [])
  }

  const updateCategory = (i, key, val) => setCategories((cs) => cs.map((c, idx) => (idx === i ? { ...c, [key]: val } : c)))
  const addCategory = () => setCategories((cs) => [...cs, { ...DEFAULT_CATEGORY, color: PALETTE[cs.length % PALETTE.length] }])
  const removeCategory = (i) => setCategories((cs) => cs.filter((_, idx) => idx !== i))

  const updateRow = (i, key, val) => setRows((rs) => rs.map((r, idx) => (idx === i ? { ...r, [key]: val } : r)))
  const addRow = () => setRows((rs) => [...rs, { ...DEFAULT_ROW, label: nextLabel(rs), category: categories[0]?.category || '' }])
  const removeRow = (i) => setRows((rs) => rs.filter((_, idx) => idx !== i))

  const previewCatPrices = Object.fromEntries(
    categories.filter((c) => c.category).map((c) => [c.category, { price: Number(c.price) || 0, color: c.color }]),
  )
  const previewLayout = { rows: rows.filter((r) => r.label && r.category).map((r) => ({ ...r, seats: Number(r.seats) || 0 })) }

  const save = async (forceSave = false) => {
    setSubmitting(true)
    setError('')
    setSuccess('')
    try {
      const payload = {
        screenName,
        layout: previewLayout,
        categories: categories.map((c) => ({ ...c, price: Number(c.price), sortOrder: Number(c.sortOrder) || 0 })),
        forceSave,
      }
      const result = await catalogApi.saveScreenLayout(theaterId, payload)
      if (result.saved) {
        setWarning(null)
        setSuccess(`Screen "${screenName}" saved!`)
        catalogApi.screenLayouts(theaterId).then(setExistingLayouts)
      } else {
        setWarning({ activeBookingCount: result.activeBookingCount })
      }
    } catch (e) {
      setError(e.message)
    } finally {
      setSubmitting(false)
    }
  }

  if (!theaters) return <LoadingSpinner />
  if (theaters.length === 0) return <p className="text-dim text-sm">Add a theater first before setting up a screen layout.</p>

  return (
    <div>
      <div className="grid lg:grid-cols-[280px_1fr] gap-8">
        {/* Left: theater + existing screens */}
        <div>
          <label className="flex flex-col gap-1.5 mb-6">
            <span className="text-xs text-dim font-medium">Theater</span>
            <select
              value={theaterId}
              onChange={(e) => setTheaterId(e.target.value)}
              className="bg-void border border-hairline rounded-lg px-3.5 py-2.5 text-sm outline-none focus:border-marquee/60"
            >
              {theaters.map((t) => <option key={t.theaterId} value={t.theaterId}>{t.theaterName}</option>)}
            </select>
          </label>

          <p className="text-xs text-dim font-medium mb-2">Existing screens</p>
          {existingLayouts.length === 0 && <p className="text-xs text-faint">None yet — create one below.</p>}
          <div className="flex flex-col gap-1.5">
            {existingLayouts.map((l) => (
              <button
                key={l.screenName}
                onClick={() => loadExisting(l)}
                className="text-left text-sm px-3 py-2 rounded-lg border border-hairline hover:border-marquee/50 hover:text-marquee transition-colors"
              >
                {l.screenName} <span className="text-faint text-xs">({l.totalSeatCount} seats)</span>
              </button>
            ))}
          </div>
        </div>

        {/* Right: editor */}
        <div>
          <label className="flex flex-col gap-1.5 mb-6 max-w-xs">
            <span className="text-xs text-dim font-medium">Screen name</span>
            <input
              value={screenName}
              onChange={(e) => setScreenName(e.target.value)}
              placeholder="Screen 1"
              className="bg-void border border-hairline rounded-lg px-3.5 py-2.5 text-sm outline-none focus:border-marquee/60"
            />
          </label>

          {/* Categories */}
          <div className="mb-8">
            <div className="flex items-center justify-between mb-3">
              <h3 className="font-display font-semibold">Seat Categories</h3>
              <button onClick={addCategory} className="text-xs font-semibold text-marquee hover:text-marquee-soft">+ Add category</button>
            </div>
            <div className="flex flex-col gap-2.5">
              {categories.map((c, i) => (
                <div key={i} className="flex items-center gap-2.5 flex-wrap">
                  <input type="color" value={c.color} onChange={(e) => updateCategory(i, 'color', e.target.value)}
                    className="w-9 h-9 rounded-lg bg-void border border-hairline cursor-pointer" />
                  <input placeholder="Name (e.g. Gold)" value={c.category} onChange={(e) => updateCategory(i, 'category', e.target.value)}
                    className="bg-void border border-hairline rounded-lg px-3 py-2 text-sm outline-none focus:border-marquee/60 w-36" />
                  <input type="number" placeholder="Price" value={c.price} onChange={(e) => updateCategory(i, 'price', e.target.value)}
                    className="bg-void border border-hairline rounded-lg px-3 py-2 text-sm outline-none focus:border-marquee/60 w-24" />
                  <button onClick={() => removeCategory(i)} className="text-xs text-velvet hover:text-velvet-deep">Remove</button>
                </div>
              ))}
            </div>
          </div>

          {/* Rows */}
          <div className="mb-8">
            <div className="flex items-center justify-between mb-3">
              <h3 className="font-display font-semibold">Rows</h3>
              <button onClick={addRow} className="text-xs font-semibold text-marquee hover:text-marquee-soft">+ Add row</button>
            </div>
            <div className="flex flex-col gap-2.5">
              {rows.map((r, i) => (
                <div key={i} className="flex items-center gap-2.5 flex-wrap">
                  <input placeholder="Label (A)" value={r.label} onChange={(e) => updateRow(i, 'label', e.target.value.toUpperCase())}
                    className="bg-void border border-hairline rounded-lg px-3 py-2 text-sm outline-none focus:border-marquee/60 w-20" />
                  <input type="number" placeholder="Seats" value={r.seats} onChange={(e) => updateRow(i, 'seats', e.target.value)}
                    className="bg-void border border-hairline rounded-lg px-3 py-2 text-sm outline-none focus:border-marquee/60 w-24" />
                  <select value={r.category} onChange={(e) => updateRow(i, 'category', e.target.value)}
                    className="bg-void border border-hairline rounded-lg px-3 py-2 text-sm outline-none focus:border-marquee/60">
                    <option value="">Category…</option>
                    {categories.filter((c) => c.category).map((c) => <option key={c.category} value={c.category}>{c.category}</option>)}
                  </select>
                  <button onClick={() => removeRow(i)} className="text-xs text-velvet hover:text-velvet-deep">Remove</button>
                </div>
              ))}
            </div>
          </div>

          {/* Preview */}
          {previewLayout.rows.length > 0 && (
            <div className="mb-8 border border-hairline rounded-xl p-6 bg-surface">
              <p className="text-xs text-dim font-medium mb-4">Preview</p>
              <SeatGrid layout={previewLayout} catPrices={previewCatPrices} bookedSeats={[]} selected={[]} onToggle={() => {}} />
            </div>
          )}

          {warning && (
            <div className="border border-velvet/40 bg-velvet/10 rounded-xl p-4 mb-4 text-sm">
              <p className="text-velvet font-medium mb-2">
                ⚠️ {warning.activeBookingCount} active booking(s) exist for this screen.
              </p>
              <p className="text-dim text-xs mb-3">Existing bookings keep their already-charged price either way — this is just a heads-up.</p>
              <button
                onClick={() => save(true)}
                className="text-xs font-semibold bg-velvet text-ivory px-4 py-2 rounded-full hover:bg-velvet-deep transition-colors"
              >
                Save anyway
              </button>
            </div>
          )}

          {error && <p className="text-velvet text-sm mb-3">{error}</p>}
          {success && <p className="text-seat-open text-sm mb-3">{success}</p>}

          <button
            onClick={() => save(false)}
            disabled={submitting || !screenName || previewLayout.rows.length === 0}
            className="bg-marquee text-void font-bold px-8 py-3.5 rounded-full hover:bg-marquee-soft
                       transition-colors shadow-glow-gold disabled:opacity-30 disabled:cursor-not-allowed disabled:shadow-none"
          >
            {submitting ? 'Saving…' : 'Save Screen Layout'}
          </button>
        </div>
      </div>
    </div>
  )
}

function nextLabel(rows) {
  const last = rows[rows.length - 1]?.label || '@'
  return String.fromCharCode(last.charCodeAt(0) + 1)
}
