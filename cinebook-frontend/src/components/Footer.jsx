export default function Footer() {
  return (
    <footer className="border-t border-hairline mt-24">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 py-10 flex flex-col sm:flex-row items-center justify-between gap-4">
        <div className="flex items-center gap-2">
          <svg viewBox="0 0 24 24" className="w-5 h-5 text-marquee" fill="currentColor">
            <path d="M20 4H4a2 2 0 0 0-2 2v3h2v2H2v2h2v2H2v3a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-3h-2v-2h2v-2h-2V9h2V6a2 2 0 0 0-2-2zm-4 13H8v-2h8v2zm0-4H8v-2h8v2zm0-4H8V7h8v2z" />
          </svg>
          <span className="font-display font-semibold text-sm">CINE<span className="text-marquee">BOOK</span></span>
        </div>
        <p className="text-xs text-faint">© 2026 CineBook. Grab your popcorn 🍿</p>
      </div>
    </footer>
  )
}
