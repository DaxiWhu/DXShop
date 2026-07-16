/**
 * DXShop 全部 API 接口封装 (87个接口)
 * 用户模块: /user, /user-shop, /api/oss, /order-data        → 端口 8082
 * 订单模块: /order, /address, /api/mq/dead-letter           → 端口 8083
 * 商品模块: /goods                                            → 端口 8084
 * 聊天模块: /chat, /cs                                        → 端口 8085
 */
import { apiGet, apiGetBody, apiPost, apiPut, apiDelete } from './request'
import type {
  Result,
  PageVO,
  GoodsSimpleDTO,
  GoodsDetailDTO,
  GoodsSkuDTO,
  GoodsCommentDTO,
  GoodsToChangeDTO,
  GoodsSkuToChangeDTO,
  UserOrderDTO,
  UserSimpleOrderDTO,
  OrderStatusCountDTO,
  PaymentPageDTO,
  PaymentStatusDTO,
  RefundRequestSimpleDTO,
  RefundRequestDetailDTO,
  AddressModifyRequestDTO,
  UserAddressDTO,
  LoginResponseDTO,
  UserDisplayDTO,
  UserPrivateDTO,
  UserCart,
  UserFavoritesShopDTO,
  UserFavoritesSpuDTO,
  UserShopSimpleDTO,
  UserShopDTO,
  UserShopDisplay,
  ShopChangeRequest,
  ChatSessionDTO,
  ChatMessageDTO,
  AgentLoginDTO,
} from './types'

/* ==================== 商品模块 (8084) ==================== */

/** 1. 首页简要商品信息 */
export const getGoodsSimple = (ids: number[]) =>
  apiGet<GoodsSimpleDTO[]>('/api/goods/goods/simple', {}, { params: { ids: ids.join(',') } })

/** 2. 商品详情 */
export const getGoodsDetail = (spuId: number) =>
  apiGet<GoodsDetailDTO>(`/api/goods/goods/detail/${spuId}`)

/** 3. 商家查看商品列表 (GET + body) */
export const getShopSpuPage = (data: any) =>
  apiGetBody<GoodsToChangeDTO[]>('/api/goods/goods/shop/spu', data)

/** 4. 商家修改商品 */
export const updateShopSpu = (spuId: number, data: any) =>
  apiPut(`/api/goods/goods/shop/spu/${spuId}`, data)

/** 5. 商家新增商品 */
export const createShopSpu = (data: any) =>
  apiPost('/api/goods/goods/shop/spu', data)

/** 6. 获取商品规格信息 */
export const getGoodsSku = (spuId: number) =>
  apiGet<GoodsSkuDTO>(`/api/goods/goods/sku/${spuId}`)

/** 7. 商家修改时的SKU展示 */
export const getShopSku = (spuId: number) =>
  apiGet<GoodsSkuToChangeDTO>(`/api/goods/goods/sku/shop/${spuId}`)

/** 8. 新增规格名 */
export const addSpecName = (spuId: number, data: any[]) =>
  apiPost(`/api/goods/goods/sku/sku-spec-name/${spuId}`, data)

/** 9. 增加/更新规格值 */
export const addSpecValue = (spuId: number, data: any) =>
  apiPost(`/api/goods/goods/sku/sku-spec-value/${spuId}`, data)

/** 10. 增加/更新SKU */
export const addSku = (spuId: number, data: any[]) =>
  apiPut(`/api/goods/goods/sku/sku/${spuId}`, data)

/** 11. 内部API: 购物车获取SKU信息 -- 前端通过正常API调用 */
export const getCartSku = (skuId: number, buyNum: number) =>
  apiGet<any>(`/api/goods/goods/sku/api`, {}, { params: { skuId, buyNum } })

/** 12. 获取商品评论 */
export const getGoodsComments = (spuId: number) =>
  apiGet<GoodsCommentDTO>(`/api/goods/goods/comment/${spuId}`)

/** 13. 发送评论 */
export const postGoodsComment = (data: any) =>
  apiPost('/api/goods/goods/comment', data)

/* ==================== 订单模块 (8083) ==================== */

