import { useEffect, useState } from 'react'
import { adminApi } from '../../api/admin'
import LoadingSpinner from '../../components/LoadingSpinner'

export default function AdminSettlements() {
  const [data, setData] = useState(null)
  const [error, setError] = useState('')
  const [commissionInput, setCommissionInput] = useState('')
  const [savingCommission, setSavingCommission] = useState(false)
  const [payingOwnerId, setPayingOwnerId] = useState(null)

  const load = () => adminApi.settlements().then((d) => { setData(d); setCommissionInput(String(d.commissionPct)) }).catch((e) => setError(e.message))
  useEffect(() => { load() }, [])

  const saveCommission = async () => {
    setSavingCommission(true)
    setError('')
    try {
      await adminApi.updateCommissionConfig({ commissionPct: Number(commissionInput) })
      await load()
    } catch (e) {
      setError(e.message)
    } finally {
      setSavingCommission(false)
    }
  }

  if (error && !data) return <p className="text-velvet text-sm">{error}</p>
  if (!data) return <LoadingSpinner />

  return (
    <div>
      <div className="border border-hairline rounded-xl p-5 bg-surface mb-8 flex items-center gap-4 flex-wrap">
        <div>
          <p className="text-sm font-medium">Platform Commission</p>
          <p className="text-xs text-dim">Applied to every owner's razorpay-paid earnings.</p>
        </div>
        <div className="flex items-center gap-2 ml-auto">
          <input
            type="number"
            step="0.1"
            value={commissionInput}
            onChange={(e) => setCommissionInput(e.target.value)}
            className="w-20 bg-void border border-hairline rounded-lg px-3 py-2 text-sm text-right outline-none focus:border-marquee/60"
          />
          <span className="text-sm text-dim">%</span>
          <button
            onClick={saveCommission}
            disabled={savingCommission}
            className="text-xs font-semibold bg-marquee text-void px-4 py-2 rounded-full hover:bg-marquee-soft transition-colors disabled:opacity-40"
          >
            {savingCommission ? 'Saving…' : 'Save'}
          </button>
        </div>
      </div>

      <h2 className="font-display font-semibold text-xl mb-4">Owner Settlements</h2>
      <div className="flex flex-col gap-3">
        {data.owners?.map((o) => (
          <OwnerRow key={o.ownerId} owner={o} onPaid={load} paying={payingOwnerId === o.ownerId} setPaying={setPayingOwnerId} />
        ))}
        {(!data.owners || data.owners.length === 0) && <p className="text-dim text-sm">No active theater owners yet.</p>}
      </div>

      {error && <p className="text-velvet text-sm mt-4">{error}</p>}
    </div>
  )
}

function OwnerRow({ owner, onPaid, paying, setPaying }) {
  const [amount, setAmount] = useState(String(owner.pendingAmount))
  const [notes, setNotes] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  const markPaid = async () => {
    setSubmitting(true)
    setError('')
    try {
      await adminApi.markSettlementPaid(owner.ownerId, { amount: Number(amount), notes })
      setPaying(null)
      onPaid()
    } catch (e) {
      setError(e.message)
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="border border-hairline rounded-xl p-5 bg-surface">
      <div className="flex items-center justify-between flex-wrap gap-3">
        <div>
          <p className="font-medium">{owner.ownerName}</p>
          <p className="text-xs text-dim">{owner.ownerEmail}</p>
        </div>
        <div className="flex items-center gap-6 text-sm font-mono">
          <Stat label="Earned" value={`₹${owner.totalEarned}`} />
          <Stat label="Commission" value={`₹${owner.adminCut}`} />
          <Stat label="Paid" value={`₹${owner.alreadyPaid}`} />
          <Stat label="Pending" value={`₹${owner.pendingAmount}`} accent />
        </div>
        <button
          onClick={() => setPaying(paying ? null : owner.ownerId)}
          disabled={Number(owner.pendingAmount) <= 0}
          className="text-xs font-semibold bg-marquee text-void px-4 py-2 rounded-full hover:bg-marquee-soft transition-colors disabled:opacity-30"
        >
          {paying ? 'Cancel' : 'Mark Paid'}
        </button>
      </div>

      {paying && (
        <div className="flex items-end gap-2.5 flex-wrap mt-4 pt-4 border-t border-hairline">
          <label className="flex flex-col gap-1">
            <span className="text-[11px] text-faint">Amount (₹)</span>
            <input type="number" value={amount} onChange={(e) => setAmount(e.target.value)}
              className="bg-void border border-hairline rounded-lg px-3 py-2 text-sm w-28 outline-none focus:border-marquee/60" />
          </label>
          <label className="flex flex-col gap-1 flex-1 min-w-[160px]">
            <span className="text-[11px] text-faint">Notes (optional)</span>
            <input value={notes} onChange={(e) => setNotes(e.target.value)}
              className="bg-void border border-hairline rounded-lg px-3 py-2 text-sm outline-none focus:border-marquee/60" />
          </label>
          <button
            onClick={markPaid}
            disabled={submitting}
            className="text-xs font-semibold bg-seat-open text-void px-4 py-2 rounded-lg hover:opacity-90 transition-opacity disabled:opacity-40"
          >
            {submitting ? 'Recording…' : 'Confirm Payment'}
          </button>
          {error && <p className="text-velvet text-xs w-full">{error}</p>}
        </div>
      )}
    </div>
  )
}

function Stat({ label, value, accent }) {
  return (
    <div className="text-right">
      <p className="text-[10px] text-faint uppercase tracking-wide">{label}</p>
      <p className={accent ? 'text-marquee font-bold' : 'text-ivory'}>{value}</p>
    </div>
  )
}
