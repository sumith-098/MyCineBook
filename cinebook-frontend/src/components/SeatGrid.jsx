/**
 * Renders the seat layout catalog-service returns (rows of {label, seats, category}) as an
 * interactive grid. Seat id = `${rowLabel}${seatNumber}`, matching booking-service's seatNo
 * convention exactly, so what's selected here can be sent straight to /api/bookings/quote.
 */
export default function SeatGrid({ layout, catPrices, bookedSeats, selected, onToggle }) {
  if (!layout?.rows?.length) {
    return <p className="text-dim text-sm">Seat layout not available for this screen yet.</p>
  }

  const bookedSet = new Set(bookedSeats || [])
  const selectedSet = new Set(selected || [])

  return (
    <div className="flex flex-col items-center gap-8">
      {/* The screen — a glowing arc, not a flat bar, gives seats somewhere to "face" */}
      <div className="w-full max-w-2xl flex flex-col items-center gap-1">
        <svg viewBox="0 0 400 40" className="w-full h-8">
          <path d="M 10 35 Q 200 0 390 35" fill="none" stroke="#f2b705" strokeWidth="2.5" strokeLinecap="round" opacity="0.8" />
        </svg>
        <span className="text-[11px] tracking-[0.3em] text-faint font-mono">SCREEN</span>
      </div>

      <div className="marquee-scroll overflow-x-auto w-full pb-2">
        <div className="flex flex-col gap-2 items-center min-w-max px-2">
          {layout.rows.map((row) => {
            const catColor = catPrices?.[row.category]?.color || '#4a5568'
            return (
              <div key={row.label} className="flex items-center gap-3">
                <span className="w-5 text-xs text-faint font-mono text-right">{row.label}</span>
                <div className="flex gap-1.5">
                  {Array.from({ length: row.seats }, (_, i) => {
                    const seatId = `${row.label}${i + 1}`
                    const isBooked = bookedSet.has(seatId)
                    const isSelected = selectedSet.has(seatId)
                    return (
                      <button
                        key={seatId}
                        type="button"
                        disabled={isBooked}
                        onClick={() => onToggle(seatId)}
                        title={`${seatId} · ${row.category}`}
                        className={`w-7 h-7 rounded-md text-[10px] font-mono font-semibold flex items-center justify-center
                          transition-all duration-150
                          ${isBooked
                            ? 'bg-seat-booked text-faint cursor-not-allowed opacity-50'
                            : isSelected
                              ? 'bg-emerald-500 text-white scale-110 shadow-[0_0_10px_#10b981]'
                              : 'text-void hover:scale-110 hover:brightness-110 cursor-pointer'}
                        `}
                        style={!isBooked && !isSelected ? { backgroundColor: catColor } : undefined}
                      >
                        {i + 1}
                      </button>
                    )
                  })}
                </div>
                <span className="w-5 text-xs text-faint font-mono">{row.label}</span>
              </div>
            )
          })}
        </div>
      </div>

      {/* Legend */}
      <div className="flex flex-wrap items-center justify-center gap-x-6 gap-y-2 text-xs text-dim">
        {Object.entries(catPrices || {}).map(([name, info]) => (
          <span key={name} className="flex items-center gap-1.5">
            <span className="w-3 h-3 rounded" style={{ backgroundColor: info.color }} />
            {name} · ₹{info.price}
          </span>
        ))}
        <span className="flex items-center gap-1.5">
          <span className="w-3 h-3 rounded bg-emerald-500" /> Selected
        </span>
        <span className="flex items-center gap-1.5">
          <span className="w-3 h-3 rounded bg-seat-booked opacity-50" /> Booked
        </span>
      </div>
    </div>
  )
}