/** 14. 内部API: 确认收货 */
export const confirmReceiveApi = (orderId: number, userId: number) =>
  apiPut<UserOrderDTO>(`/api/order/order/api/ok`, {}, { params: { orderId, userId } })

/** 15. 取消订单 */
export const cancelOrder = (orderId: number) =>
  apiDelete(`/api/order/order/fail/${orderId}`)

/** 16. 下单 */
export const createOrder = (data: any) =>
  apiPost('/api/order/order', data)

/** 17. 用户按状态查订单列表 (GET + body) */
export const getUserOrders = (data: any) =>
  apiGetBody<UserSimpleOrderDTO[]>('/api/order/order/simple', data)

/** 18. 商家按状态查订单列表 (GET + body) */
export const getShopOrders = (data: any) =>
  apiGetBody<UserSimpleOrderDTO[]>('/api/order/order/simple/shop', data)

/** 19. 用户各状态订单数 */
export const getUserOrderStatusCount = () =>
  apiGet<OrderStatusCountDTO>('/api/order/order/category-number/user')

/** 20. 商家各状态订单数 */
export const getShopOrderStatusCount = () =>
  apiGet<OrderStatusCountDTO>('/api/order/order/category-number/shop')

/** 21. 申请更换收货地址 */
export const applyAddressModify = (data: any) =>
  apiPut('/api/order/order/address', data)

/** 22. 申请退款 */
export const applyRefund = (data: any) =>
  apiPost('/api/order/order/refunds', data)

/** 23. 拉取支付页面 */
export const getPaymentPage = (orderId: number) =>
  apiPost<PaymentPageDTO>(`/api/order/order/payments/page`, {}, { params: { orderId } })

/** 24. 支付成功回调 */
export const paymentSuccess = (data: any) =>
  apiPost('/api/order/order/payments/success', data)

/** 25. 确认支付状态 */
export const checkPaymentStatus = (orderId: number) =>
  apiPut<PaymentStatusDTO>(`/api/order/order/payments`, {}, { params: { orderId } })

/** 26. 用户查看地址修改申请 (GET + body) */
export const getUserAddressRequests = (data: any) =>
  apiGetBody<AddressModifyRequestDTO[]>('/api/order/order/address/requests/user', data)

/** 27. 用户查看退款申请 (GET + body) */
export const getUserRefundRequests = (data: any) =>
  apiGetBody<RefundRequestSimpleDTO[]>('/api/order/order/refund/requests/user', data)

/** 28. 商家查看地址修改申请 (GET + body) */
export const getShopAddressRequests = (data: any) =>
  apiGetBody<AddressModifyRequestDTO[]>('/api/order/order/address/requests/shop', data)

/** 29. 商家查看退款申请 (GET + body) */
export const getShopRefundRequests = (data: any) =>
  apiGetBody<RefundRequestSimpleDTO[]>('/api/order/order/refund/requests/shop', data)

/** 30. 获取订单详情 */
export const getOrderDetail = (orderId: number) =>
  apiGet<UserOrderDTO>(`/api/order/order/detail/${orderId}`)

/** 31. 查看退款详情 */
export const getRefundDetail = (orderId: number) =>
  apiGet<RefundRequestDetailDTO>(`/api/order/order/refund/requests/${orderId}`)

/** 32. 商家审核地址修改 */
export const auditAddressModify = (data: any) =>
  apiPut('/api/order/order/address/audit', data)

/** 33. 商家审核退款 */
export const auditRefund = (data: any) =>
  apiPut('/api/order/order/refund/audit', data)

/** 34. 用户退货发货 */
export const userReturnShip = (data: any) =>
  apiPut('/api/order/order/refund/user-ship', data)

/** 35. 获取评论所需信息 */
export const getCommentInfo = (params: { userId: number; orderId: number; spuId: number; skuId: number }) =>
  apiGet<any>('/api/order/order/comment', {}, { params })

/** 36. 新增/修改地址 */
export const saveAddress = (data: any) =>
  apiPut('/api/order/address', data)

/** 37. 删除地址 */
export const deleteAddress = (addressId: number) =>
  apiDelete(`/api/order/address/${addressId}`)

