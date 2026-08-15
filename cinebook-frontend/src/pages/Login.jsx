import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

const TABS = [
  { key: 'CUSTOMER', label: 'Moviegoer' },
  { key: 'OWNER', label: 'Theater Owner' },
  { key: 'ADMIN', label: 'Admin' },
]

export default function Login() {
  const [tab, setTab] = useState('CUSTOMER')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const { loginCustomer, loginOwner, loginAdmin } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const from = location.state?.from?.pathname

  const submit = async (e) => {
    e.preventDefault()
    setSubmitting(true)
    setError('')
    try {
      if (tab === 'CUSTOMER') await loginCustomer(email, password)
      else if (tab === 'OWNER') await loginOwner(email, password)
      else await loginAdmin(email, password)
  

      navigate(from || (tab === 'OWNER' ? '/owner' : tab === 'ADMIN' ? '/admin' : '/'), { replace: true })
    } catch (e) {
      setError(e.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="max-w-md mx-auto px-4 py-16">
      <h1 className="font-display font-bold text-3xl mb-1 text-center">Welcome back</h1>
      <p className="text-dim text-sm text-center mb-8">Log in to book your next show.</p>

      <div className="flex gap-1 bg-surface border border-hairline rounded-full p-1 mb-8">
        {TABS.map((t) => (
          <button
            key={t.key}
            onClick={() => { setTab(t.key); setError('') }}
            className={`flex-1 text-xs font-semibold py-2 rounded-full transition-colors
              ${tab === t.key ? 'bg-marquee text-void' : 'text-dim hover:text-ivory'}`}
          >
            {t.label}
          </button>
        ))}
      </div>

      <form onSubmit={submit} className="flex flex-col gap-4">
        <Field label="Email" type="email" value={email} onChange={setEmail} required />
        <Field label="Password" type="password" value={password} onChange={setPassword} required />

        {error && <p className="text-velvet text-sm">{error}</p>}

        <button
          type="submit"
          disabled={submitting}
          className="mt-2 bg-marquee text-void font-bold py-3.5 rounded-full hover:bg-marquee-soft transition-colors disabled:opacity-40"
        >
          {submitting ? 'Logging in…' : 'Log In'}
        </button>
      </form>

      {tab === 'CUSTOMER' && (
        <p className="text-sm text-dim text-center mt-6">
          New here? <Link to="/register" className="text-marquee font-medium hover:text-marquee-soft">Create an account</Link>
        </p>
      )}
      {tab === 'OWNER' && (
        <p className="text-sm text-dim text-center mt-6">
          Own a theater? <Link to="/register?role=owner" className="text-marquee font-medium hover:text-marquee-soft">Register your theater</Link>
        </p>
      )}
    </div>
  )
}

function Field({ label, type = 'text', value, onChange, required }) {
  return (
    <label className="flex flex-col gap-1.5">
      <span className="text-xs text-dim font-medium">{label}</span>
      <input
        type={type}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        required={required}
        className="bg-surface border border-hairline rounded-lg px-4 py-3 text-sm outline-none
                   focus:border-marquee/60 transition-colors placeholder:text-faint"
      />
    </label>
  )
}
