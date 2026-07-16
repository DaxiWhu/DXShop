import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '../store/auth'
import { useToast } from '../components/Toast'
import {
  getFullShopInfo, updateShop, createShopSpu, getShopSpuPage,
  getShopOrders, getShopOrderStatusCount, getShopRefundRequests,
  getShopAddressRequests, auditRefund, auditAddressModify,
} from '../api'
import type { UserShopDTO, GoodsToChangeDTO, UserSimpleOrderDTO, OrderStatusCountDTO,
  RefundRequestSimpleDTO, AddressModifyRequestDTO } from '../api/types'

type Tab = 'overview' | 'goods' | 'orders' | 'refunds' | 'address-requests' | 'settings'

export default function ShopManage() {
  const navigate = useNavigate()
  const { isShopMode, shopId, shopName, logoutShop } = useAuthStore()
  const { toast } = useToast()

  const [tab, setTab] = useState<Tab>('overview')
  const [shopInfo, setShopInfo] = useState<UserShopDTO | null>(null)
  const [goods, setGoods] = useState<GoodsToChangeDTO[]>([])
  const [orders, setOrders] = useState<UserSimpleOrderDTO[]>([])
  const [orderCount, setOrderCount] = useState<OrderStatusCountDTO | null>(null)
  const [refunds, setRefunds] = useState<RefundRequestSimpleDTO[]>([])
  const [addrRequests, setAddrRequests] = useState<AddressModifyRequestDTO[]>([])
  const [loading, setLoading] = useState(true)

  // New goods form
  const [showNewGoods, setShowNewGoods] = useState(false)
  const [newGoods, setNewGoods] = useState({ title: '', price: '', subTitle: '' })

  // Edit shop form
  const [editShop, setEditShop] = useState({ shopDesc: '', contactPhone: '', contactEmail: '', businessHours: '' })

  useEffect(() => {
    if (!isShopMode) {
      navigate('/')
      return
    }
    loadShopInfo()
    if (tab === 'goods') loadGoods()
    if (tab === 'orders') loadOrders()
    if (tab === 'refunds') loadRefunds()
    if (tab === 'address-requests') loadAddrRequests()
  }, [tab, isShopMode])

  const loadShopInfo = async () => {
    try {
      setLoading(true)
      const data = await getFullShopInfo()
      setShopInfo(data)
      setEditShop({
        shopDesc: data.shopDesc || '',
        contactPhone: data.contactPhone || '',
        contactEmail: data.contactEmail || '',
        businessHours: data.businessHours || '',
      })
    } catch { toast('加载店铺信息失败', 'error') } finally { setLoading(false) }
  }

  const loadGoods = async () => {
    try {
      const data = await getShopSpuPage({ page: 1, size: 50 })
      setGoods(data || [])
    } catch {}
  }

  const loadOrders = async () => {
    try {
      const count = await getShopOrderStatusCount()
      setOrderCount(count)
      const data = await getShopOrders({})
      setOrders(data || [])
    } catch {}
  }

  const loadRefunds = async () => {
    try {
      const data = await getShopRefundRequests({})
      setRefunds(data || [])
    } catch {}
  }

  const loadAddrRequests = async () => {
    try {
      const data = await getShopAddressRequests({})
      setAddrRequests(data || [])
    } catch {}
  }

  const handleCreateGoods = async () => {
    if (!newGoods.title || !newGoods.price) {
      toast('请填写商品信息', 'error')
      return
    }
    try {
      await createShopSpu({
        title: newGoods.title,
        price: parseFloat(newGoods.price),
        subTitle: newGoods.subTitle,
      })
      toast('商品创建成功', 'success')
      setShowNewGoods(false)
      setNewGoods({ title: '', price: '', subTitle: '' })
      loadGoods()
    } catch { toast('创建失败', 'error') }
  }

  const handleUpdateShop = async () => {
    try {
      await updateShop(editShop)
      toast('店铺信息已更新', 'success')
    } catch { toast('更新失败', 'error') }
  }

  const handleAuditRefund = async (orderId: number, approved: boolean) => {
    try {
      await auditRefund({ orderId, result: approved ? 1 : 2 })
      toast(approved ? '已同意退款' : '已拒绝退款', 'success')
      loadRefunds()
    } catch { toast('操作失败', 'error') }
  }

  const handleAuditAddress = async (id: number, approved: boolean) => {
    try {
      await auditAddressModify({ id, result: approved ? 1 : 2 })
      toast(approved ? '已同意地址修改' : '已拒绝地址修改', 'success')
      loadAddrRequests()
    } catch { toast('操作失败', 'error') }
  }

  const tabs: { key: Tab; label: string }[] = [
    { key: 'overview', label: '概览' },
    { key: 'goods', label: '商品管理' },
    { key: 'orders', label: '订单管理' },
    { key: 'refunds', label: '退款审核' },
    { key: 'address-requests', label: '地址修改' },
    { key: 'settings', label: '店铺设置' },
  ]

  if (!isShopMode) return null

  return (
    <div className="bg-gray-100 min-h-screen">
      {/* Tab Nav */}
      <div className="bg-white sticky top-12 z-10 border-b border-gray-100">
        <div className="flex overflow-x-auto">
          {tabs.map(t => (
            <button
              key={t.key}
              onClick={() => setTab(t.key)}
              className={`flex-shrink-0 px-4 py-3 text-sm font-medium border-b-2 transition-colors ${
                tab === t.key ? 'text-taobao-500 border-taobao-500' : 'text-gray-500 border-transparent'
              }`}
            >
              {t.label}
            </button>
          ))}
        </div>
      </div>

      <div className="p-4">
        {/* Overview */}
        {tab === 'overview' && shopInfo && (
          <div className="space-y-4">
            <div className="card p-4 text-center">
              <div className="w-20 h-20 rounded-full bg-gray-100 mx-auto mb-3 overflow-hidden">
                {shopInfo.logoUrl && <img src={shopInfo.logoUrl} alt="" className="w-full h-full object-cover" />}
              </div>
              <h2 className="text-lg font-bold">{shopInfo.shopName}</h2>
              <p className="text-sm text-gray-400 mt-1">{shopInfo.shopDesc}</p>
              <div className="flex justify-center gap-8 mt-4 text-sm">
                <div><span className="text-gray-400">状态</span><p className="font-medium">{shopInfo.shopStatus === 1 ? '营业中' : '已关闭'}</p></div>
                <div><span className="text-gray-400">电话</span><p className="font-medium">{shopInfo.contactPhone || '未设置'}</p></div>
                <div><span className="text-gray-400">营业时间</span><p className="font-medium">{shopInfo.businessHours || '未设置'}</p></div>
              </div>
            </div>

            {orderCount && (
              <div className="card p-4">
                <h3 className="text-sm font-bold mb-3">订单统计</h3>
                <div className="grid grid-cols-3 gap-3 text-center text-sm">
                  <div><p className="text-2xl font-bold text-taobao-500">{orderCount.unpaidCount}</p><p className="text-gray-400 text-xs mt-1">待付款</p></div>
                  <div><p className="text-2xl font-bold text-blue-500">{orderCount.unshippedCount}</p><p className="text-gray-400 text-xs mt-1">待发货</p></div>
                  <div><p className="text-2xl font-bold text-green-500">{orderCount.completedCount}</p><p className="text-gray-400 text-xs mt-1">已完成</p></div>
                </div>
              </div>
            )}
          </div>
        )}

        {/* Goods Management */}
        {tab === 'goods' && (
          <div>
            <button onClick={() => setShowNewGoods(true)} className="btn-taobao-sm mb-4">+ 新增商品</button>
            {showNewGoods && (
              <div className="card p-4 mb-4 space-y-3">
                <input placeholder="商品标题" value={newGoods.title} onChange={e => setNewGoods({...newGoods, title: e.target.value})} className="w-full h-10 px-3 border border-gray-200 rounded-lg text-sm" />
                <input placeholder="价格" type="number" value={newGoods.price} onChange={e => setNewGoods({...newGoods, price: e.target.value})} className="w-full h-10 px-3 border border-gray-200 rounded-lg text-sm" />
                <input placeholder="副标题" value={newGoods.subTitle} onChange={e => setNewGoods({...newGoods, subTitle: e.target.value})} className="w-full h-10 px-3 border border-gray-200 rounded-lg text-sm" />
                <div className="flex gap-2">
                  <button onClick={() => setShowNewGoods(false)} className="flex-1 h-9 rounded-full border border-gray-200 text-sm text-gray-500">取消</button>
                  <button onClick={handleCreateGoods} className="flex-1 h-9 rounded-full bg-taobao-500 text-white text-sm">创建</button>
                </div>
              </div>
            )}
            <div className="space-y-2">
              {goods.map(g => (
                <div key={g.spuId} className="card p-3 flex items-center gap-3">
                  <div className="w-16 h-16 rounded-lg bg-gray-100 overflow-hidden shrink-0">
                    {g.mainImg ? <img src={g.mainImg} alt="" className="w-full h-full object-cover" /> : <div className="w-full h-full flex items-center justify-center">📦</div>}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm line-clamp-2">{g.title}</p>
                    <p className="text-taobao-500 text-sm font-bold mt-1">¥{g.price.toFixed(2)}</p>
                  </div>
                  <span className={`text-xs px-2 py-0.5 rounded ${g.status === 1 ? 'bg-green-50 text-green-500' : 'bg-gray-100 text-gray-400'}`}>
                    {g.status === 1 ? '上架' : '下架'}
                  </span>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Orders */}
        {tab === 'orders' && (
          <div className="space-y-2">
            {orders.map(order => (
              <div key={order.orderId} className="card p-4">
                <div className="flex justify-between text-sm mb-2">
                  <span className="text-gray-500">{order.orderSn}</span>
                  <span className="text-taobao-500 font-bold">¥{order.price.toFixed(2)}</span>
                </div>
                {order.items?.map((item, i) => (
                  <div key={i} className="flex gap-2 text-sm mt-1">
                    <span className="line-clamp-1 flex-1">{item.goodsName}</span>
                    <span className="text-gray-400">x{item.buyNum}</span>
                  </div>
                ))}
                <div className="flex justify-between items-center mt-2 pt-2 border-t border-gray-50">
                  <span className="text-xs text-gray-400">{order.createTime}</span>
                  <span className="text-xs px-2 py-0.5 rounded bg-gray-100 text-gray-500">
                    {['待付款', '待发货', '待收货', '已完成'][order.orderStatus] || '未知'}
                  </span>
                </div>
              </div>
            ))}
            {orders.length === 0 && <p className="text-center py-10 text-gray-400">暂无订单</p>}
          </div>
        )}

        {/* Refunds */}
        {tab === 'refunds' && (
          <div className="space-y-2">
            {refunds.map(r => (
              <div key={r.refundId} className="card p-4">
                <div className="flex justify-between text-sm mb-2">
                  <span className="text-gray-500">订单: {r.orderSn}</span>
                  <span className="text-taobao-500 font-bold">¥{r.refundAmount.toFixed(2)}</span>
                </div>
                <p className="text-sm text-gray-600 mb-2">{r.reason}</p>
                <div className="flex justify-between items-center">
                  <span className="text-xs text-gray-400">{r.createTime}</span>
                  {r.status === 0 && (
                    <div className="flex gap-2">
                      <button onClick={() => handleAuditRefund(r.orderId, false)} className="text-xs px-3 py-1 rounded-full border border-gray-200 text-gray-500">拒绝</button>
                      <button onClick={() => handleAuditRefund(r.orderId, true)} className="btn-taobao-sm">同意</button>
                    </div>
                  )}
                  {r.status !== 0 && (
                    <span className="text-xs px-2 py-0.5 rounded bg-gray-100 text-gray-500">
                      {r.status === 1 ? '已同意' : '已拒绝'}
                    </span>
                  )}
                </div>
              </div>
            ))}
            {refunds.length === 0 && <p className="text-center py-10 text-gray-400">暂无退款申请</p>}
          </div>
        )}

        {/* Address Requests */}
        {tab === 'address-requests' && (
          <div className="space-y-2">
            {addrRequests.map(r => (
              <div key={r.id} className="card p-4">
                <p className="text-sm text-gray-500 mb-1">订单: {r.orderSn}</p>
                <p className="text-sm text-gray-700 mb-2">新地址: {r.newAddress}</p>
                <div className="flex justify-between items-center">
                  <span className="text-xs text-gray-400">{r.createTime}</span>
                  {r.status === 0 && (
                    <div className="flex gap-2">
                      <button onClick={() => handleAuditAddress(r.id, false)} className="text-xs px-3 py-1 rounded-full border border-gray-200 text-gray-500">拒绝</button>
                      <button onClick={() => handleAuditAddress(r.id, true)} className="btn-taobao-sm">同意</button>
                    </div>
                  )}
                </div>
              </div>
            ))}
            {addrRequests.length === 0 && <p className="text-center py-10 text-gray-400">暂无地址修改申请</p>}
          </div>
        )}

        {/* Settings */}
        {tab === 'settings' && (
          <div className="space-y-4">
            <div className="card p-4 space-y-3">
              <h3 className="text-sm font-bold">店铺设置</h3>
              <div>
                <label className="text-xs text-gray-400 block mb-1">店铺描述</label>
                <input value={editShop.shopDesc} onChange={e => setEditShop({...editShop, shopDesc: e.target.value})} className="w-full h-10 px-3 border border-gray-200 rounded-lg text-sm" />
              </div>
              <div>
                <label className="text-xs text-gray-400 block mb-1">联系电话</label>
                <input value={editShop.contactPhone} onChange={e => setEditShop({...editShop, contactPhone: e.target.value})} className="w-full h-10 px-3 border border-gray-200 rounded-lg text-sm" />
              </div>
              <div>
                <label className="text-xs text-gray-400 block mb-1">联系邮箱</label>
                <input value={editShop.contactEmail} onChange={e => setEditShop({...editShop, contactEmail: e.target.value})} className="w-full h-10 px-3 border border-gray-200 rounded-lg text-sm" />
              </div>
              <div>
                <label className="text-xs text-gray-400 block mb-1">营业时间</label>
                <input value={editShop.businessHours} onChange={e => setEditShop({...editShop, businessHours: e.target.value})} placeholder="如: 9:00-21:00" className="w-full h-10 px-3 border border-gray-200 rounded-lg text-sm" />
              </div>
              <button onClick={handleUpdateShop} className="btn-taobao w-full">保存修改</button>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
