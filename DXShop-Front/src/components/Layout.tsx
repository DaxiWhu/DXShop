import { Outlet } from 'react-router-dom'
import Navbar from './Navbar'
import BottomNav from './BottomNav'

export default function Layout() {
  return (
    <div className="min-h-screen bg-gray-100 flex flex-col">
      <Navbar />
      <main className="flex-1 pb-16 max-w-6xl mx-auto w-full">
        <Outlet />
      </main>
      <BottomNav />
    </div>
  )
}
