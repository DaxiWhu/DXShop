import { create } from 'zustand'

interface AuthState {
  token: string | null
  userId: number | null
  nickname: string | null
  avatarUrl: string | null
  isLoggedIn: boolean

  // 店铺模式
  shopToken: string | null
  shopId: number | null
  shopName: string | null
  isShopMode: boolean

  // Actions
  login: (token: string, userId: number, nickname: string, avatarUrl: string) => void
  logout: () => void
  loginShop: (token: string, shopId: number, shopName: string) => void
  logoutShop: () => void
}

function loadFromStorage() {
  return {
    token: localStorage.getItem('token'),
    userId: Number(localStorage.getItem('userId')) || null,
    nickname: localStorage.getItem('nickname'),
    avatarUrl: localStorage.getItem('avatarUrl'),
    shopToken: localStorage.getItem('shopToken'),
    shopId: Number(localStorage.getItem('shopId')) || null,
    shopName: localStorage.getItem('shopName'),
  }
}

export const useAuthStore = create<AuthState>((set, get) => {
  const stored = loadFromStorage()

  return {
    token: stored.token,
    userId: stored.userId,
    nickname: stored.nickname,
    avatarUrl: stored.avatarUrl,
    isLoggedIn: !!stored.token,

    shopToken: stored.shopToken,
    shopId: stored.shopId,
    shopName: stored.shopName,
    isShopMode: !!stored.shopToken,

    login: (token, userId, nickname, avatarUrl) => {
      localStorage.setItem('token', token)
      localStorage.setItem('userId', String(userId))
      localStorage.setItem('nickname', nickname || '')
      localStorage.setItem('avatarUrl', avatarUrl || '')
      set({ token, userId, nickname, avatarUrl, isLoggedIn: true })
    },

    logout: () => {
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('nickname')
      localStorage.removeItem('avatarUrl')
      localStorage.removeItem('shopToken')
      localStorage.removeItem('shopId')
      localStorage.removeItem('shopName')
      set({
        token: null, userId: null, nickname: null, avatarUrl: null, isLoggedIn: false,
        shopToken: null, shopId: null, shopName: null, isShopMode: false,
      })
    },

    loginShop: (token, shopId, shopName) => {
      localStorage.setItem('shopToken', token)
      localStorage.setItem('shopId', String(shopId))
      localStorage.setItem('shopName', shopName || '')
      set({ shopToken: token, shopId, shopName, isShopMode: true })
    },

    logoutShop: () => {
      localStorage.removeItem('shopToken')
      localStorage.removeItem('shopId')
      localStorage.removeItem('shopName')
      set({ shopToken: null, shopId: null, shopName: null, isShopMode: false })
    },
  }
})
