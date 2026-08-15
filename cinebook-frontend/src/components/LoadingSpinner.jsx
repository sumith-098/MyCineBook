/** A spinning film reel — reinforces the cinema subject even in a loading state, rather than a generic spinner. */
export default function LoadingSpinner({ label = 'Loading…' }) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 py-16 text-dim">
      <svg viewBox="0 0 48 48" className="w-10 h-10 animate-spin" style={{ animationDuration: '1.4s' }}>
        <circle cx="24" cy="24" r="20" fill="none" stroke="#2e2840" strokeWidth="3" />
        <circle cx="24" cy="24" r="20" fill="none" stroke="#f2b705" strokeWidth="3" strokeDasharray="30 95" strokeLinecap="round" />
        <circle cx="24" cy="14" r="3.2" fill="#2e2840" />
        <circle cx="33" cy="30" r="3.2" fill="#2e2840" />
        <circle cx="15" cy="30" r="3.2" fill="#2e2840" />
        <circle cx="24" cy="24" r="4" fill="#2e2840" />
      </svg>
      <span className="text-xs tracking-wide">{label}</span>
    </div>
  )
}
