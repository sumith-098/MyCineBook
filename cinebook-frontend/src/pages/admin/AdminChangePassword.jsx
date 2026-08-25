import { useState } from 'react'
import { adminApi } from '../../api/admin'

export default function AdminChangePassword() {
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmNewPassword, setConfirmNewPassword] = useState('')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const submit = async (e) => {
    e.preventDefault()
    setError('')
    setSuccess('')
    if (newPassword !== confirmNewPassword) {
      setError('New passwords do not match.')
      return
    }
    setSubmitting(true)
    try {
      await adminApi.changePassword({ currentPassword, newPassword, confirmNewPassword })
      setSuccess('Password updated.')
      setCurrentPassword('')
      setNewPassword('')
      setConfirmNewPassword('')
    } catch (e) {
      setError(e.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="max-w-md">
      <h2 className="font-display font-bold text-xl mb-4">Change password</h2>
      <form onSubmit={submit} className="flex flex-col gap-4">
        <Field label="Current password" value={currentPassword} onChange={setCurrentPassword} />
        <Field label="New password" value={newPassword} onChange={setNewPassword} />
        <Field label="Confirm new password" value={confirmNewPassword} onChange={setConfirmNewPassword} />

        {error && <p className="text-velvet text-sm">{error}</p>}
        {success && <p className="text-sm text-green-500">{success}</p>}

        <button
          type="submit"
          disabled={submitting}
          className="mt-2 bg-marquee text-void font-bold py-3 rounded-full hover:bg-marquee-soft transition-colors disabled:opacity-40 self-start px-8"
        >
          {submitting ? 'Updating…' : 'Update password'}
        </button>
      </form>
    </div>
  )
}

function Field({ label, value, onChange }) {
  return (
    <label className="flex flex-col gap-1.5">
      <span className="text-xs text-dim font-medium">{label}</span>
      <input
        type="password"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        required
        className="bg-surface border border-hairline rounded-lg px-4 py-3 text-sm outline-none
                   focus:border-marquee/60 transition-colors placeholder:text-faint"
      />
    </label>
  )
}