import { useState } from 'react'
import { Routes, Route } from 'react-router-dom'
import { ToastProvider } from './components/Toast'
import Layout from './components/Layout'
import Home from './pages/Home'
import Login from './pages/Login'
import ProductDetail from './pages/ProductDetail'
import Cart from './pages/Cart'
import Checkout from './pages/Checkout'
import Orders from './pages/Orders'
import OrderDetail from './pages/OrderDetail'
import Profile from './pages/Profile'
import ShopManage from './pages/ShopManage'
import Chat from './pages/Chat'

function NotFound() {
  return (
    <div className="flex flex-col items-center justify-center min-h-[60vh] text-gray-400">
      <div className="text-6xl mb-4">🔍</div>
      <p className="text-lg">页面不存在</p>
      <a href="/" className="btn-taobao mt-4">返回首页</a>
    </div>
  )
}

export default function App() {
  return (
    <ToastProvider>
      <Routes>
        {/* Shop manage has its own layout */}
        <Route path="/shop-manage" element={<ShopManage />} />

        {/* Login page */}
        <Route path="/login" element={<Login />} />

        {/* Chat page */}
        <Route path="/chat" element={<Chat />} />

        {/* Main layout with navbar and bottom nav */}
        <Route element={<Layout />}>
          <Route path="/" element={<Home />} />
          <Route path="/goods/:id" element={<ProductDetail />} />
          <Route path="/cart" element={<Cart />} />
          <Route path="/checkout" element={<Checkout />} />
          <Route path="/orders" element={<Orders />} />
          <Route path="/order/:id" element={<OrderDetail />} />
          <Route path="/profile" element={<Profile />} />
          <Route path="/search" element={<SearchPage />} />
          <Route path="*" element={<NotFound />} />
        </Route>
      </Routes>
    </ToastProvider>
  )
}

/** 简易搜索页 */
function SearchPage() {
  const [query] = useState(() => {
    return new URLSearchParams(window.location.search).get('q') || ''
  })

  return (
    <div className="min-h-screen bg-gray-100">
      <div className="bg-white px-4 py-3">
        <h1 className="text-base font-bold">搜索: {query}</h1>
      </div>
      <div className="flex flex-col items-center justify-center py-20 text-gray-400">
        <div className="text-5xl mb-4">🔍</div>
        <p>搜索功能开发中</p>
        <p className="text-xs mt-1">请通过首页浏览商品</p>
      </div>
    </div>
  )
}

