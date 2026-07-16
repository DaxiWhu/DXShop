import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { getOrderDetail, applyRefund, applyAddressModify, confirmReceive, cancelOrder, getRefundDetail } from '../api'
import { useToast } from '../components/Toast'
import type { UserOrderDTO, RefundRequestDetailDTO } from '../api/types'

const STATUS_MAP: Record<number, string> = {
  0: '待付款', 1: '待发货', 2: '待收货', 3: '已完成', 4: '退款中', 5: '已取消', 6: '已退款',
}

export default function OrderDetail() {
  const { id } = useParams<{ id: string }>()
  const orderId = Number(id)
  const navigate = useNavigate()
  const { toast } = useToast()

  const [order, setOrder] = useState<UserOrderDTO | null>(null)
  const [refundDetail, setRefundDetail] = useState<RefundRequestDetailDTO | null>(null)
  const [loading, setLoading] = useState(true)
  const [showRefundForm, setShowRefundForm] = useState(false)
  const [showAddressForm, setShowAddressForm] = useState(false)
  const [refundReason, setRefundReason] = useState('')
  const [newAddress, setNewAddress] = useState('')

  useEffect(() => {
    loadDetail()
  }, [orderId])

  const loadDetail = async () => {
    try {
      setLoading(true)
      const data = await getOrderDetail(orderId)
      setOrder(data)
      if (data.orderStatus >= 4) {
        try {
          const rd = await getRefundDetail(orderId)
          setRefundDetail(rd)
        } catch {}
      }
    } catch {
      toast('加载订单详情失败', 'error')
    } finally {
      setLoading(false)
    }
  }

  const handleCancel = async () => {
    try {
      await cancelOrder(orderId)
      toast('订单已取消', 'success')
      loadDetail()
    } catch { toast('操作失败', 'error') }
  }

  const handleConfirmReceive = async () => {
    try {
      await confirmReceive(orderId)
      toast('已确认收货', 'success')
      loadDetail()
    } catch { toast('操作失败', 'error') }
  }

  const handleRefund = async () => {
    if (!refundReason) { toast('请填写退款原因', 'error'); return }
    try {
      await applyRefund({ orderId, reason: refundReason })
      toast('退款申请已提交', 'success')
      setShowRefundForm(false)
      loadDetail()
    } catch { toast('申请失败', 'error') }
  }

  const handleAddressModify = async () => {
    if (!newAddress) { toast('请填写新地址', 'error'); return }
    try {
      await applyAddressModify({ orderId, newAddress })
      toast('地址修改申请已提交', 'success')
      setShowAddressForm(false)
    } catch { toast('申请失败', 'error') }
  }

  if (loading) {
    return (
      <div className="bg-gray-100 min-h-screen animate-pulse p-4 space-y-3">
        <div className="card p-4 space-y-3">
          <div className="h-5 bg-gray-200 rounded w-1/4" />
          <div className="h-4 bg-gray-200 rounded w-3/4" />
          <div className="h-4 bg-gray-200 rounded w-1/2" />
        </div>
      </div>
    )
  }

  if (!order) {
    return (
      <div className="text-center py-20">
        <p className="text-gray-400">订单不存在</p>
      </div>
    )
  }

  return (
    <div className="bg-gray-100 min-h-screen pb-20">
      {/* Header */}
      <div className="bg-white px-4 py-3 flex items-center sticky top-12 z-10 border-b border-gray-100">
        <button onClick={() => navigate(-1)} className="mr-3">
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
        </button>
        <h1 className="text-base font-bold">订单详情</h1>
        <span className={`ml-auto text-xs px-2 py-0.5 rounded ${
          order.orderStatus === 0 ? 'bg-taobao-50 text-taobao-500' :
          order.orderStatus === 3 ? 'bg-green-50 text-green-500' :
          'bg-gray-100 text-gray-500'
        }`}>
          {STATUS_MAP[order.orderStatus] || '未知'}
        </span>
      </div>

      {/* Receiver Info */}
      <div className="bg-white mt-2 p-4">
        <div className="flex items-start gap-2 mb-1">
          <svg className="w-5 h-5 text-taobao-500 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
          </svg>
          <div>
            <p className="text-sm font-medium">{order.receiverName} <span className="text-gray-500 font-normal">{order.receiverPhone}</span></p>
            <p className="text-xs text-gray-500 mt-0.5">{order.receiverAddress}</p>
          </div>
        </div>
      </div>

      {/* Order Items */}
      <div className="bg-white mt-2 p-4">
        <div className="flex items-center gap-2 mb-3">
          <span className="text-sm font-medium">{order.shopName}</span>
        </div>
        {order.items?.map((item, i) => (
          <div key={i} className="flex gap-3 py-3 border-b border-gray-50 last:border-0">
            <div className="w-20 h-20 rounded-lg bg-gray-100 overflow-hidden shrink-0">
              {item.goodsImg ? (
                <img src={item.goodsImg} alt="" className="w-full h-full object-cover" />
              ) : (
                <div className="w-full h-full flex items-center justify-center text-xl">📦</div>
              )}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm text-gray-800 line-clamp-2">{item.goodsName}</p>
              <p className="text-xs text-gray-400 mt-1">{item.skuSpec}</p>
              <div className="flex items-center justify-between mt-2">
                <span className="text-taobao-500 font-bold">¥{item.perPrice.toFixed(2)}</span>
                <span className="text-xs text-gray-400">x{item.buyNum}</span>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Order Info */}
      <div className="bg-white mt-2 p-4">
        <div className="space-y-2 text-sm">
          <div className="flex justify-between">
            <span className="text-gray-400">订单编号</span>
            <span className="text-gray-700">{order.orderSn}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-gray-400">创建时间</span>
            <span className="text-gray-700">{order.createTime}</span>
          </div>
          {order.payTime && (
            <div className="flex justify-between">
              <span className="text-gray-400">支付时间</span>
              <span className="text-gray-700">{order.payTime}</span>
            </div>
          )}
          {order.logisticsCompany && (
            <div className="flex justify-between">
              <span className="text-gray-400">物流</span>
              <span className="text-gray-700">{order.logisticsCompany}: {order.logisticsNo}</span>
            </div>
          )}
          <div className="flex justify-between pt-2 border-t border-gray-50">
            <span className="text-gray-400">实付款</span>
            <span className="text-taobao-500 font-bold text-base">¥{order.price.toFixed(2)}</span>
          </div>
        </div>
      </div>

      {/* Refund Info (if any) */}
      {refundDetail && (
        <div className="bg-white mt-2 p-4">
          <h3 className="text-sm font-bold mb-2">退款信息</h3>
          <div className="space-y-1.5 text-sm">
            <div className="flex justify-between">
              <span className="text-gray-400">退款金额</span>
              <span className="text-gray-700">¥{refundDetail.refundAmount.toFixed(2)}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-400">退款原因</span>
              <span className="text-gray-700">{refundDetail.reason}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-400">状态</span>
              <span className="text-gray-700">
                {refundDetail.status === 0 ? '待审核' : refundDetail.status === 1 ? '已同意' : refundDetail.status === 2 ? '已拒绝' : '已完成'}
              </span>
            </div>
          </div>
        </div>
      )}

      {/* Actions */}
      <div className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-100 z-50">
        <div className="max-w-6xl mx-auto flex items-center justify-end h-14 px-4 gap-2">
          {order.orderStatus === 0 && (
            <>
              <button onClick={handleCancel} className="text-xs px-4 py-2 rounded-full border border-gray-200 text-gray-500">
                取消订单
              </button>
              <button className="btn-taobao-sm px-6">
                去付款
              </button>
            </>
          )}
          {order.orderStatus === 1 && (
            <button onClick={() => setShowRefundForm(true)} className="text-xs px-4 py-2 rounded-full border border-gray-200 text-gray-500">
              申请退款
            </button>
          )}
          {order.orderStatus === 2 && (
            <>
              <button onClick={() => setShowAddressForm(true)} className="text-xs px-4 py-2 rounded-full border border-gray-200 text-gray-500">
                修改地址
              </button>
              <button onClick={() => setShowRefundForm(true)} className="text-xs px-4 py-2 rounded-full border border-gray-200 text-gray-500">
                申请退款
              </button>
              <button onClick={handleConfirmReceive} className="btn-taobao-sm px-6">
                确认收货
              </button>
            </>
          )}
        </div>
      </div>

      {/* Refund Modal */}
      {showRefundForm && (
        <div className="fixed inset-0 z-[100] flex items-end justify-center">
          <div className="absolute inset-0 bg-black/50" onClick={() => setShowRefundForm(false)} />
          <div className="relative bg-white rounded-t-2xl w-full max-w-lg p-6">
            <h2 className="font-bold text-base mb-4">申请退款</h2>
            <textarea
              value={refundReason}
              onChange={e => setRefundReason(e.target.value)}
              placeholder="请描述退款原因..."
              className="w-full h-24 border border-gray-200 rounded-lg p-3 text-sm resize-none focus:outline-none focus:border-taobao-500"
            />
            <button onClick={handleRefund} className="btn-taobao w-full mt-4">提交申请</button>
          </div>
        </div>
      )}

      {/* Address Modify Modal */}
      {showAddressForm && (
        <div className="fixed inset-0 z-[100] flex items-end justify-center">
          <div className="absolute inset-0 bg-black/50" onClick={() => setShowAddressForm(false)} />
          <div className="relative bg-white rounded-t-2xl w-full max-w-lg p-6">
            <h2 className="font-bold text-base mb-4">修改收货地址</h2>
            <textarea
              value={newAddress}
              onChange={e => setNewAddress(e.target.value)}
              placeholder="请输入新地址..."
              className="w-full h-24 border border-gray-200 rounded-lg p-3 text-sm resize-none focus:outline-none focus:border-taobao-500"
            />
            <button onClick={handleAddressModify} className="btn-taobao w-full mt-4">提交申请</button>
          </div>
        </div>
      )}
    </div>
  )
}
