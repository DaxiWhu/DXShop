import { useState, useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { getUserOrders, getUserOrderStatusCount, cancelOrder, confirmReceive } from '../api'
import { useAuthStore } from '../store/auth'
import { useToast } from '../components/Toast'
import type { UserSimpleOrderDTO, OrderStatusCountDTO } from '../api/types'

const STATUS_TABS = [
  { key: -1, label: '全部' },
  { key: 0, label: '待付款' },
  { key: 1, label: '待发货' },
  { key: 2, label: '待收货' },
  { key: 3, label: '已完成' },
]

export default function Orders() {
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()
  const { isLoggedIn } = useAuthStore()
  const { toast } = useToast()

  const statusParam = searchParams.get('status')
  const [activeTab, setActiveTab] = useState(statusParam !== null ? Number(statusParam) : -1)
  const [orders, setOrders] = useState<UserSimpleOrderDTO[]>([])
  const [counts, setCounts] = useState<OrderStatusCountDTO | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!isLoggedIn) {
      navigate('/login')
      return
    }
    loadOrders()
  }, [activeTab, isLoggedIn])

  const loadOrders = async () => {
    try {
      setLoading(true)
      const [orderData, countData] = await Promise.all([
        getUserOrders({ orderStatus: activeTab === -1 ? undefined : activeTab }),
        getUserOrderStatusCount(),
      ])
      setOrders(orderData || [])
      setCounts(countData)
    } catch {
      setOrders([])
    } finally {
      setLoading(false)
    }
  }

  const handleCancelOrder = async (orderId: number, e: React.MouseEvent) => {
    e.stopPropagation()
    try {
      await cancelOrder(orderId)
      toast('订单已取消', 'success')
      loadOrders()
    } catch {
      toast('取消失败', 'error')
    }
  }

  const handleConfirmReceive = async (orderId: number, e: React.MouseEvent) => {
    e.stopPropagation()
    try {
      await confirmReceive(orderId)
      toast('已确认收货', 'success')
      loadOrders()
    } catch {
      toast('操作失败', 'error')
    }
  }

  const switchTab = (key: number) => {
    setActiveTab(key)
    if (key === -1) {
      searchParams.delete('status')
    } else {
      searchParams.set('status', String(key))
    }
    setSearchParams(searchParams)
  }

  if (!isLoggedIn) return null

  return (
    <div className="bg-gray-100 min-h-screen">
      <div className="bg-white px-4 py-3 flex items-center sticky top-12 z-10 border-b border-gray-100">
        <h1 className="text-base font-bold">我的订单</h1>
      </div>

      {/* Tab */}
      <div className="bg-white border-b border-gray-100 overflow-x-auto">
        <div className="flex">
          {STATUS_TABS.map(tab => {
            const count = counts ? (
              tab.key === 0 ? counts.unpaidCount :
              tab.key === 1 ? counts.unshippedCount :
              tab.key === 2 ? counts.shippedCount :
              tab.key === 3 ? counts.completedCount : 0
            ) : 0
            return (
              <button
                key={tab.key}
                onClick={() => switchTab(tab.key)}
                className={`flex-shrink-0 flex-1 py-3 text-center text-sm font-medium border-b-2 transition-colors ${
                  activeTab === tab.key
                    ? 'text-taobao-500 border-taobao-500'
                    : 'text-gray-500 border-transparent'
                }`}
              >
                {tab.label}
                {(count > 0 && tab.key !== -1) && (
                  <span className={`ml-1 text-xs px-1.5 py-0.5 rounded-full ${
                    activeTab === tab.key ? 'bg-taobao-500 text-white' : 'bg-gray-100 text-gray-500'
                  }`}>
                    {count}
                  </span>
                )}
              </button>
            )
          })}
        </div>
      </div>

      {/* Order List */}
      <div className="p-3 space-y-2">
        {loading ? (
          [1, 2, 3].map(i => (
            <div key={i} className="card p-4 animate-pulse space-y-3">
              <div className="h-5 bg-gray-200 rounded w-1/3" />
              <div className="flex gap-3">
                <div className="w-16 h-16 bg-gray-200 rounded-lg shrink-0" />
                <div className="flex-1 space-y-2">
                  <div className="h-4 bg-gray-200 rounded w-3/4" />
                  <div className="h-4 bg-gray-200 rounded w-1/2" />
                </div>
              </div>
            </div>
          ))
        ) : orders.length === 0 ? (
          <div className="flex flex-col items-center py-20">
            <div className="text-5xl mb-4">📋</div>
            <p className="text-gray-400">暂无订单</p>
          </div>
        ) : (
          orders.map(order => (
            <div
              key={order.orderId}
              onClick={() => navigate(`/order/${order.orderId}`)}
              className="card p-4 cursor-pointer"
            >
              {/* Header */}
              <div className="flex items-center justify-between mb-3">
                <span className="text-sm font-medium text-gray-700">{order.shopName}</span>
                <span className={`text-xs px-2 py-0.5 rounded ${
                  order.orderStatus === 0 ? 'text-taobao-500 bg-taobao-50' :
                  order.orderStatus === 3 ? 'text-green-500 bg-green-50' :
                  order.orderStatus === 5 ? 'text-gray-400 bg-gray-100' :
                  'text-gray-500 bg-gray-100'
                }`}>
                  {['待付款', '待发货', '待收货', '已完成', '退款中', '已取消'][order.orderStatus] || '未知'}
                </span>
              </div>

              {/* Items */}
              {order.items?.map((item, i) => (
                <div key={i} className="flex gap-3 py-2">
                  <div className="w-16 h-16 rounded-lg bg-gray-100 overflow-hidden shrink-0">
                    {item.goodsImg ? (
                      <img src={item.goodsImg} alt="" className="w-full h-full object-cover" />
                    ) : (
                      <div className="w-full h-full flex items-center justify-center text-lg">📦</div>
                    )}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm text-gray-800 line-clamp-2">{item.goodsName}</p>
                    <p className="text-xs text-gray-400 mt-0.5">{item.skuSpec}</p>
                    <div className="flex items-center justify-between mt-1">
                      <span className="text-taobao-500 font-bold text-sm">¥{item.perPrice.toFixed(2)}</span>
                      <span className="text-xs text-gray-400">x{item.buyNum}</span>
                    </div>
                  </div>
                </div>
              ))}

              {/* Footer */}
              <div className="flex items-center justify-between mt-3 pt-3 border-t border-gray-50">
                <span className="text-xs text-gray-400">{order.createTime}</span>
                <div className="flex gap-2" onClick={e => e.stopPropagation()}>
                  {order.orderStatus === 0 && (
                    <button
                      onClick={e => handleCancelOrder(order.orderId, e)}
                      className="text-xs px-3 py-1.5 rounded-full border border-gray-200 text-gray-500"
                    >
                      取消订单
                    </button>
                  )}
                  {order.orderStatus === 2 && (
                    <button
                      onClick={e => handleConfirmReceive(order.orderId, e)}
                      className="btn-taobao-sm"
                    >
                      确认收货
                    </button>
                  )}
                  <span className="text-xs text-gray-400 self-center">
                    共{order.items?.reduce((s, i) => s + i.buyNum, 0) || 0}件 合计:
                    <span className="text-taobao-500 font-bold ml-1">¥{order.price.toFixed(2)}</span>
                  </span>
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  )
}
