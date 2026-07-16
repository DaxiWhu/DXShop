import { useState, useEffect } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { getAddressList, saveAddress, createOrder, getPaymentPage, paymentSuccess, checkPaymentStatus } from '../api'
import { useToast } from '../components/Toast'
import type { UserAddressDTO, UserCart, PaymentPageDTO, PaymentStatusDTO } from '../api/types'

type Step = 'checkout' | 'payment' | 'success'

export default function Checkout() {
  const navigate = useNavigate()
  const location = useLocation()
  const { toast } = useToast()
  const items: UserCart[] = location.state?.items || []

  const [step, setStep] = useState<Step>('checkout')
  const [addresses, setAddresses] = useState<UserAddressDTO[]>([])
  const [selectedAddress, setSelectedAddress] = useState<UserAddressDTO | null>(null)
  const [showAddressForm, setShowAddressForm] = useState(false)
  const [orderId, setOrderId] = useState<number | null>(null)
  const [paymentPage, setPaymentPage] = useState<PaymentPageDTO | null>(null)
  const [loading, setLoading] = useState(false)

  // 新地址表单
  const [addrForm, setAddrForm] = useState({
    receiverName: '', receiverPhone: '',
    province: '', city: '', district: '', detailAddress: '',
    isDefault: 0,
  })

  useEffect(() => {
    if (items.length === 0) {
      navigate('/cart')
      return
    }
    loadAddresses()
  }, [])

  const loadAddresses = async () => {
    try {
      const data = await getAddressList()
      setAddresses(data || [])
      const defaultAddr = data?.find(a => a.isDefault === 1) || data?.[0]
      if (defaultAddr) setSelectedAddress(defaultAddr)
    } catch {
      // 忽略
    }
  }

  const submitAddress = async () => {
    if (!addrForm.receiverName || !addrForm.receiverPhone || !addrForm.detailAddress) {
      toast('请填写完整地址信息', 'error')
      return
    }
    try {
      await saveAddress(addrForm)
      toast('地址保存成功', 'success')
      setShowAddressForm(false)
      loadAddresses()
    } catch {
      toast('保存失败', 'error')
    }
  }

  const totalPrice = items.reduce((sum, item) => sum + item.price * item.buyNum, 0)

  const handleSubmitOrder = async () => {
    if (!selectedAddress) {
      toast('请选择收货地址', 'error')
      return
    }
    try {
      setLoading(true)
      const orderData = {
        addressId: selectedAddress.addressId,
        items: items.map(item => ({
          skuId: item.skuId,
          spuId: item.spuId,
          buyNum: item.buyNum,
        })),
      }
      await createOrder(orderData)
      toast('下单成功', 'success')
      setStep('success')
    } catch {
      toast('下单失败，请重试', 'error')
    } finally {
      setLoading(false)
    }
  }

  // 模拟支付流程
  const handlePayment = async () => {
    try {
      setLoading(true)
      await paymentSuccess({ orderId, paymentMethod: 'ALIPAY' })
      toast('支付成功', 'success')
      setStep('success')
    } catch {
      toast('支付失败', 'error')
    } finally {
      setLoading(false)
    }
  }

  if (items.length === 0) return null

  return (
    <div className="bg-gray-100 min-h-screen pb-20">
      <div className="bg-white px-4 py-3 flex items-center sticky top-12 z-10 border-b border-gray-100">
        <button onClick={() => navigate(-1)} className="mr-3">
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
        </button>
        <h1 className="text-base font-bold">
          {step === 'checkout' ? '确认订单' : step === 'payment' ? '支付' : '下单成功'}
        </h1>
      </div>

      {step === 'success' && (
        <div className="flex flex-col items-center justify-center py-20 px-6 text-center">
          <div className="text-6xl mb-4">🎉</div>
          <h2 className="text-xl font-bold text-gray-800 mb-2">下单成功!</h2>
          <p className="text-gray-400 mb-8">感谢您的购买，我们将尽快发货</p>
          <div className="flex gap-3">
            <button onClick={() => navigate('/orders')} className="btn-taobao-outline">
              查看订单
            </button>
            <button onClick={() => navigate('/')} className="btn-taobao">
              继续购物
            </button>
          </div>
        </div>
      )}

      {step === 'checkout' && (
        <>
          {/* Address */}
          <div className="bg-white mt-2 p-4">
            <h2 className="text-sm font-bold mb-3">收货地址</h2>
            {selectedAddress ? (
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <p className="text-sm font-medium">
                    {selectedAddress.receiverName}
                    <span className="text-gray-500 font-normal ml-3">{selectedAddress.receiverPhone}</span>
                  </p>
                  <p className="text-xs text-gray-500 mt-1">{selectedAddress.fullAddress || `${selectedAddress.province}${selectedAddress.city}${selectedAddress.district}${selectedAddress.detailAddress}`}</p>
                </div>
                <button onClick={() => setShowAddressForm(true)} className="text-xs text-taobao-500 shrink-0">
                  更换
                </button>
              </div>
            ) : (
              <button onClick={() => setShowAddressForm(true)} className="w-full py-8 border-2 border-dashed border-gray-200 rounded-lg text-gray-400 text-sm">
                + 添加收货地址
              </button>
            )}

            {/* Address list */}
            {addresses.length > 1 && !showAddressForm && (
              <div className="mt-3 space-y-2">
                {addresses.filter(a => a.addressId !== selectedAddress?.addressId).map(a => (
                  <button
                    key={a.addressId}
                    onClick={() => setSelectedAddress(a)}
                    className="w-full text-left p-2 border border-gray-100 rounded-lg text-sm"
                  >
                    <p className="font-medium">{a.receiverName} <span className="text-gray-500 font-normal">{a.receiverPhone}</span></p>
                    <p className="text-xs text-gray-500">{a.fullAddress || `${a.province}${a.city}${a.district}${a.detailAddress}`}</p>
                  </button>
                ))}
              </div>
            )}

            {/* Add Address Form */}
            {showAddressForm && (
              <div className="mt-3 space-y-3 border-t pt-3">
                <input placeholder="收货人姓名" value={addrForm.receiverName} onChange={e => setAddrForm({...addrForm, receiverName: e.target.value})} className="w-full h-10 px-3 border border-gray-200 rounded-lg text-sm" />
                <input placeholder="手机号" value={addrForm.receiverPhone} onChange={e => setAddrForm({...addrForm, receiverPhone: e.target.value})} className="w-full h-10 px-3 border border-gray-200 rounded-lg text-sm" />
                <div className="flex gap-2">
                  <input placeholder="省" value={addrForm.province} onChange={e => setAddrForm({...addrForm, province: e.target.value})} className="flex-1 h-10 px-3 border border-gray-200 rounded-lg text-sm" />
                  <input placeholder="市" value={addrForm.city} onChange={e => setAddrForm({...addrForm, city: e.target.value})} className="flex-1 h-10 px-3 border border-gray-200 rounded-lg text-sm" />
                  <input placeholder="区" value={addrForm.district} onChange={e => setAddrForm({...addrForm, district: e.target.value})} className="flex-1 h-10 px-3 border border-gray-200 rounded-lg text-sm" />
                </div>
                <input placeholder="详细地址" value={addrForm.detailAddress} onChange={e => setAddrForm({...addrForm, detailAddress: e.target.value})} className="w-full h-10 px-3 border border-gray-200 rounded-lg text-sm" />
                <div className="flex gap-2">
                  <button onClick={() => setShowAddressForm(false)} className="flex-1 h-10 rounded-full border border-gray-200 text-sm text-gray-500">取消</button>
                  <button onClick={submitAddress} className="flex-1 h-10 rounded-full bg-taobao-500 text-white text-sm">保存</button>
                </div>
              </div>
            )}
          </div>

          {/* Order Items */}
          <div className="bg-white mt-2 p-4">
            <h2 className="text-sm font-bold mb-3">商品信息</h2>
            <div className="space-y-3">
              {items.map((item, i) => (
                <div key={i} className="flex gap-3">
                  <div className="w-20 h-20 rounded-lg bg-gray-100 overflow-hidden shrink-0">
                    {item.mainImg ? (
                      <img src={item.mainImg} alt="" className="w-full h-full object-cover" />
                    ) : (
                      <div className="w-full h-full flex items-center justify-center text-2xl">📦</div>
                    )}
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm text-gray-800 line-clamp-2">{item.title}</p>
                    <p className="text-xs text-gray-400 mt-1">{item.skuSpec}</p>
                    <div className="flex items-center justify-between mt-2">
                      <span className="text-taobao-500 font-bold">¥{item.price.toFixed(2)}</span>
                      <span className="text-xs text-gray-400">x{item.buyNum}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Bottom */}
          <div className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-100 z-50">
            <div className="max-w-6xl mx-auto flex items-center h-14 px-4 gap-3">
              <div className="flex-1 text-right">
                <span className="text-sm text-gray-500">共{items.length}件</span>
                <span className="text-sm ml-2">
                  合计: <span className="text-taobao-500 font-bold text-lg">¥{totalPrice.toFixed(2)}</span>
                </span>
              </div>
              <button
                onClick={handleSubmitOrder}
                disabled={loading}
                className="btn-taobao px-10 h-10"
              >
                {loading ? '提交中...' : '提交订单'}
              </button>
            </div>
          </div>
        </>
      )}
    </div>
  )
}
