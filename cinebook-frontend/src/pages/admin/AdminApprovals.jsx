import { useEffect, useState } from 'react'
import { adminApi } from '../../api/admin'
import LoadingSpinner from '../../components/LoadingSpinner'

export default function AdminApprovals() {
  const [owners, setOwners] = useState(null)
  const [error, setError] = useState('')
  const [busyId, setBusyId] = useState(null)

  const load = () => adminApi.pendingOwners().then(setOwners).catch((e) => setError(e.message))
  useEffect(() => { load() }, [])

  const approve = async (ownerId) => {
    setBusyId(ownerId)
    setError('')
    try {
      await adminApi.approveOwner(ownerId)
      await load()
    } catch (e) {
      setError(e.message)
    } finally {
      setBusyId(null)
    }
  }

  if (error) return <p className="text-velvet text-sm">{error}</p>
  if (!owners) return <LoadingSpinner />

  return (
    <div>
      <h2 className="font-display font-semibold text-xl mb-5">Pending Theater Owner Approvals</h2>
      {owners.length === 0 && <p className="text-dim text-sm">No pending approvals — you're all caught up.</p>}
      <div className="flex flex-col gap-3">
        {owners.map((o) => (
          <div key={o.ownerId} className="flex items-center justify-between border border-hairline rounded-xl p-5 bg-surface">
            <div>
              <p className="font-medium">{o.name}</p>
              <p className="text-sm text-dim">{o.email}</p>
            </div>
            <button
              onClick={() => approve(o.ownerId)}
              disabled={busyId === o.ownerId}
              className="text-sm font-semibold bg-marquee text-void px-5 py-2.5 rounded-full hover:bg-marquee-soft transition-colors disabled:opacity-40"
            >
              {busyId === o.ownerId ? 'Approving…' : 'Approve'}
            </button>
          </div>
        ))}
      </div>
    </div>
  )
}
