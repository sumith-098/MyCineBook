import { useState } from 'react'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const navLink = ({ isActive }) =>
  `text-sm font-medium transition-colors ${isActive ? 'text-marquee' : 'text-dim hover:text-ivory'}`

export default function Navbar() {
  const { isAuthenticated, user, role, logout } = useAuth()
  const [menuOpen, setMenuOpen] = useState(false)
  const navigate = useNavigate()

  return (
    <header className="sticky top-0 z-40 bg-void/85 backdrop-blur-md border-b border-hairline">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between">
        <Link to="/" className="flex items-center gap-2 group">
          <svg viewBox="0 0 24 24" className="w-6 h-6 text-marquee" fill="currentColor">
            <path d="M20 4H4a2 2 0 0 0-2 2v3h2v2H2v2h2v2H2v3a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-3h-2v-2h2v-2h-2V9h2V6a2 2 0 0 0-2-2zm-4 13H8v-2h8v2zm0-4H8v-2h8v2zm0-4H8V7h8v2z" />
          </svg>
          <span className="font-display font-bold text-lg tracking-tight">
            CINE<span className="text-marquee">BOOK</span>
          </span>
        </Link>

        <nav className="hidden md:flex items-center gap-7">
          <NavLink to="/" end className={navLink}>Home</NavLink>
          <NavLink to="/movies" className={navLink}>Movies</NavLink>
          <NavLink to="/theaters" className={navLink}>Theaters</NavLink>
          {role === 'CUSTOMER' && <NavLink to="/my-bookings" className={navLink}>My Bookings</NavLink>}
          {role === 'OWNER' && <NavLink to="/owner" className={navLink}>Owner Dashboard</NavLink>}
          {role === 'ADMIN' && <NavLink to="/admin" className={navLink}>Admin</NavLink>}
        </nav>

        <div className="hidden md:flex items-center gap-3">
          {isAuthenticated ? (
            <div className="flex items-center gap-3">
              <span className="text-sm text-dim">Hi, {user?.name?.split(' ')[0]}</span>
              <button
                onClick={() => { logout(); navigate('/') }}
                className="text-sm font-medium text-dim hover:text-velvet transition-colors"
              >
                Log out
              </button>
            </div>
          ) : (
            <>
              <Link to="/login" className="text-sm font-medium text-dim hover:text-ivory transition-colors">Log in</Link>
              <Link
                to="/register"
                className="text-sm font-semibold bg-marquee text-void px-4 py-2 rounded-full hover:bg-marquee-soft transition-colors"
              >
                Sign up
              </Link>
            </>
          )}
        </div>

        <button
          className="md:hidden text-ivory"
          onClick={() => setMenuOpen((v) => !v)}
          aria-label="Toggle menu"
        >
          <svg viewBox="0 0 24 24" className="w-6 h-6" fill="none" stroke="currentColor" strokeWidth="2">
            {menuOpen ? <path d="M6 6l12 12M18 6L6 18" /> : <path d="M4 7h16M4 12h16M4 17h16" />}
          </svg>
        </button>
      </div>

      {menuOpen && (
        <div className="md:hidden border-t border-hairline bg-void px-4 py-4 flex flex-col gap-4">
          <NavLink to="/" end className={navLink} onClick={() => setMenuOpen(false)}>Home</NavLink>
          <NavLink to="/movies" className={navLink} onClick={() => setMenuOpen(false)}>Movies</NavLink>
          <NavLink to="/theaters" className={navLink} onClick={() => setMenuOpen(false)}>Theaters</NavLink>
          {role === 'CUSTOMER' && <NavLink to="/my-bookings" className={navLink} onClick={() => setMenuOpen(false)}>My Bookings</NavLink>}
          {role === 'OWNER' && <NavLink to="/owner" className={navLink} onClick={() => setMenuOpen(false)}>Owner Dashboard</NavLink>}
          {role === 'ADMIN' && <NavLink to="/admin" className={navLink} onClick={() => setMenuOpen(false)}>Admin</NavLink>}
          <div className="h-px bg-hairline my-1" />
          {isAuthenticated ? (
            <button onClick={() => { logout(); setMenuOpen(false); navigate('/') }} className="text-sm text-left text-velvet font-medium">
              Log out
            </button>
          ) : (
            <>
              <Link to="/login" className="text-sm text-dim" onClick={() => setMenuOpen(false)}>Log in</Link>
              <Link to="/register" className="text-sm font-semibold text-marquee" onClick={() => setMenuOpen(false)}>Sign up</Link>
            </>
          )}
        </div>
      )}
    </header>
  )
}
