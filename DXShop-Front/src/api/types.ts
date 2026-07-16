/* ==================== 通用类型 ==================== */
export interface Result<T> {
  code: number
  msg: string
  data: T
}

export interface PageVO<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/* ==================== 商品模块 ==================== */
export interface GoodsSimpleDTO {
  spuId: number
  shopId: number
  shopName: string
  title: string
  subTitle: string
  mainImg: string
  price: number
  originPrice?: number
  saleCount: number
  isHot: number
  status: number
}

export interface GoodsDetailDTO {
  spuId: number
  shopId: number
  shopName: string
  categoryId: number
  brand: string
  title: string
  subTitle: string
  mainImg: string
  images: GoodsImageDTO[]
  price: number
  originPrice: number
  status: number
  saleCount: number
  collectCount: number
  clickCount: number
  commentCount: number
  tags: GoodsTagDTO[]
  params: GoodsParamDTO[]
  customParams: GoodsCustomParamDTO[]
  detail: GoodsDetailItemDTO
  hotGoods: GoodsSimpleDTO[]
  commentStat: GoodsCommentStatDTO | null
}

export interface GoodsImageDTO {
  imgUrl: string
  sort: number
  isMain: number
}

export interface GoodsDetailItemDTO {
  content: string
}

export interface GoodsTagDTO {
  tagId: number
  tagName: string
}

export interface GoodsParamDTO {
  paramName: string
  paramValue: string
}

export interface GoodsCustomParamDTO {
  paramName: string
  paramValue: string
}

export interface GoodsCommentStatDTO {
  goodCount: number
  midCount: number
  badCount: number
  totalCount: number
  goodRate: number
}

export interface GoodsSkuDTO {
  spuId: number
  specNames: SpecNameDTO[]
  specValues: SpecValueDTO[]
  skus: SkuItemDTO[]
}

export interface SpecNameDTO {
  specNameId: number
  specName: string
}

export interface SpecValueDTO {
  specValueId: number
  specNameId: number
  specValue: string
}

export interface SkuItemDTO {
  skuId: number
  price: number
  stock: number
  skuSpec: string
  specValueIds: number[]
  status: number
}

export interface GoodsCommentDTO {
  comments: CommentItemDTO[]
  tags: CommentTagCountDTO[]
  goodRate: number
}

export interface CommentItemDTO {
  commentId: number
  userId: number
  nickname: string
  avatarUrl: string
  content: string
  tags: string[]
  createTime: string
  skuSpec: string
}

export interface CommentTagCountDTO {
  tagId: number
  tagName: string
  count: number
}

export interface GoodsToChangeDTO {
  spuId: number
  title: string
  mainImg: string
  price: number
  status: number
  saleCount: number
}

export interface GoodsSkuToChangeDTO {
  specNames: SpecNameDTO[]
  specValues: SpecValueDTO[]
  skus: SkuItemDTO[]
}

/* ==================== 订单模块 ==================== */
export interface UserOrderDTO {
  orderId: number
  orderSn: string
  userId: number
  shopId: number
  shopName: string
  price: number
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  orderStatus: number
  payStatus: number
  logisticsCompany: string
  logisticsNo: string
  items: UserOrderItemDTO[]
  createTime: string
  payTime: string
}

export interface UserOrderItemDTO {
  orderItemId: number
  spuId: number
  skuId: number
  goodsName: string
  goodsImg: string
  skuSpec: string
  perPrice: number
  buyNum: number
  isComment: number
}

export interface UserSimpleOrderDTO {
  orderId: number
  orderSn: string
  shopName: string
  price: number
  orderStatus: number
  payStatus: number
  items: UserOrderItemDTO[]
  createTime: string
}

export interface OrderStatusCountDTO {
  unpaidCount: number
  unshippedCount: number
  shippedCount: number
  completedCount: number
  refundCount: number
}

export interface PaymentPageDTO {
  orderId: number
  orderSn: string
  price: number
  items: UserOrderItemDTO[]
  expireTime: string
}

export interface PaymentStatusDTO {
  payStatus: number
  orderStatus: number
}

export interface RefundRequestSimpleDTO {
  refundId: number
  orderId: number
  orderSn: string
  refundAmount: number
  reason: string
  status: number
  createTime: string
}

export interface RefundRequestDetailDTO {
  refundId: number
  orderId: number
  orderSn: string
  refundAmount: number
  reason: string
  status: number
  logisticsCompany: string
  logisticsNo: string
  createTime: string
}

export interface AddressModifyRequestDTO {
  id: number
  orderId: number
  orderSn: string
  newAddress: string
  status: number
  createTime: string
}

export interface UserAddressDTO {
  addressId: number
  receiverName: string
  receiverPhone: string
  province: string
  city: string
  district: string
  detailAddress: string
  fullAddress: string
  isDefault: number
}

/* ==================== 用户模块 ==================== */
export interface LoginResponseDTO {
  token: string
  userId: number
  nickname: string
  avatarUrl: string
}

export interface UserDisplayDTO {
  userId: number
  nickname: string
  gender: number
  avatarUrl: string
  accountStatus: number
  isRealName: number
}

export interface UserPrivateDTO {
  phone: string
  email: string
  birthday: string
  realName: string
  idCard: string
}

export interface UserCart {
  cartId: number
  skuId: number
  spuId: number
  buyNum: number
  price: number
  title: string
  mainImg: string
  skuSpec: string
  checked: number
  stock?: number
}

export interface UserFavoritesShopDTO {
  shopId: number
  shopName: string
  logoUrl: string
}

export interface UserFavoritesSpuDTO {
  spuId: number
  title: string
  mainImg: string
  price: number
}

/* ==================== 店铺模块 ==================== */
export interface UserShopSimpleDTO {
  shopId: number
  shopName: string
  logoUrl: string
  shopStatus: number
}

export interface UserShopDTO {
  shopId: number
  shopName: string
  logoUrl: string
  shopDesc: string
  businessHours: string
  contactPhone: string
  contactEmail: string
  address: string
  shopStatus: number
}

export interface UserShopDisplay {
  shopId: number
  shopName: string
  logoUrl: string
  shopDesc: string
  shopStatus: number
}

export interface ShopChangeRequest {
  id: number
  shopId: number
  shopName: string
  fieldName: string
  oldValue: string
  newValue: string
  status: number
  createTime: string
}

/* ==================== 聊天模块 ==================== */
export interface ChatSessionDTO {
  id: number
  userId: number
  spuId: number
  status: number
  csAgentId: number | null
  lastMessage: string
  lastMessageTime: string
  spuTitle?: string
  spuMainImg?: string
}

export interface ChatMessageDTO {
  id: number
  sessionId: number
  senderType: string
  senderId: number
  content: string
  createTime: string
}

export interface AgentLoginDTO {
  token: string
  agentId: number
  nickname: string
}