/** 38. 查看地址列表 */
export const getAddressList = () =>
  apiGet<UserAddressDTO[]>('/api/order/address')

/** 39. 分页查询死信消息 */
export const getDeadLetterPage = (params: any) =>
  apiGet<PageVO<any>>('/api/mq/dead-letter/page', params)

/** 40. 未处理死信数量 */
export const getDeadLetterCount = () =>
  apiGet<number>('/api/mq/dead-letter/unprocessed-count')

/** 41. 死信消息详情 */
export const getDeadLetterDetail = (id: number) =>
  apiGet<any>(`/api/mq/dead-letter/${id}`)

/** 42. 标记死信已处理 */
export const handleDeadLetter = (id: number, handler: string, remark?: string) =>
  apiPost(`/api/mq/dead-letter/handle/${id}`, {}, { params: { handler, remark } })

/** 43. 忽略死信 */
export const ignoreDeadLetter = (id: number, handler: string, remark?: string) =>
  apiPost(`/api/mq/dead-letter/ignore/${id}`, {}, { params: { handler, remark } })

/** 44. 批量删除死信 */
export const batchDeleteDeadLetter = (ids: number[]) =>
  apiDelete('/api/mq/dead-letter/batch-delete', ids)

/* ==================== 用户模块 (8082) ==================== */

/** 45. 获取验证码 */
export const getVerifyCode = (phone: string) =>
  apiGet<string>(`/api/user/user/verify-code`, {}, { params: { phone } })

/** 46. 验证码注册 */
export const register = (data: { phone: string; verifyCode: string }) =>
  apiPost('/api/user/user/register', data)

/** 47. 手机号登录 */
export const login = (data: { phone: string; verifyCode: string }) =>
  apiPost<LoginResponseDTO>('/api/user/user/login', data)

/** 48. 关注/取消关注店铺 */
export const toggleFollowShop = (shopId: number) =>
  apiPut(`/api/user/user/favorites/shop/${shopId}`)

/** 49. 关注/取消关注商品 */
export const toggleFollowSpu = (spuId: number) =>
  apiPut(`/api/user/user/favorites/spu/${spuId}`)

/** 50. 加入购物车 */
export const addToCart = (data: { skuId: number; spuId: number; buyNum: number }) =>
  apiPut('/api/user/user/cart', data)

/** 51. 购物车勾选 */
export const checkCartItems = (cartIds: number[]) =>
  apiPut('/api/user/user/cart/checked', cartIds)

/** 52. 购物车删除 */
export const deleteCartItems = (cartIds: number[]) =>
  apiDelete('/api/user/user/cart', cartIds)

/** 53. 确认收货 */
export const confirmReceive = (orderId: number) =>
  apiPost(`/api/user/user/order/ok/${orderId}`)

/** 54. 查看收藏店铺 */
export const getFavoritesShop = () =>
  apiGet<UserFavoritesShopDTO[]>('/api/user/user/favorites/shop')

/** 55. 检查是否关注店铺 */
export const checkFollowShop = (shopId: number) =>
  apiGet<number>(`/api/user/user/favorites/shop/${shopId}`)

/** 56. 检查是否关注商品 */
export const checkFollowSpu = (spuId: number) =>
  apiGet<number>(`/api/user/user/favorites/spu/${spuId}`)

/** 57. 查看收藏商品 */
export const getFavoritesSpu = () =>
  apiGet<UserFavoritesSpuDTO[]>('/api/user/user/favorites/spu')

/** 58. 查看购物车 */
export const getCart = () =>
  apiGet<UserCart[]>('/api/user/user/cart')

/** 59. 内部API: 获取评论信息 */
export const getUserCommentInfo = (params: any) =>
  apiGet<any>('/api/user/user/comment/information', {}, { params })

/** 60. 修改展示信息 */
export const updateUserDisplay = (data: any) =>
  apiPut('/api/user/user/display', data)

/** 61. 修改隐私信息 */
export const updateUserPrivate = (data: any) =>
  apiPut('/api/user/user/private', data)

/** 62. 获取展示信息 */
export const getUserDisplay = (userId: number) =>
  apiGet<UserDisplayDTO>(`/api/user/user/display/${userId}`)

