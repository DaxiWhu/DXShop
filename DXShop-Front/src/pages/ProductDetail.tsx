import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { getGoodsDetail, getGoodsSku, getGoodsComments, addToCart, checkFollowSpu, toggleFollowSpu, createChatSession } from '../api'
import { useAuthStore } from '../store/auth'
import { useToast } from '../components/Toast'
import type { GoodsDetailDTO, GoodsSkuDTO, SkuItemDTO, GoodsCommentDTO } from '../api/types'

export default function ProductDetail() {
  const { id } = useParams<{ id: string }>()
  const spuId = Number(id)
  const navigate = useNavigate()
  const { isLoggedIn } = useAuthStore()
  const { toast } = useToast()

  const [detail, setDetail] = useState<GoodsDetailDTO | null>(null)
  const [skuData, setSkuData] = useState<GoodsSkuDTO | null>(null)
  const [comments, setComments] = useState<GoodsCommentDTO | null>(null)
  const [loading, setLoading] = useState(true)
  const [currentImg, setCurrentImg] = useState(0)
  const [buyNum, setBuyNum] = useState(1)
  const [selectedSku, setSelectedSku] = useState<SkuItemDTO | null>(null)
  const [selectedSpecs, setSelectedSpecs] = useState<Record<number, number>>({})
  const [showSkuPanel, setShowSkuPanel] = useState(false)
  const [isFollowed, setIsFollowed] = useState(false)
  const [activeTab, setActiveTab] = useState<'detail' | 'params' | 'comments'>('detail')

  useEffect(() => {
    loadDetail()
    if (isLoggedIn) {
      checkFollowSpu(spuId).then(res => setIsFollowed(res === 1)).catch(() => {})
    }
  }, [spuId])

  const loadDetail = async () => {
    try {
      setLoading(true)
      const [d, s, c] = await Promise.all([
        getGoodsDetail(spuId),
        isLoggedIn ? getGoodsSku(spuId) : Promise.resolve(null),
        getGoodsComments(spuId),
      ])
      setDetail(d)
      setSkuData(s)
      setComments(c)
    } catch {
      toast('加载商品信息失败', 'error')
    } finally {
      setLoading(false)
    }
  }

  const handleSpecSelect = (specNameId: number, specValueId: number) => {
    const newSpecs = { ...selectedSpecs, [specNameId]: specValueId }
    setSelectedSpecs(newSpecs)
    // 匹配 SKU
    if (skuData) {
      const specValueIds = Object.values(newSpecs)
      const matched = skuData.skus.find(s =>
        s.specValueIds.length === specValueIds.length &&
        s.specValueIds.every(id => specValueIds.includes(id))
      )
      setSelectedSku(matched || null)
    }
  }

  const handleBuy = async (isCart: boolean) => {
    if (!isLoggedIn) { navigate('/login'); return }
    if (skuData && !selectedSku) {
      toast('请选择规格', 'error')
      setShowSkuPanel(true)
      return
    }
    try {
      await addToCart({
        skuId: selectedSku?.skuId || 0,
        spuId,
        buyNum,
      })
      toast(isCart ? '已加入购物车' : '准备下单', 'success')
      setShowSkuPanel(false)
      if (!isCart) navigate('/cart')
    } catch {
      toast('操作失败', 'error')
    }
  }

  const handleFollow = async () => {
    if (!isLoggedIn) { navigate('/login'); return }
    try {
      await toggleFollowSpu(spuId)
      setIsFollowed(!isFollowed)
      toast(isFollowed ? '已取消收藏' : '已收藏', 'success')
    } catch {
      toast('操作失败', 'error')
    }
  }

  const handleChat = async () => {
    if (!isLoggedIn) { navigate('/login'); return }
    try {
      await createChatSession(spuId)
      navigate('/chat')
    } catch {
      toast('创建会话失败', 'error')
    }
  }

  if (loading) {
    return (
      <div className="bg-white min-h-screen animate-pulse">
        <div className="aspect-square bg-gray-200" />
        <div className="p-4 space-y-3">
          <div className="h-5 bg-gray-200 rounded w-3/4" />
          <div className="h-8 bg-gray-200 rounded w-1/3" />
        </div>
      </div>
    )
  }

  if (!detail) {
    return (
      <div className="text-center py-20 text-gray-400">
        <div className="text-5xl mb-4">😕</div>
        <p>商品不存在或已下架</p>
        <button onClick={() => navigate('/')} className="btn-taobao mt-4">返回首页</button>
      </div>
    )
  }

  return (
    <div className="bg-white min-h-screen pb-24">
      {/* Image Carousel */}
      <div className="relative aspect-square bg-gray-100">
        {detail.images?.length > 0 ? (
          <>
            <img
              src={detail.images[currentImg]?.imgUrl || detail.mainImg}
              alt={detail.title}
              className="w-full h-full object-cover"
              onError={(e) => { (e.target as HTMLImageElement).style.display = 'none' }}
            />
            <div className="absolute bottom-3 right-3 bg-black/50 text-white text-xs px-2 py-0.5 rounded-full">
              {currentImg + 1}/{detail.images.length}
            </div>
            <div className="absolute bottom-3 left-3 flex gap-1.5">
              {detail.images.map((_, i) => (
                <button
                  key={i}
                  onClick={() => setCurrentImg(i)}
                  className={`w-1.5 h-1.5 rounded-full ${i === currentImg ? 'bg-white' : 'bg-white/40'}`}
                />
              ))}
            </div>
          </>
        ) : (
          <div className="w-full h-full flex items-center justify-center text-gray-400">
            <svg className="w-20 h-20" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
            </svg>
          </div>
        )}
        <button onClick={() => navigate(-1)} className="absolute top-3 left-3 w-8 h-8 bg-black/30 rounded-full flex items-center justify-center">
          <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
        </button>
      </div>

      {/* Price & Title */}
      <div className="p-4 border-b border-gray-100">
        <div className="flex items-baseline gap-1 mb-2">
          <span className="text-taobao-500 text-2xl font-bold">¥{detail.price.toFixed(2)}</span>
          {detail.originPrice > detail.price && (
            <span className="text-gray-400 text-sm line-through">¥{detail.originPrice.toFixed(2)}</span>
          )}
        </div>
        <h1 className="text-base font-medium text-gray-800 leading-6">{detail.title}</h1>
        <p className="text-xs text-gray-400 mt-1">
          月销{detail.saleCount} | {detail.shopName}
        </p>
        <div className="flex gap-2 mt-3">
          {detail.tags?.map(tag => (
            <span key={tag.tagId} className="text-xs px-2 py-0.5 bg-taobao-50 text-taobao-500 rounded">
              {tag.tagName}
            </span>
          ))}
        </div>
      </div>

      {/* SKU Selector trigger */}
      {skuData && skuData.skus.length > 0 && (
        <button
          onClick={() => setShowSkuPanel(true)}
          className="w-full p-4 border-b border-gray-100 flex items-center justify-between hover:bg-gray-50"
        >
          <span className="text-sm text-gray-500">规格</span>
          <span className="text-sm text-gray-800">
            {selectedSku ? selectedSku.skuSpec : '请选择规格'}
          </span>
          <svg className="w-4 h-4 text-gray-400 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
          </svg>
        </button>
      )}

      {/* Tabs */}
      <div className="sticky top-12 bg-white border-b border-gray-100 z-10">
        <div className="flex">
          {(['detail', 'params', 'comments'] as const).map(tab => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`flex-1 py-3 text-sm font-medium border-b-2 transition-colors ${
                activeTab === tab
                  ? 'text-taobao-500 border-taobao-500'
                  : 'text-gray-500 border-transparent'
              }`}
            >
              {tab === 'detail' ? '详情' : tab === 'params' ? '参数' : `评价(${comments?.comments?.length || detail.commentCount})`}
            </button>
          ))}
        </div>
      </div>

      {/* Tab Content */}
      <div className="p-4">
        {activeTab === 'detail' && (
          <div className="text-sm text-gray-700 leading-relaxed">
            {detail.detail?.content ? (
              <div dangerouslySetInnerHTML={{ __html: detail.detail.content }} />
            ) : (
              <div className="text-center py-8 text-gray-400">
                <p>暂无详情介绍</p>
              </div>
            )}
          </div>
        )}

        {activeTab === 'params' && (
          <div className="space-y-2">
            {detail.params?.length > 0 ? (
              detail.params.map((p, i) => (
                <div key={i} className="flex text-sm py-2 border-b border-gray-50">
                  <span className="text-gray-400 w-24 shrink-0">{p.paramName}</span>
                  <span className="text-gray-700">{p.paramValue}</span>
                </div>
              ))
            ) : (
              <p className="text-center py-8 text-gray-400">暂无参数信息</p>
            )}
            {detail.customParams?.length > 0 && detail.customParams.map((p, i) => (
              <div key={i} className="flex text-sm py-2 border-b border-gray-50">
                <span className="text-gray-400 w-24 shrink-0">{p.paramName}</span>
                <span className="text-gray-700">{p.paramValue}</span>
              </div>
            ))}
          </div>
        )}

        {activeTab === 'comments' && (
          <div>
            {comments?.comments?.length ? (
              <div className="space-y-4">
                {comments.comments.map(c => (
                  <div key={c.commentId} className="border-b border-gray-50 pb-3">
                    <div className="flex items-center gap-2 mb-1.5">
                      <div className="w-7 h-7 rounded-full bg-gray-200 flex items-center justify-center text-xs text-gray-500 overflow-hidden">
                        {c.avatarUrl ? <img src={c.avatarUrl} alt="" className="w-full h-full object-cover"/> : c.nickname?.[0]}
                      </div>
                      <span className="text-sm text-gray-700">{c.nickname}</span>
                      <span className="text-xs text-gray-400">{c.skuSpec}</span>
                    </div>
                    <p className="text-sm text-gray-700 ml-9">{c.content}</p>
                    {c.tags?.length > 0 && (
                      <div className="flex gap-1 ml-9 mt-1.5">
                        {c.tags.map((t, i) => (
                          <span key={i} className="text-xs px-1.5 py-0.5 bg-taobao-50 text-taobao-500 rounded">{t}</span>
                        ))}
                      </div>
                    )}
                    <span className="text-xs text-gray-400 ml-9 mt-1 block">{c.createTime}</span>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-center py-8 text-gray-400">暂无评价</p>
            )}
          </div>
        )}
      </div>

      {/* Bottom Bar */}
      <div className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-100 z-50">
        <div className="max-w-6xl mx-auto flex items-center h-14 px-3 gap-2">
          <button onClick={handleChat} className="flex flex-col items-center text-gray-500 text-xs px-2">
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
            </svg>
            <span>客服</span>
          </button>
          <button
            onClick={handleFollow}
            className={`flex flex-col items-center text-xs px-2 ${isFollowed ? 'text-taobao-500' : 'text-gray-500'}`}
          >
            <svg className="w-5 h-5" fill={isFollowed ? 'currentColor' : 'none'} stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
            </svg>
            <span>{isFollowed ? '已收藏' : '收藏'}</span>
          </button>
          <button onClick={() => handleBuy(true)} className="btn-taobao-outline flex-1 h-10 text-sm">
            加入购物车
          </button>
          <button onClick={() => handleBuy(false)} className="btn-taobao flex-1 h-10 text-sm">
            立即购买
          </button>
        </div>
      </div>

      {/* SKU Panel Modal */}
      {showSkuPanel && skuData && (
        <div className="fixed inset-0 z-[100] flex flex-col justify-end">
          <div className="absolute inset-0 bg-black/50" onClick={() => setShowSkuPanel(false)} />
          <div className="relative bg-white rounded-t-2xl max-h-[70vh] overflow-y-auto animate-slide-up">
            <div className="sticky top-0 bg-white p-4 border-b border-gray-100 flex items-center justify-between rounded-t-2xl">
              <h2 className="font-bold text-base">选择规格</h2>
              <button onClick={() => setShowSkuPanel(false)} className="text-gray-400">
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
            <div className="p-4 space-y-4">
              {/* 商品信息 */}
              <div className="flex gap-3">
                <div className="w-20 h-20 rounded-lg bg-gray-100 overflow-hidden shrink-0">
                  <img src={detail?.mainImg} alt="" className="w-full h-full object-cover" />
                </div>
                <div>
                  <p className="text-taobao-500 text-lg font-bold">¥{selectedSku?.price || detail?.price}</p>
                  <p className="text-xs text-gray-400 mt-1">
                    库存: {selectedSku?.stock ?? '请选择'}
                  </p>
                </div>
              </div>

              {/* 规格选择 */}
              {skuData.specNames.map(sn => {
                const values = skuData.specValues.filter(sv => sv.specNameId === sn.specNameId)
                return (
                  <div key={sn.specNameId}>
                    <p className="text-sm text-gray-500 mb-2">{sn.specName}</p>
                    <div className="flex flex-wrap gap-2">
                      {values.map(sv => {
                        const isSelected = selectedSpecs[sn.specNameId] === sv.specValueId
                        return (
                          <button
                            key={sv.specValueId}
                            onClick={() => handleSpecSelect(sn.specNameId, sv.specValueId)}
                            className={`text-sm px-4 py-1.5 rounded-full border transition-colors ${
                              isSelected
                                ? 'border-taobao-500 bg-taobao-50 text-taobao-500'
                                : 'border-gray-200 text-gray-600'
                            }`}
                          >
                            {sv.specValue}
                          </button>
                        )
                      })}
                    </div>
                  </div>
                )
              })}

              {/* 数量 */}
              <div>
                <p className="text-sm text-gray-500 mb-2">数量</p>
                <div className="flex items-center gap-0">
                  <button
                    onClick={() => setBuyNum(Math.max(1, buyNum - 1))}
                    className="w-8 h-8 rounded-l-full border border-gray-200 flex items-center justify-center text-gray-500"
                  >-</button>
                  <input
                    type="number"
                    value={buyNum}
                    onChange={e => setBuyNum(Math.max(1, parseInt(e.target.value) || 1))}
                    className="w-14 h-8 border-t border-b border-gray-200 text-center text-sm outline-none"
                  />
                  <button
                    onClick={() => setBuyNum(buyNum + 1)}
                    className="w-8 h-8 rounded-r-full border border-gray-200 flex items-center justify-center text-gray-500"
                  >+</button>
                </div>
              </div>

              <button onClick={() => handleBuy(true)} className="btn-taobao w-full h-11 text-sm">
                确定
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
