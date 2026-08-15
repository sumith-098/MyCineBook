import { useState } from 'react'
import { bookingApi } from '../api/booking'

export default function ReviewForm({ bookingId, onDone }) {
  const [rating, setRating] = useState(5)
  const [text, setText] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  const submit = async () => {
    setSubmitting(true)
    setError('')
    try {
      await bookingApi.addReview(bookingId, { rating, reviewText: text })
      onDone()
    } catch (e) {
      setError(e.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="mt-3 border border-hairline rounded-xl p-5 bg-surface">
      <div className="flex items-center gap-1 mb-3">
        {[1, 2, 3, 4, 5].map((n) => (
          <button key={n} onClick={() => setRating(n)} className="text-xl leading-none" aria-label={`${n} stars`}>
            <span className={n <= rating ? 'text-marquee' : 'text-hairline'}>★</span>
          </button>
        ))}
      </div>
      <textarea
        value={text}
        onChange={(e) => setText(e.target.value)}
        placeholder="How was it? (optional)"
        rows={2}
        className="w-full bg-void border border-hairline rounded-lg px-3 py-2 text-sm placeholder:text-faint outline-none focus:border-marquee/60 mb-3"
      />
      {error && <p className="text-velvet text-xs mb-2">{error}</p>}
      <button
        onClick={submit}
        disabled={submitting}
        className="text-xs font-semibold bg-marquee text-void px-5 py-2 rounded-full hover:bg-marquee-soft transition-colors disabled:opacity-40"
      >
        {submitting ? 'Submitting…' : 'Submit Review'}
      </button>
    </div>
  )
}
