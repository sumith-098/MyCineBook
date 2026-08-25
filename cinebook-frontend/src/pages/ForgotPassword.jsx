import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { authApi } from '../api/auth'

// Backend only exposes a resend-otp route for the customer flow — owner has none.
const FLOWS = {
  CUSTOMER: {
    forgotPassword: authApi.forgotPassword,
    verifyResetOtp: authApi.verifyResetOtp,
    resetPassword: authApi.resetPassword,
    canResend: true,
  },
  OWNER: {
    forgotPassword: authApi.forgotPasswordOwner,
    verifyResetOtp: authApi.verifyResetOtpOwner,
    resetPassword: authApi.resetPasswordOwner,
    canResend: false,
  },
}

export default function ForgotPassword() {
  const location = useLocation()
  const role = FLOWS[location.state?.role] ? location.state.role : 'CUSTOMER'
  const flow = FLOWS[role]

  const [step, setStep] = useState('email') // 'email' | 'otp' | 'reset'
  const [email, setEmail] = useState('')
  const [otp, setOtp] = useState('')
  const [resetToken, setResetToken] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState('')
  const [info, setInfo] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const navigate = useNavigate()

  const submitEmail = async (e) => {
    e.preventDefault()
    setSubmitting(true)
    setError('')
    try {
      const data = await flow.forgotPassword(email)
      setInfo('If this email is registered, an OTP has been sent.')
      if (data?.devOtp) setOtp(data.devOtp) // dev/staging convenience, harmless in prod (field absent)
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
      const data = await flow.verifyResetOtp({ email, otp })
      setResetToken(data.resetToken)
      setInfo('')
      setStep('reset')
    } catch (e) {
      setError(e.message)
    } finally {
      setSubmitting(false)
    }
  }

  const resendOtp = async () => {
    setError('')
    setInfo('')
    try {
      await authApi.resendOtp('reset', email) // customer-only endpoint, gated by flow.canResend below
      setInfo('A new OTP has been sent.')
    } catch (e) {
      setError(e.message)
    }
  }

  const submitReset = async (e) => {
    e.preventDefault()
    if (password !== confirmPassword) {
      setError('Passwords do not match.')
      return
    }
    setSubmitting(true)
    setError('')
    try {
      await flow.resetPassword({ email, resetToken, password, confirmPassword })
      navigate('/login', { replace: true, state: { resetSuccess: true } })
    } catch (e) {
      setError(e.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="max-w-md mx-auto px-4 py-16">
      <h1 className="font-display font-bold text-3xl mb-1 text-center">Reset password</h1>
      <p className="text-dim text-sm text-center mb-8">
        {step === 'email' && "Enter your email and we'll send you an OTP."}
        {step === 'otp' && `Enter the OTP sent to ${email}.`}
        {step === 'reset' && 'Choose a new password.'}
      </p>

      {step === 'email' && (
        <form onSubmit={submitEmail} className="flex flex-col gap-4">
          <Field label="Email" type="email" value={email} onChange={setEmail} required />
          {error && <p className="text-velvet text-sm">{error}</p>}
          <button
            type="submit"
            disabled={submitting}
            className="mt-2 bg-marquee text-void font-bold py-3.5 rounded-full hover:bg-marquee-soft transition-colors disabled:opacity-40"
          >
            {submitting ? 'Sending…' : 'Send OTP'}
          </button>
        </form>
      )}

      {step === 'otp' && (
        <form onSubmit={submitOtp} className="flex flex-col gap-4">
          <Field label="OTP" type="text" value={otp} onChange={setOtp} required />
          {info && <p className="text-dim text-sm">{info}</p>}
          {error && <p className="text-velvet text-sm">{error}</p>}
          <button
            type="submit"
            disabled={submitting}
            className="mt-2 bg-marquee text-void font-bold py-3.5 rounded-full hover:bg-marquee-soft transition-colors disabled:opacity-40"
          >
            {submitting ? 'Verifying…' : 'Verify OTP'}
          </button>
          {flow.canResend && (
            <button
              type="button"
              onClick={resendOtp}
              className="text-xs text-dim hover:text-ivory text-center"
            >
              Resend OTP
            </button>
          )}
        </form>
      )}

      {step === 'reset' && (
        <form onSubmit={submitReset} className="flex flex-col gap-4">
          <Field label="New password" type="password" value={password} onChange={setPassword} required />
          <Field label="Confirm new password" type="password" value={confirmPassword} onChange={setConfirmPassword} required />
          {error && <p className="text-velvet text-sm">{error}</p>}
          <button
            type="submit"
            disabled={submitting}
            className="mt-2 bg-marquee text-void font-bold py-3.5 rounded-full hover:bg-marquee-soft transition-colors disabled:opacity-40"
          >
            {submitting ? 'Updating…' : 'Update password'}
          </button>
        </form>
      )}

      <p className="text-sm text-dim text-center mt-6">
        Remembered it? <Link to="/login" className="text-marquee font-medium hover:text-marquee-soft">Back to log in</Link>
      </p>
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