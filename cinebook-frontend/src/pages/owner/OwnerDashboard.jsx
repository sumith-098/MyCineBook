import { useState } from 'react'
import { useAuth } from '../../context/AuthContext'
import DashboardTabs from '../../components/DashboardTabs'
import OwnerTheaters from './OwnerTheaters'
import OwnerMovies from './OwnerMovies'
import ScreenLayoutEditor from './ScreenLayoutEditor'

const TABS = [
  { key: 'theaters', label: 'Theaters' },
  { key: 'movies', label: 'Movies' },
  { key: 'layout', label: 'Screen Layout' },
]

export default function OwnerDashboard() {
  const { user } = useAuth()
  const [tab, setTab] = useState('theaters')

  return (
    <div className="max-w-6xl mx-auto px-4 sm:px-6 py-10">
      <h1 className="font-display font-bold text-3xl mb-1">Owner Dashboard</h1>
      <p className="text-dim text-sm mb-8">Welcome back, {user?.name}.</p>

      <DashboardTabs tabs={TABS} active={tab} onChange={setTab} />

      {tab === 'theaters' && <OwnerTheaters />}
      {tab === 'movies' && <OwnerMovies />}
      {tab === 'layout' && <ScreenLayoutEditor />}
    </div>
  )
}
