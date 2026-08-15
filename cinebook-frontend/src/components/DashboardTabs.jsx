export default function DashboardTabs({ tabs, active, onChange }) {
  return (
    <div className="flex gap-1 border-b border-hairline mb-8 overflow-x-auto">
      {tabs.map((t) => (
        <button
          key={t.key}
          onClick={() => onChange(t.key)}
          className={`px-4 py-3 text-sm font-medium whitespace-nowrap border-b-2 -mb-px transition-colors
            ${active === t.key
              ? 'border-marquee text-marquee'
              : 'border-transparent text-dim hover:text-ivory'}`}
        >
          {t.label}
        </button>
      ))}
    </div>
  )
}
