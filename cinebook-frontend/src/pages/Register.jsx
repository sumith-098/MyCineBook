import { useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { authApi } from '../api/auth'
import { useAuth } from '../context/AuthContext'

export default function Register() {
  const [searchParams] = useSearchParams()
  const isOwner = searchParams.get('role') === 'owner'
  const navigate = useNavigate()
  const { verifyCustomerOtpAndLogin } = useAuth()

  const [step, setStep] = useState('form') // 'form' | 'otp' | 'pending-approval'
  const [form, setForm] = useState({ name: '', email: '', phone: '', password: '', confirmPassword: '' })
  const [otp, setOtp] = useState('')
  const [devOtp, setDevOtp] = useState(null)
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const update = (key) => (val) => setForm((f) => ({ ...f, [key]: val }))

  const submitForm = async (e) => {
    e.preventDefault()
    setSubmitting(true)
    setError('')
    try {
      const result = isOwner ? await authApi.registerOwner(form) : await authApi.registerCustomer(form)
      setDevOtp(result?.devOtp || null)
      setStep('otp')
    } catch (e) {
      setError(e.message)
    } finally {
      setSubmitting(false)
    }
  }

  const submitOtp = async (e) => {
    e.preventDefault()
    setSubmitting(true)
    setError('')
    try {
      if (isOwner) {
        await authApi.verifyOwnerOtp({ email: form.email, otp })
        setStep('pending-approval')
      } else {
        await verifyCustomerOtpAndLogin(form.email, otp)
        navigate('/')
      }
    } catch (e) {
      setError(e.message)
    } finally {
      setSubmitting(false)
    }
  }

  if (step === 'pending-approval') {
    return (
      <div className="max-w-md mx-auto px-4 py-24 text-center">
        <div className="w-14 h-14 rounded-full bg-marquee/10 text-marquee flex items-center justify-center mx-auto mb-6">
          <svg viewBox="0 0 24 24" className="w-7 h-7" fill="none" stroke="currentColor" strokeWidth="2"><path d="M12 8v4l3 3M12 22a10 10 0 100-20 10 10 0 000 20z" /></svg>
        </div>
        <h1 className="font-display font-bold text-2xl mb-2">Account created!</h1>
        <p className="text-dim text-sm mb-8">Your theater owner account is under review — an admin will approve it shortly. You'll be notified by email.</p>
        <Link to="/login" className="text-sm font-semibold bg-marquee text-void px-6 py-3 rounded-full hover:bg-marquee-soft transition-colors">
          Go to Login
        </Link>
      </div>
    )
  }

  return (
    <div className="max-w-md mx-auto px-4 py-16">
      <h1 className="font-display font-bold text-3xl mb-1 text-center">
        {isOwner ? 'Register your theater' : 'Create your account'}
      </h1>
      <p className="text-dim text-sm text-center mb-8">
        {isOwner ? 'Get approved, then start listing your theaters and movies.' : 'Join CineBook and start booking in minutes.'}
      </p>

      {step === 'form' && (
        <form onSubmit={submitForm} className="flex flex-col gap-4">
          <Field label="Full name" value={form.name} onChange={update('name')} required />
          <Field label="Email" type="email" value={form.email} onChange={update('email')} required />
          <Field label="Phone" type="tel" value={form.phone} onChange={update('phone')} />
          <Field label="Password" type="password" value={form.password} onChange={update('password')} required />
          <Field label="Confirm password" type="password" value={form.confirmPassword} onChange={update('confirmPassword')} required />

          {error && <p className="text-velvet text-sm">{error}</p>}

          <button
            type="submit"
            disabled={submitting}
            className="mt-2 bg-marquee text-void font-bold py-3.5 rounded-full hover:bg-marquee-soft transition-colors disabled:opacity-40"
          >
            {submitting ? 'Sending OTP…' : 'Continue'}
          </button>
        </form>
      )}

      {step === 'otp' && (
        <form onSubmit={submitOtp} className="flex flex-col gap-4">
          <p className="text-sm text-dim text-center -mt-2 mb-2">
            We sent a 6-digit code to <span className="text-ivory">{form.email}</span>
          </p>
          {devOtp && (
            <p className="text-xs text-marquee text-center font-mono bg-marquee/10 rounded-lg py-2">
              DEV MODE — your OTP is {devOtp}
            </p>
          )}
          <Field label="Verification code" value={otp} onChange={setOtp} required />

          {error && <p className="text-velvet text-sm">{error}</p>}

          <button
            type="submit"
            disabled={submitting}
            className="mt-2 bg-marquee text-void font-bold py-3.5 rounded-full hover:bg-marquee-soft transition-colors disabled:opacity-40"
          >
            {submitting ? 'Verifying…' : 'Verify & Continue'}
          </button>
        </form>
      )}

      {!isOwner && step === 'form' && (
        <p className="text-sm text-dim text-center mt-6">
          Already have an account? <Link to="/login" className="text-marquee font-medium hover:text-marquee-soft">Log in</Link>
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
