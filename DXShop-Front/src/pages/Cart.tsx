import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { getCart, checkCartItems, deleteCartItems, addToCart } from '../api'
import { useAuthStore } from '../store/auth'
import { useToast } from '../components/Toast'
import type { UserCart } from '../api/types'

export default function Cart() {
  const navigate = useNavigate()
  const { isLoggedIn } = useAuthStore()
  const { toast } = useToast()

  const [items, setItems] = useState<UserCart[]>([])
  const [loading, setLoading] = useState(true)
  const [editing, setEditing] = useState(false)

  useEffect(() => {
    if (!isLoggedIn) {
      navigate('/login')
      return
    }
    loadCart()
  }, [isLoggedIn])

  const loadCart = async () => {
    try {
      setLoading(true)
      const data = await getCart()
      setItems(data || [])
    } catch {
      toast('加载购物车失败', 'error')
      setItems([])
    } finally {
      setLoading(false)
    }
  }

  const handleCheck = async (cartId: number, checked: number) => {
    try {
      setItems(prev => prev.map(i => i.cartId === cartId ? { ...i, checked: checked ? 1 : 0 } : i))
      await checkCartItems([cartId])
    } catch {
      toast('操作失败', 'error')
      loadCart()
    }
  }

  const handleCheckAll = async (check: boolean) => {
    const ids = items.map(i => i.cartId)
    try {
      setItems(prev => prev.map(i => ({ ...i, checked: check ? 1 : 0 })))
      await checkCartItems(ids)
    } catch {
      toast('操作失败', 'error')
    }
  }

  const handleDelete = async (cartId: number) => {
    try {
      await deleteCartItems([cartId])
      setItems(prev => prev.filter(i => i.cartId !== cartId))
      toast('已删除', 'success')
    } catch {
      toast('删除失败', 'error')
    }
  }

  const handleBatchDelete = async () => {
    const checkedIds = items.filter(i => i.checked === 1).map(i => i.cartId)
    if (checkedIds.length === 0) {
      toast('请先选择商品', 'error')
      return
    }
    try {
      await deleteCartItems(checkedIds)
      setItems(prev => prev.filter(i => i.checked !== 1))
      toast('已删除', 'success')
    } catch {
      toast('删除失败', 'error')
    }
  }

  const handleQuantityChange = async (item: UserCart, delta: number) => {
    const newNum = item.buyNum + delta
    if (newNum < 1) return
    try {
      setItems(prev => prev.map(i => i.cartId === item.cartId ? { ...i, buyNum: newNum } : i))
      await addToCart({ skuId: item.skuId, spuId: item.spuId, buyNum: newNum })
    } catch {
      toast('修改失败', 'error')
      loadCart()
    }
  }

  const checkedItems = items.filter(i => i.checked === 1)
  const totalPrice = checkedItems.reduce((sum, i) => sum + i.price * i.buyNum, 0)

  const handleCheckout = () => {
    if (checkedItems.length === 0) {
      toast('请选择商品', 'error')
      return
    }
    navigate('/checkout', { state: { items: checkedItems } })
  }

  if (!isLoggedIn) return null

  return (
    <div className="bg-gray-100 min-h-screen pb-24">
      <div className="bg-white px-4 py-3 flex items-center sticky top-12 z-10 border-b border-gray-100">
        <h1 className="text-base font-bold">购物车</h1>
        <button
          onClick={() => setEditing(!editing)}
          className="ml-auto text-sm text-gray-500"
        >
          {editing ? '完成' : '管理'}
        </button>
      </div>

      {loading ? (
        <div className="space-y-2 p-3">
          {[1, 2, 3].map(i => (
            <div key={i} className="card p-4 flex gap-3 animate-pulse">
              <div className="w-5 h-5 rounded-full bg-gray-200 shrink-0 mt-0.5" />
              <div className="w-20 h-20 rounded-lg bg-gray-200 shrink-0" />
              <div className="flex-1 space-y-2">
                <div className="h-4 bg-gray-200 rounded w-3/4" />
                <div className="h-4 bg-gray-200 rounded w-1/2" />
              </div>
            </div>
          ))}
        </div>
      ) : items.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-24">
          <div className="text-6xl mb-4">🛒</div>
          <p className="text-gray-400 mb-2">购物车是空的</p>
          <button onClick={() => navigate('/')} className="btn-taobao">去逛逛</button>
        </div>
      ) : (
        <div className="space-y-2 p-3">
          {items.map(item => (
            <div key={item.cartId} className="card p-4 flex items-start gap-3">
              {/* Checkbox */}
              <button
                onClick={() => handleCheck(item.cartId, item.checked === 1 ? 0 : 1)}
                className={`w-5 h-5 rounded-full border-2 shrink-0 mt-0.5 flex items-center justify-center transition-colors ${
                  item.checked === 1 ? 'bg-taobao-500 border-taobao-500' : 'border-gray-300'
                }`}
              >
                {item.checked === 1 && (
                  <svg className="w-3 h-3 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
                  </svg>
                )}
              </button>

              {/* Image */}
              <div className="w-20 h-20 rounded-lg bg-gray-100 overflow-hidden shrink-0"
                   onClick={() => navigate(`/goods/${item.spuId}`)}>
                {item.mainImg ? (
                  <img src={item.mainImg} alt="" className="w-full h-full object-cover" />
                ) : (
                  <div className="w-full h-full flex items-center justify-center text-2xl">📦</div>
                )}
              </div>

              {/* Info */}
              <div className="flex-1 min-w-0">
                <p className="text-sm text-gray-800 line-clamp-2"
                   onClick={() => navigate(`/goods/${item.spuId}`)}>{item.title}</p>
                <p className="text-xs text-gray-400 mt-1">{item.skuSpec}</p>
                <div className="flex items-center justify-between mt-2">
                  <span className="text-taobao-500 font-bold">¥{item.price.toFixed(2)}</span>
                  {editing ? (
                    <button
                      onClick={() => handleDelete(item.cartId)}
                      className="text-xs text-red-400 px-2"
                    >
                      删除
                    </button>
                  ) : (
                    <div className="flex items-center gap-0">
                      <button
                        onClick={() => handleQuantityChange(item, -1)}
                        className="w-6 h-6 rounded-full border border-gray-200 flex items-center justify-center text-gray-400 text-xs"
                      >-</button>
                      <span className="w-8 text-center text-sm">{item.buyNum}</span>
                      <button
                        onClick={() => handleQuantityChange(item, 1)}
                        className="w-6 h-6 rounded-full border border-gray-200 flex items-center justify-center text-gray-400 text-xs"
                      >+</button>
                    </div>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Bottom Bar */}
      {items.length > 0 && (
        <div className="fixed bottom-14 left-0 right-0 bg-white border-t border-gray-100 z-40">
          <div className="max-w-6xl mx-auto flex items-center h-14 px-4 gap-3">
            {/* Select All */}
            <button
              onClick={() => handleCheckAll(items.some(i => i.checked === 0))}
              className="flex items-center gap-1.5"
            >
              <div className={`w-5 h-5 rounded-full border-2 flex items-center justify-center transition-colors ${
                items.every(i => i.checked === 1) ? 'bg-taobao-500 border-taobao-500' : 'border-gray-300'
              }`}>
                {items.every(i => i.checked === 1) && (
                  <svg className="w-3 h-3 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
                  </svg>
                )}
              </div>
              <span className="text-sm text-gray-500">全选</span>
            </button>

            {editing ? (
              <button onClick={handleBatchDelete} className="ml-auto text-sm text-red-500">
                删除选中
              </button>
            ) : (
              <>
                <div className="flex-1 text-right">
                  <span className="text-sm text-gray-500">合计:</span>
                  <span className="text-taobao-500 font-bold text-lg ml-1">¥{totalPrice.toFixed(2)}</span>
                </div>
                <button onClick={handleCheckout} className="btn-taobao px-10 h-10">
                  结算({checkedItems.length})
                </button>
              </>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
