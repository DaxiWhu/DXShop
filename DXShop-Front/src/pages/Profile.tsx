import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '../store/auth'
import { useToast } from '../components/Toast'
import { getUserDisplay, updateUserDisplay, getFavoritesShop, getFavoritesSpu, getMyShops, loginShop, getAddressList, deleteAddress, saveAddress } from '../api'
import type { UserDisplayDTO, UserFavoritesShopDTO, UserFavoritesSpuDTO, UserShopSimpleDTO, UserAddressDTO } from '../api/types'

type Section = 'main' | 'favorites-shop' | 'favorites-spu' | 'addresses' | 'shops' | 'edit-profile'

export default function Profile() {
  const navigate = useNavigate()
  const { isLoggedIn, nickname, avatarUrl, userId, login: authLogin, logout, loginShop: shopLogin } = useAuthStore()
  const { toast } = useToast()

  const [section, setSection] = useState<Section>('main')
  const [userDisplay, setUserDisplay] = useState<UserDisplayDTO | null>(null)
  const [favShops, setFavShops] = useState<UserFavoritesShopDTO[]>([])
  const [favSpus, setFavSpus] = useState<UserFavoritesSpuDTO[]>([])
  const [addresses, setAddresses] = useState<UserAddressDTO[]>([])
  const [myShops, setMyShops] = useState<UserShopSimpleDTO[]>([])

  // Edit form
  const [editNickname, setEditNickname] = useState(nickname || '')
  const [editAvatar, setEditAvatar] = useState(avatarUrl || '')

  useEffect(() => {
    if (!isLoggedIn) {
      navigate('/login')
      return
    }
    loadUserDisplay()
  }, [isLoggedIn])

  const loadUserDisplay = async () => {
    try {
      if (userId) {
        const data = await getUserDisplay(userId)
        setUserDisplay(data)
      }
    } catch {}
  }

  const loadFavShops = async () => {
    try { const data = await getFavoritesShop(); setFavShops(data || []) } catch {}
    setSection('favorites-shop')
  }

  const loadFavSpus = async () => {
    try { const data = await getFavoritesSpu(); setFavSpus(data || []) } catch {}
    setSection('favorites-spu')
  }

  const loadAddresses = async () => {
    try { const data = await getAddressList(); setAddresses(data || []) } catch {}
    setSection('addresses')
  }

  const loadMyShops = async () => {
    try { const data = await getMyShops(); setMyShops(data || []) } catch {}
    setSection('shops')
  }

  const handleEnterShop = async (shopId: number) => {
    try {
      const res = await loginShop(shopId)
      shopLogin(res.token, shopId, myShops.find(s => s.shopId === shopId)?.shopName || '')
      toast('已进入商家模式', 'success')
      navigate('/shop-manage')
    } catch {
      toast('登录店铺失败', 'error')
    }
  }

  const handleDeleteAddress = async (addressId: number) => {
    try {
      await deleteAddress(addressId)
      setAddresses(prev => prev.filter(a => a.addressId !== addressId))
      toast('已删除', 'success')
    } catch { toast('删除失败', 'error') }
  }

  const handleSaveProfile = async () => {
    try {
      await updateUserDisplay({ nickname: editNickname, avatarUrl: editAvatar })
      authLogin(localStorage.getItem('token')!, userId!, editNickname, editAvatar)
      toast('保存成功', 'success')
      setSection('main')
    } catch { toast('保存失败', 'error') }
  }

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  if (!isLoggedIn) return null

  // Section views
  if (section === 'edit-profile') {
    return (
      <div className="bg-gray-100 min-h-screen">
        <div className="bg-white px-4 py-3 flex items-center sticky top-12 z-10">
          <button onClick={() => setSection('main')} className="mr-3">
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
            </svg>
          </button>
          <h1 className="text-base font-bold">编辑资料</h1>
        </div>
        <div className="p-4 space-y-4">
          <div>
            <label className="text-sm text-gray-500 block mb-1">昵称</label>
            <input value={editNickname} onChange={e => setEditNickname(e.target.value)} className="w-full h-10 px-3 border border-gray-200 rounded-lg text-sm" />
          </div>
          <div>
            <label className="text-sm text-gray-500 block mb-1">头像URL</label>
            <input value={editAvatar} onChange={e => setEditAvatar(e.target.value)} className="w-full h-10 px-3 border border-gray-200 rounded-lg text-sm" placeholder="https://..." />
          </div>
          <button onClick={handleSaveProfile} className="btn-taobao w-full">保存</button>
        </div>
      </div>
    )
  }

  // Favorites Shop
  if (section === 'favorites-shop') {
    return (
      <SubPage title="收藏的店铺" onBack={() => setSection('main')}>
        {favShops.length === 0 ? (
          <Empty text="暂无收藏店铺" />
        ) : (
          <div className="space-y-2">
            {favShops.map(shop => (
              <div key={shop.shopId} className="card p-4 flex items-center gap-3">
                <div className="w-12 h-12 rounded-full bg-gray-100 overflow-hidden">
                  {shop.logoUrl && <img src={shop.logoUrl} alt="" className="w-full h-full object-cover" />}
                </div>
                <span className="text-sm font-medium">{shop.shopName}</span>
              </div>
            ))}
          </div>
        )}
      </SubPage>
    )
  }

  // Favorites SPU
  if (section === 'favorites-spu') {
    return (
      <SubPage title="收藏的商品" onBack={() => setSection('main')}>
        {favSpus.length === 0 ? (
          <Empty text="暂无收藏商品" />
        ) : (
          <div className="space-y-2">
            {favSpus.map(spu => (
              <div
                key={spu.spuId}
                onClick={() => navigate(`/goods/${spu.spuId}`)}
                className="card p-3 flex gap-3 cursor-pointer"
              >
                <div className="w-20 h-20 rounded-lg bg-gray-100 overflow-hidden shrink-0">
                  {spu.mainImg ? <img src={spu.mainImg} alt="" className="w-full h-full object-cover" /> : <div className="w-full h-full flex items-center justify-center text-xl">📦</div>}
                </div>
                <div className="flex-1">
                  <p className="text-sm line-clamp-2">{spu.title}</p>
                  <p className="text-taobao-500 font-bold mt-1">¥{spu.price.toFixed(2)}</p>
                </div>
              </div>
            ))}
          </div>
        )}
      </SubPage>
    )
  }

  // Addresses
  if (section === 'addresses') {
    return (
      <SubPage title="收货地址" onBack={() => setSection('main')}>
        {addresses.length === 0 ? (
          <Empty text="暂无地址" />
        ) : (
          <div className="space-y-2">
            {addresses.map(addr => (
              <div key={addr.addressId} className="card p-4 flex items-start justify-between">
                <div>
                  <p className="text-sm font-medium">
                    {addr.receiverName}
                    <span className="text-gray-500 font-normal ml-2">{addr.receiverPhone}</span>
                    {addr.isDefault === 1 && <span className="text-xs text-taobao-500 ml-2 bg-taobao-50 px-1.5 py-0.5 rounded">默认</span>}
                  </p>
                  <p className="text-xs text-gray-500 mt-1">{addr.fullAddress || `${addr.province}${addr.city}${addr.district}${addr.detailAddress}`}</p>
                </div>
                <button onClick={() => handleDeleteAddress(addr.addressId)} className="text-xs text-red-400">删除</button>
              </div>
            ))}
          </div>
        )}
      </SubPage>
    )
  }

  // My Shops
  if (section === 'shops') {
    return (
      <SubPage title="我的店铺" onBack={() => setSection('main')}>
        {myShops.length === 0 ? (
          <Empty text="暂无店铺">
            <button onClick={() => navigate('/shop-manage')} className="btn-taobao-sm mt-4">创建店铺</button>
          </Empty>
        ) : (
          <div className="space-y-2">
            {myShops.map(shop => (
              <div key={shop.shopId} className="card p-4 flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium">{shop.shopName}</p>
                  <p className="text-xs text-gray-400">
                    {shop.shopStatus === 1 ? '营业中' : shop.shopStatus === 0 ? '审核中' : '已关闭'}
                  </p>
                </div>
                {shop.shopStatus === 1 && (
                  <button onClick={() => handleEnterShop(shop.shopId)} className="text-xs text-taobao-500 px-3 py-1.5 rounded-full border border-taobao-500">
                    进入管理
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
      </SubPage>
    )
  }

  // Main Profile
  return (
    <div className="bg-gray-100 min-h-screen">
      {/* User Card */}
      <div className="bg-gradient-to-r from-taobao-500 to-orange-400 p-6">
        <div className="flex items-center gap-4">
          <div className="w-16 h-16 rounded-full bg-white/20 overflow-hidden border-2 border-white/50">
            {avatarUrl ? (
              <img src={avatarUrl} alt="" className="w-full h-full object-cover" />
            ) : (
              <div className="w-full h-full flex items-center justify-center text-white text-2xl">
                {nickname?.[0] || 'U'}
              </div>
            )}
          </div>
          <div className="text-white">
            <p className="text-lg font-bold">{nickname || '未设置昵称'}</p>
            <p className="text-sm text-white/70 mt-0.5">
              {userDisplay?.isRealName === 1 ? '已实名' : '未实名'} | ID: {userId}
            </p>
          </div>
        </div>
      </div>

      {/* Menu */}
      <div className="px-3 mt-2 space-y-2">
        <div className="card divide-y divide-gray-50">
          <MenuItem label="我的订单" icon="📋" badge={null} onClick={() => navigate('/orders?status=-1')} />
          <MenuItem label="收藏的店铺" icon="🏪" badge={null} onClick={loadFavShops} />
          <MenuItem label="收藏的商品" icon="❤️" badge={null} onClick={loadFavSpus} />
          <MenuItem label="收货地址" icon="📍" badge={null} onClick={loadAddresses} />
          <MenuItem label="我的店铺" icon="🏬" badge={null} onClick={loadMyShops} />
        </div>

        <div className="card divide-y divide-gray-50">
          <MenuItem label="编辑资料" icon="✏️" badge={null} onClick={() => setSection('edit-profile')} />
          <MenuItem label="客服中心" icon="💬" badge={null} onClick={() => navigate('/chat')} />
        </div>

        <button onClick={handleLogout} className="w-full py-3 text-sm text-red-400 bg-white rounded-xl mt-4">
          退出登录
        </button>
      </div>
    </div>
  )
}

function MenuItem({ label, icon, badge, onClick }: {
  label: string; icon: string; badge: string | null; onClick: () => void
}) {
  return (
    <button onClick={onClick} className="w-full flex items-center justify-between px-4 py-3.5 hover:bg-gray-50 transition-colors">
      <div className="flex items-center gap-3">
        <span className="text-lg">{icon}</span>
        <span className="text-sm text-gray-700">{label}</span>
      </div>
      <div className="flex items-center gap-1.5">
        {badge && <span className="text-xs text-gray-400">{badge}</span>}
        <svg className="w-4 h-4 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
        </svg>
      </div>
    </button>
  )
}

function SubPage({ title, onBack, children }: { title: string; onBack: () => void; children: React.ReactNode }) {
  return (
    <div className="bg-gray-100 min-h-screen">
      <div className="bg-white px-4 py-3 flex items-center sticky top-12 z-10 border-b border-gray-100">
        <button onClick={onBack} className="mr-3">
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
        </button>
        <h1 className="text-base font-bold">{title}</h1>
      </div>
      <div className="p-3">{children}</div>
    </div>
  )
}

function Empty({ text, children }: { text: string; children?: React.ReactNode }) {
  return (
    <div className="flex flex-col items-center py-20">
      <div className="text-4xl mb-3">📭</div>
      <p className="text-gray-400">{text}</p>
      {children}
    </div>
  )
}
