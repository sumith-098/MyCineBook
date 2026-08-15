import { useState } from 'react'
import { useAuth } from '../../context/AuthContext'
import DashboardTabs from '../../components/DashboardTabs'
import AdminOverview from './AdminOverview'
import AdminApprovals from './AdminApprovals'
import AdminSettlements from './AdminSettlements'

const TABS = [
  { key: 'overview', label: 'Overview' },
  { key: 'approvals', label: 'Owner Approvals' },
  { key: 'settlements', label: 'Settlements' },
]

export default function AdminDashboard() {
  const { user } = useAuth()
  const [tab, setTab] = useState('overview')

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 py-10">
      <h1 className="font-display font-bold text-3xl mb-1">Admin Dashboard</h1>
      <p className="text-dim text-sm mb-8">Welcome, {user?.name}.</p>

      <DashboardTabs tabs={TABS} active={tab} onChange={setTab} />

      {tab === 'overview' && <AdminOverview />}
      {tab === 'approvals' && <AdminApprovals />}
      {tab === 'settlements' && <AdminSettlements />}
    </div>
  )
}
