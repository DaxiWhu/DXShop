import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '../store/auth'

export default function Navbar() {
  const navigate = useNavigate()
  const { isLoggedIn, isShopMode, logoutShop } = useAuthStore()
  const [searchText, setSearchText] = useState('')

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    if (searchText.trim()) {
      navigate(`/search?q=${encodeURIComponent(searchText.trim())}`)
    }
  }

  return (
    <nav className="bg-gradient-to-r from-taobao-500 to-orange-400 sticky top-0 z-50 shadow-sm">
      <div className="max-w-6xl mx-auto px-3 h-12 flex items-center gap-2">
        {/* Logo */}
        <button onClick={() => navigate('/')} className="shrink-0 text-white font-bold text-lg tracking-wide mr-1">
          DXShop
        </button>

        {/* Search Bar */}
        <form onSubmit={handleSearch} className="flex-1 relative">
          <input
            type="text"
            value={searchText}
            onChange={e => setSearchText(e.target.value)}
            placeholder="搜索商品"
            className="w-full h-8 rounded-2xl bg-white/90 px-4 pr-10 text-sm placeholder-gray-400
                       focus:outline-none focus:bg-white focus:shadow-lg transition-all"
          />
          <button type="submit" className="absolute right-0 top-0 h-8 w-10 flex items-center justify-center text-gray-400">
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </button>
        </form>

        {/* Actions */}
        <div className="flex items-center gap-1 shrink-0">
          {isShopMode ? (
            <button
              onClick={() => { logoutShop(); navigate('/') }}
              className="text-white/90 text-xs px-2 py-1 rounded-lg hover:bg-white/20 transition-colors"
            >
              退出商家
            </button>
          ) : (
            <button
              onClick={() => navigate(isLoggedIn ? '/profile' : '/login')}
              className="text-white/90 text-xs px-2 py-1 rounded-lg hover:bg-white/20 transition-colors"
            >
              {isLoggedIn ? '我的' : '登录'}
            </button>
          )}
        </div>
      </div>
    </nav>
  )
}
