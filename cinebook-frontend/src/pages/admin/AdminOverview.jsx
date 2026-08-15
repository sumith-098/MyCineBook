import { useEffect, useState } from 'react'
import { adminApi } from '../../api/admin'
import LoadingSpinner from '../../components/LoadingSpinner'
import StatCard from '../../components/StatCard'

export default function AdminOverview() {
  const [data, setData] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    adminApi.dashboard().then(setData).catch((e) => setError(e.message))
  }, [])

  if (error) return <p className="text-velvet text-sm">{error}</p>
  if (!data) return <LoadingSpinner />

  return (
    <div>
      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-4 mb-10">
        <StatCard label="Customers" value={data.customerCount} />
        <StatCard label="Theaters" value={data.theaterCount} />
        <StatCard label="Movies" value={data.movieCount} />
        <StatCard label="Bookings" value={data.bookingCount} />
        <StatCard label="Revenue" value={`₹${data.revenue}`} accent="success" />
      </div>

      <div className="grid sm:grid-cols-2 gap-4 mb-10">
        <StatCard label="Pending Owner Approvals" value={data.pendingOwnerCount} accent="velvet" />
        <StatCard label="Owners Awaiting Settlement" value={data.pendingSettlementOwnerCount} accent="velvet" />
      </div>

      <h2 className="font-display font-semibold text-xl mb-4">Recent Bookings</h2>
      <div className="border border-hairline rounded-xl overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-surface text-dim text-xs">
            <tr>
              <Th>Ref</Th><Th>Movie</Th><Th>Theater</Th><Th>Amount</Th><Th>Status</Th><Th>Payment</Th>
            </tr>
          </thead>
          <tbody>
            {data.recentBookings?.map((b) => (
              <tr key={b.bookingRef} className="border-t border-hairline">
                <Td mono>{b.bookingRef}</Td>
                <Td>{b.movieTitle}</Td>
                <Td>{b.theaterName}</Td>
                <Td mono>₹{b.amount}</Td>
                <Td>{b.status}</Td>
                <Td>{b.paymentMethod}</Td>
              </tr>
            ))}
          </tbody>
        </table>
        {(!data.recentBookings || data.recentBookings.length === 0) && (
          <p className="text-dim text-sm p-5">No bookings yet.</p>
        )}
      </div>
    </div>
  )
}

function Th({ children }) { return <th className="text-left font-medium px-4 py-3">{children}</th> }
function Td({ children, mono }) { return <td className={`px-4 py-3 ${mono ? 'font-mono text-xs' : ''}`}>{children}</td> }
