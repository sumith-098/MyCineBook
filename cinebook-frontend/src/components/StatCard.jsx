export default function StatCard({ label, value, accent = 'marquee' }) {
  const colorClass = accent === 'marquee' ? 'text-marquee' : accent === 'velvet' ? 'text-velvet' : 'text-seat-open'
  return (
    <div className="rounded-2xl border border-hairline bg-surface p-5">
      <p className="text-xs text-dim mb-1.5">{label}</p>
      <p className={`font-display font-bold text-2xl ${colorClass}`}>{value}</p>
    </div>
  )
}
