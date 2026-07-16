import { useNavigate } from 'react-router-dom'
import type { GoodsSimpleDTO } from '../api/types'

interface ProductCardProps {
  goods: GoodsSimpleDTO
}

export default function ProductCard({ goods }: ProductCardProps) {
  const navigate = useNavigate()

  return (
    <div
      onClick={() => navigate(`/goods/${goods.spuId}`)}
      className="card-hover overflow-hidden cursor-pointer active:scale-[0.98] transition-transform"
    >
      {/* Image */}
      <div className="aspect-square bg-gray-100 relative overflow-hidden">
        {goods.mainImg ? (
          <img
            src={goods.mainImg}
            alt={goods.title}
            className="w-full h-full object-cover"
            loading="lazy"
            onError={(e) => {
              (e.target as HTMLImageElement).style.display = 'none'
              ;(e.target as HTMLImageElement).nextElementSibling?.classList.remove('hidden')
            }}
          />
        ) : null}
        <div className={`w-full h-full flex items-center justify-center text-gray-400 text-sm ${goods.mainImg ? 'hidden' : ''}`}>
          <svg className="w-12 h-12" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
          </svg>
        </div>
        {goods.isHot === 1 && (
          <span className="absolute top-1.5 left-1.5 bg-taobao-500 text-white text-xs px-1.5 py-0.5 rounded">
            热卖
          </span>
        )}
      </div>

      {/* Info */}
      <div className="p-2.5">
        <h3 className="text-sm text-gray-800 line-clamp-2 leading-5 mb-1.5 min-h-[2.5rem]">
          {goods.title}
        </h3>
        <div className="flex items-baseline gap-1">
          <span className="text-taobao-500 font-bold text-base">
            ¥{goods.price.toFixed(2)}
          </span>
          {goods.originPrice && goods.originPrice > goods.price && (
            <span className="text-gray-400 text-xs line-through">
              ¥{goods.originPrice.toFixed(2)}
            </span>
          )}
        </div>
        <div className="flex items-center justify-between mt-1">
          <span className="text-gray-400 text-xs">{goods.shopName}</span>
          <span className="text-gray-400 text-xs">已售{goods.saleCount}</span>
        </div>
      </div>
    </div>
  )
}
