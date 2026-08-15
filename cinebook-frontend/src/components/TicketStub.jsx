/**
 * The signature element: a physically-accurate torn cinema ticket. Two punched notch circles
 * (colored to match the page background — pass `notchColor` if the stub sits on a surface other
 * than the default void page background) sit on a dashed perforation between the main body and
 * the narrow "ADMIT ONE" stub half.
 *
 * Used for: booking summary (seat picker), booking confirmation, and My Bookings list items.
 */
export default function TicketStub({ children, stubLabel = 'ADMIT ONE · CINEBOOK', notchColor = '#0b0a10', className = '' }) {
  return (
    <div className={`ticket-stub ${className}`}>
      <div className="p-6">{children}</div>
      <div className="ticket-stub__seam flex items-center justify-center px-3 py-6 bg-void/40">
        <span className="ticket-stub__notch" style={{ top: -9, background: notchColor }} />
        <span className="ticket-stub__notch" style={{ bottom: -9, background: notchColor }} />
        <span className="ticket-stub__admit text-[10px] text-dim font-mono">{stubLabel}</span>
      </div>
    </div>
  )
}
