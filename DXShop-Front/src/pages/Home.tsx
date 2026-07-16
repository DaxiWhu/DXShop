import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import ProductCard from '../components/ProductCard'
import { getGoodsSimple } from '../api'
import type { GoodsSimpleDTO } from '../api/types'

const CATEGORIES = [
  { name: '精选推荐', icon: '🔥' },
  { name: '数码电子', icon: '📱' },
  { name: '服饰鞋包', icon: '👗' },
  { name: '美妆护肤', icon: '💄' },
  { name: '食品生鲜', icon: '🍎' },
  { name: '家居生活', icon: '🏠' },
  { name: '母婴亲子', icon: '👶' },
  { name: '运动户外', icon: '⚽' },
]

// 示例商品 ID 池用于 Demo（实际场景需后端提供分页/推荐接口）
const DEMO_IDS = Array.from({ length: 50 }, (_, i) => i + 1)

export default function Home() {
  const navigate = useNavigate()
  const [goodsList, setGoodsList] = useState<GoodsSimpleDTO[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadGoods()
  }, [])

  const loadGoods = async () => {
    try {
      setLoading(true)
      // 加载首页商品（批量查询指定ID）
      const data = await getGoodsSimple(DEMO_IDS.slice(0, 20))
      setGoodsList(data || [])
    } catch {
      // Demo 模式: 连接不上后端时使用模拟数据
      setGoodsList(generateMockData())
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="pb-4">
      {/* Banner */}
      <div className="bg-gradient-to-b from-taobao-500/10 to-gray-100 px-4 pt-4 pb-6">
        <div className="bg-gradient-to-r from-taobao-500 to-orange-400 rounded-2xl p-6 text-white relative overflow-hidden">
          <div className="absolute right-4 top-1/2 -translate-y-1/2 text-7xl opacity-20">🛒</div>
          <h2 className="text-xl font-bold mb-1">大溪商城</h2>
          <p className="text-white/80 text-sm">好物低价，尽在大溪</p>
          <button
            onClick={() => document.getElementById('goods-section')?.scrollIntoView({ behavior: 'smooth' })}
            className="mt-3 bg-white text-taobao-500 text-xs font-medium px-4 py-1.5 rounded-full inline-block"
          >
            去逛逛 →
          </button>
        </div>
      </div>

      {/* Categories */}
      <div className="px-4 -mt-3">
        <div className="card grid grid-cols-4 gap-3 p-4">
          {CATEGORIES.map((cat, i) => (
            <button
              key={i}
              onClick={() => navigate(`/search?q=${cat.name}`)}
              className="flex flex-col items-center gap-1.5 py-1"
            >
              <span className="text-2xl">{cat.icon}</span>
              <span className="text-xs text-gray-600">{cat.name}</span>
            </button>
          ))}
        </div>
      </div>

      {/* Goods Section */}
      <div id="goods-section" className="px-3 mt-4">
        <div className="flex items-center justify-between mb-3">
          <h2 className="text-base font-bold text-gray-800">为你推荐</h2>
          <button className="text-xs text-taobao-500">查看更多 →</button>
        </div>

        {loading ? (
          <div className="grid grid-cols-2 gap-2">
            {Array.from({ length: 6 }).map((_, i) => (
              <div key={i} className="card overflow-hidden animate-pulse">
                <div className="aspect-square bg-gray-200" />
                <div className="p-2.5 space-y-2">
                  <div className="h-4 bg-gray-200 rounded w-3/4" />
                  <div className="h-5 bg-gray-200 rounded w-1/3" />
                </div>
              </div>
            ))}
          </div>
        ) : goodsList.length > 0 ? (
          <div className="grid grid-cols-2 gap-2">
            {goodsList.map(goods => (
              <ProductCard key={goods.spuId} goods={goods} />
            ))}
          </div>
        ) : (
          <div className="text-center py-16 text-gray-400">
            <div className="text-5xl mb-4">📭</div>
            <p className="text-sm">暂无商品</p>
            <p className="text-xs mt-1">请先通过商家后台添加商品</p>
          </div>
        )}
      </div>
    </div>
  )
}

/** 生成演示数据 */
function generateMockData(): GoodsSimpleDTO[] {
  const mockNames = [
    'Apple iPhone 16 Pro Max 256GB 原色钛金属 5G手机',
    '华为 Mate 70 Pro 昆仑玻璃版 12GB+512GB',
    'Sony WH-1000XM5 无线降噪头戴式耳机 铂金银',
    'Apple MacBook Pro 14英寸 M4 Pro芯片 18GB',
    '戴森 V15 Detect 无线吸尘器 激光探测',
    '耐克 Air Jordan 1 Low OG 黑白熊猫 Dunk',
    '优衣库 男装 高级轻型羽绒服 3D剪裁',
    '兰蔻 小黑瓶 肌底精华液 115ml',
    '茅台 飞天 53度 酱香型白酒 500ml',
    '三只松鼠 坚果大礼包 每日坚果 30袋装',
    '戴尔 U2724D 27英寸 2K 高清显示器 IPS',
    'Apple Watch Ultra 3 钛金属 49mm',
  ]
  return mockNames.map((title, i) => ({
    spuId: i + 1,
    shopId: 1,
    shopName: '大溪自营',
    title,
    subTitle: '',
    mainImg: '',
    price: Math.floor(Math.random() * 9999) + 9.9,
    originPrice: Math.floor(Math.random() * 19999) + 99,
    saleCount: Math.floor(Math.random() * 10000),
    isHot: Math.random() > 0.7 ? 1 : 0,
    status: 1,
  }))
}