/** 63. 获取隐私信息 */
export const getUserPrivate = () =>
  apiGet<UserPrivateDTO>('/api/user/user/private')

/** 64. 查看我的店铺 */
export const getMyShops = () =>
  apiGet<UserShopSimpleDTO[]>('/api/user/user-shop/my')

/** 65. 登录店铺 */
export const loginShop = (shopId: number) =>
  apiPost<LoginResponseDTO>(`/api/user/user-shop/login/${shopId}`)

/** 66. 获取店铺展示 */
export const getShopDisplay = (shopId: number) =>
  apiGet<UserShopDisplay>(`/api/user/user-shop/show/${shopId}`)

/** 67. 创建店铺 */
export const createShop = (data: any) =>
  apiPost('/api/user/user-shop', data)

/** 68. 审核创建店铺 */
export const auditCreateShop = (shopId: number, result: number) =>
  apiPut(`/api/user/user-shop/audit/${shopId}/${result}`)

/** 69. 查看创建店铺审核 (GET + body) */
export const getShopCreateRequests = (data: any) =>
  apiGetBody<UserShopDTO[]>('/api/user/user-shop/audit', data)

/** 70. 店铺主查看修改申请 */
export const getShopChangeRequests = () =>
  apiGet<ShopChangeRequest[]>('/api/user/user-shop/request')

/** 71. 审核员查看修改申请 (GET + body) */
export const getShopChangeRequestsAudit = (data: any) =>
  apiGetBody<ShopChangeRequest[]>('/api/user/user-shop/request/audit', data)

/** 72. 审核店铺修改 */
export const auditShopChange = (id: number, result: number) =>
  apiPut(`/api/user/user-shop/request/audit/${id}/${result}`)

/** 73. 修改店铺信息 */
export const updateShop = (data: any) =>
  apiPut<string>('/api/user/user-shop', data)

/** 74. 商家获取完整店铺信息 */
export const getFullShopInfo = () =>
  apiGet<UserShopDTO>('/api/user/user-shop/all')

/** 75. 获取OSS上传凭证 */
export const getOssPolicy = (key: string) =>
  apiPost<any>('/api/oss/upload', {}, { params: { key } })

/* ==================== 聊天模块 (8085) ==================== */

/** 77. 客服登录 */
export const agentLogin = (data: { username: string; password: string }) =>
  apiPost<AgentLoginDTO>('/api/cs/agent/login', data)

/** 78. 获取待处理会话 */
export const getPendingSessions = (data: any) =>
  apiGet<ChatSessionDTO[]>('/api/cs/session/pending', data)

/** 79. 获取客服会话列表 (GET + body) */
export const getAgentSessions = (data: any) =>
  apiGetBody<ChatSessionDTO[]>('/api/cs/session', data)

/** 80. 客服接管会话 */
export const takeSession = (sessionId: number) =>
  apiPut(`/api/cs/session/${sessionId}/take`)

/** 81. 客服发送消息 */
export const csSendMessage = (sessionId: number, data: { content: string }) =>
  apiPost<ChatMessageDTO>(`/api/cs/session/${sessionId}/message`, data)

/** 82. 客服关闭会话 */
export const csCloseSession = (sessionId: number) =>
  apiPut(`/api/cs/session/${sessionId}/close`)

/** 83. 用户创建会话 */
export const createChatSession = (spuId: number) =>
  apiPost<ChatSessionDTO>('/api/chat/session', {}, { params: { spuId } })

/** 84. 用户获取会话列表 */
export const getUserChatSessions = () =>
  apiGet<ChatSessionDTO[]>('/api/chat/session')

/** 85. 用户获取会话详情 */
export const getUserSessionDetail = (sessionId: number) =>
  apiGet<ChatSessionDTO>(`/api/chat/session/${sessionId}/user`)

/** 86. 用户发送消息 */
export const userSendMessage = (sessionId: number, data: { content: string }) =>
  apiPost<ChatMessageDTO>(`/api/chat/session/${sessionId}/message/user`, data)

/** 87. 用户关闭会话 */
export const userCloseSession = (sessionId: number) =>
  apiPut(`/api/chat/session/${sessionId}/close`)
