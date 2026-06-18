package com.daxi.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.daxi.Exception.BusinessException;
import com.daxi.converter.OrderAddressModifyToDto;
import com.daxi.converter.OrderRefundToRefundDetailDto;
import com.daxi.converter.RefundToDto;
import com.daxi.domain.ao.AddressModifyAuditAO;
import com.daxi.domain.ao.OrderRefundAO;
import com.daxi.domain.ao.PaymentAO;
import com.daxi.domain.ao.RefundAuditAO;
import com.daxi.domain.ao.UserOrderAO;
import com.daxi.domain.ao.UserOrderAddressAO;
import com.daxi.domain.dto.AddressModifyRequestDTO;
import com.daxi.domain.dto.OrderStatusCountDTO;
import com.daxi.domain.dto.PaymentStatusDTO;
import com.daxi.domain.dto.RefundDTO;
import com.daxi.domain.dto.RefundRequestDetailDTO;
import com.daxi.domain.dto.RefundRequestSimpleDTO;
import com.daxi.domain.dto.SendCommentDTO;
import com.daxi.domain.dto.UserOrderDTO;
import com.daxi.domain.dto.UserSimpleOrderDTO;
import com.daxi.domain.entity.GoodsSku;
import com.daxi.domain.entity.GoodsSpu;
import com.daxi.domain.entity.OrderAddressModify;
import com.daxi.domain.entity.OrderRefund;
import com.daxi.domain.entity.UserOrder;
import com.daxi.domain.entity.UserOrderItem;
import com.daxi.feign.UserFeignClient;
import com.daxi.key.redis.GoodsKey;
import com.daxi.limit.OrderLimit;
import com.daxi.mapper.order.GoodsSkuMapper;
import com.daxi.mapper.order.GoodsSpuMapper;
import com.daxi.mapper.order.OrderAddressModifyMapper;
import com.daxi.mapper.order.OrderRefundMapper;
import com.daxi.mapper.order.UserOrderItemMapper;
import com.daxi.mapper.order.UserOrderMapper;
import com.daxi.mq.producer.AddressModifyTimeoutProducer;
import com.daxi.mq.producer.OrderTimeoutProducer;
import com.daxi.mq.producer.RefundTimeOutProducer;
import com.daxi.service.IOrderService;
import com.daxi.util.PayUtil;
import com.daxi.util.PriceUtil;
import com.daxi.util.RedisUtil;
import com.daxi.util.UserUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.daxi.domain.ao.AddressModifyAuditAO.AUDIT_RESULT_AGREE;
import static com.daxi.domain.ao.AddressModifyAuditAO.AUDIT_RESULT_REJECT;
import static com.daxi.key.redis.OrderKey.GOODS_STOCK;
import static com.daxi.key.redis.OrderKey.ORDER_CHANGE_ADDRESS_DETAIL;
import static com.daxi.key.redis.OrderKey.ORDER_CHANGE_ADDRESS_SHOP_REQUEST;
import static com.daxi.key.redis.OrderKey.ORDER_CHANGE_ADDRESS_USER_REQUEST;
import static com.daxi.key.redis.OrderKey.ORDER_DETAIL;
import static com.daxi.key.redis.OrderKey.ORDER_PAY_LOCK;
import static com.daxi.key.redis.OrderKey.ORDER_SHOP_STATUS;
import static com.daxi.key.redis.OrderKey.ORDER_SUBMIT_LOCK;
import static com.daxi.key.redis.OrderKey.ORDER_USER_ALL;
import static com.daxi.key.redis.OrderKey.ORDER_USER_STATUS;
import static com.daxi.key.redis.OrderKey.REFUND_DETAIL;
import static com.daxi.key.redis.OrderKey.REFUND_SHOP_LIST;
import static com.daxi.key.redis.OrderKey.REFUND_USER_LIST;
import static com.daxi.limit.GoodsLimit.SKU_ON_SALE;
import static com.daxi.limit.GoodsLimit.SPU_NOT_ON_SALE;
import static com.daxi.limit.OrderLimit.ADDRESS_STATUS_CHANGE;
import static com.daxi.limit.OrderLimit.MERCHANT_REJECT;
import static com.daxi.limit.OrderLimit.ONLY_REFUND;
import static com.daxi.limit.OrderLimit.OPERATE_STATUS_APPLY_ADDRESS_CHANGE;
import static com.daxi.limit.OrderLimit.OPERATE_STATUS_NONE;
import static com.daxi.limit.OrderLimit.OPERATE_STATUS_REFUND_AND_RETURN;
import static com.daxi.limit.OrderLimit.ORDERID_EXPIRE_SECONDS;
import static com.daxi.limit.OrderLimit.ORDER_ADDRESS_CHANGE;
import static com.daxi.limit.OrderLimit.ORDER_ADDRESS_MODIFY_STATUS_CANCEL;
import static com.daxi.limit.OrderLimit.ORDER_ADDRESS_MODIFY_STATUS_EXPIRE;
import static com.daxi.limit.OrderLimit.ORDER_ADDRESS_MODIFY_STATUS_REJECT;
import static com.daxi.limit.OrderLimit.ORDER_ADDRESS_MODIFY_STATUS_SUCCESS;
import static com.daxi.limit.OrderLimit.ORDER_ADDRESS_MODIFY_STATUS_WAIT;
import static com.daxi.limit.OrderLimit.ORDER_CHANGE_ADDRESS_REQUEST_HOURS;
import static com.daxi.limit.OrderLimit.ORDER_CHANGE_ADDRESS_REQUEST_SECONDS;
import static com.daxi.limit.OrderLimit.ORDER_REFUND_REQUEST_HOURS;
import static com.daxi.limit.OrderLimit.ORDER_STATUS_CANCELLED;
import static com.daxi.limit.OrderLimit.ORDER_STATUS_COMPLETED;
import static com.daxi.limit.OrderLimit.ORDER_STATUS_PENDING_PAYMENT;
import static com.daxi.limit.OrderLimit.ORDER_STATUS_PENDING_RECEIPT;
import static com.daxi.limit.OrderLimit.ORDER_STATUS_PENDING_REVIEW;
import static com.daxi.limit.OrderLimit.ORDER_STATUS_PENDING_SHIPMENT;
import static com.daxi.limit.OrderLimit.ORDER_STATUS_REFUND_AFTER_SALES;
import static com.daxi.limit.OrderLimit.ORDER_SUBMIT_LOCK_EXPIRE_SECONDS;
import static com.daxi.limit.OrderLimit.ORDER_SUBMIT_LOCK_ON;
import static com.daxi.limit.OrderLimit.PAYMENT_EXPIRE_MINUTES;
import static com.daxi.limit.OrderLimit.PAY_NO_STATUS;
import static com.daxi.limit.OrderLimit.PAY_STATUS_PAID;
import static com.daxi.limit.OrderLimit.PAY_TIME_CACHE_MINUTES;
import static com.daxi.limit.OrderLimit.PAY_TIME_MINUTES;
import static com.daxi.limit.OrderLimit.REFUND_AND_RETURN;
import static com.daxi.limit.OrderLimit.REFUND_SUCCESS;
import static com.daxi.limit.OrderLimit.SKU_CACHE_MINUTE;
import static com.daxi.limit.OrderLimit.SPU_CACHE_MINUTE;
import static com.daxi.limit.OrderLimit.WAIT_MERCHANT_AUDIT;
import static com.daxi.limit.OrderLimit.WAIT_MERCHANT_RECEIVE;
import static com.daxi.limit.OrderLimit.WAIT_USER_RETURN;
import static com.daxi.response.CommonResponse.TRY_AGAIN;
import static com.daxi.response.GoodsResponse.GOODS_NOT_ON_SALE;
import static com.daxi.response.OrderResponse.ADDRESS_MODIFY_NOT_EXIST;
import static com.daxi.response.OrderResponse.CREATE_ORDER_ERROR;
import static com.daxi.response.OrderResponse.NO_REASON_AND_IMG;
import static com.daxi.response.OrderResponse.ORDER_ADDRESS_MODIFY_STATUS_ERROR;
import static com.daxi.response.OrderResponse.ORDER_NOT_CHANGE_ADDRESS;
import static com.daxi.response.OrderResponse.ORDER_NOT_EXIST;
import static com.daxi.response.OrderResponse.ORDER_NOT_REPEAT;
import static com.daxi.response.OrderResponse.ORDER_REFUND_STATUS_ERROR;
import static com.daxi.response.OrderResponse.ORDER_STATUS_ERROR;
import static com.daxi.response.OrderResponse.PARAM_ERROR;
import static com.daxi.response.OrderResponse.PAYMENT_ALREADY_PAID;
import static com.daxi.response.OrderResponse.PAYMENT_EXPIRED;
import static com.daxi.response.OrderResponse.PAYMENT_NOT_PAID;
import static com.daxi.response.OrderResponse.PAYMENT_ORDER_NOT_EXIST;
import static com.daxi.response.OrderResponse.REFUNDS_ERROR;
import static com.daxi.response.OrderResponse.REFUND_NOT_EXIST;
import static com.daxi.response.OrderResponse.REFUND_ORDER_STATUS_NOT_SUPPORT;
import static com.daxi.response.OrderResponse.REFUND_STATUS_ERROR;
import static com.daxi.response.OrderResponse.SKU_SOLDED_OUT;
import static com.daxi.response.UserResponse.NOT_LOGIN;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {
    private final @NonNull UserOrderMapper userOrderMapper;
    private final @NonNull UserOrderItemMapper userOrderItemMapper;
    private final @NonNull StringRedisTemplate stringRedisTemplate;
    private final @NonNull GoodsSkuMapper goodsSkuMapper;
    private final @NonNull GoodsSpuMapper goodsSpuMapper;
    private final @NonNull RedisUtil redisUtil;
    private final @NonNull PayUtil payUtil;
    private final @NonNull ObjectMapper objectMapper;
    private final @NonNull OrderAddressModifyMapper orderAddressModifyMapper;
    private final @NonNull UserFeignClient userFeignClient;
    private final @NonNull OrderRefundMapper orderRefundMapper;
    private final @NonNull OrderRefundToRefundDetailDto orderRefundToRefundDetailDto;
    private final @NonNull OrderAddressModifyToDto orderAddressModifyToDto;
    private final @NonNull RefundToDto refundToDto;
    private final @NonNull OrderTimeoutProducer orderTimeoutProducer;
    private final @NonNull RefundTimeOutProducer refundTimeOutProducer;
    private final @NonNull AddressModifyTimeoutProducer addressModifyTimeoutProducer;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void craeteOrder(UserOrderAO ao) {
        // 1. 获取当前用户ID
        Long currentUserId = UserUtil.getLocalUserId();
        if(currentUserId== null){
            throw new BusinessException(NOT_LOGIN);
        }
        // 2. 分布式锁防重提交
        String lockKey = ORDER_SUBMIT_LOCK.format(currentUserId);
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, ORDER_SUBMIT_LOCK_ON, ORDER_SUBMIT_LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(lock)) {
            throw new BusinessException(ORDER_NOT_REPEAT);
        }

        try {
            // 3. 校验库存并预扣减 (Redis)
            boolean stockResult = redisUtil.deductStock(GOODS_STOCK.format(ao.getSkuId()), ao.getBuyNum());
            if (!stockResult) {
                throw new BusinessException(SKU_SOLDED_OUT);
            }

            // 4. 获取 SKU 真实信息 (Redis -> DB)
            GoodsSku sku;
            String skuKey = GoodsKey.SKU_DETAIL_CACHE.format(ao.getSkuId());
            String skuJson = stringRedisTemplate.opsForValue().get(skuKey);
            if (skuJson != null) {
                sku=JSON.parseObject(skuJson, GoodsSku.class);
            }
            LambdaQueryWrapper<GoodsSku> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper
                    .eq(GoodsSku::getSkuId, ao.getSkuId())
                    .select(
                            GoodsSku::getSpuId,
                            GoodsSku::getPrice,
                            GoodsSku::getSkuSpec,
                            GoodsSku::getStatus
                    );
            sku = goodsSkuMapper.selectOne(queryWrapper);
            if (sku != null) {
                stringRedisTemplate.opsForValue().set(skuKey, JSON.toJSONString(sku), SKU_CACHE_MINUTE, TimeUnit.MINUTES);
            }
            if (sku == null || sku.getStatus() != SKU_ON_SALE) {
                throw new BusinessException(GOODS_NOT_ON_SALE);
            }

            // 5. 获取 SPU 真实信息 (Redis -> DB)
            GoodsSpu spu;
            String spuKey = GoodsKey.SPU_DETAIL_CACHE.format(sku.getSpuId());
            String json = stringRedisTemplate.opsForValue().get(spuKey);
            if (json != null) {
                spu=JSON.parseObject(json, GoodsSpu.class);
            }
            LambdaQueryWrapper<GoodsSpu> queryWrapperSpu = new LambdaQueryWrapper<>();
            queryWrapperSpu
                    .eq(GoodsSpu::getSpuId, sku.getSpuId())
                    .select(
                            GoodsSpu::getShopId,
                            GoodsSpu::getTitle,
                            GoodsSpu::getMainImg,
                            GoodsSpu::getStatus
                    );
            spu = goodsSpuMapper.selectOne(queryWrapperSpu);
            if (spu != null) {
                stringRedisTemplate.opsForValue().set(spuKey, JSON.toJSONString(spu), SPU_CACHE_MINUTE, TimeUnit.MINUTES);
            }
            if (spu == null || spu.getStatus() != SPU_NOT_ON_SALE) {
                throw new BusinessException(GOODS_NOT_ON_SALE);
            }

            // 6. 后端计算金额
            BigDecimal perPrice = PriceUtil.fenToYuan(sku.getPrice());
            BigDecimal totalPrice = perPrice.multiply(new BigDecimal(ao.getBuyNum()));
            LocalDateTime now = LocalDateTime.now();

            // 生成订单ID
            Long orderSn = IdWorker.getId();

            // 7. 组装并插入【订单主表 UserOrder】
            UserOrder order = new UserOrder();
            order.setOrderSn(orderSn.toString());
            order.setUserId(currentUserId);
            order.setShopId(spu.getShopId());

            // 主表存总价和收货信息（严格对应你的表结构）
            order.setPrice(totalPrice);
            order.setReceiverName(ao.getReceiverName());
            order.setReceiverPhone(ao.getReceiverPhone());
            order.setReceiverAddress(ao.getReceiverAddress());
            order.setRemark(ao.getRemark());

            userOrderMapper.insert(order);

            // 8. 组装并插入【订单明细表 UserOrderItem】
            UserOrderItem item = new UserOrderItem();
            item.setOrderId(order.getOrderId());
            item.setSpuId(sku.getSpuId());
            item.setSkuId(ao.getSkuId());

            // 明细表存商品快照和单价
            item.setGoodsName(spu.getTitle());
            item.setGoodsImg(spu.getMainImg());
            item.setSkuSpec(sku.getSkuSpec());
            item.setPerPrice(perPrice);
            item.setBuyNum(ao.getBuyNum());

            userOrderItemMapper.insert(item);

            // 9. 更新数据库库存

            new LambdaUpdateChainWrapper<>(goodsSkuMapper)
                        .eq(GoodsSku::getSkuId, ao.getSkuId())
                        .setDecrBy(GoodsSku::getStock, ao.getBuyNum())
                        .update();

            // 10. 写入 Redis 索引缓存
            redisUtil.cacheOrderIndex(order, spu, sku, ao.getBuyNum());
            
            // 11. 发送订单超时检查延时消息（30分钟后检查）
            try {
                com.daxi.domain.dto.OrderTimeoutMessageDTO timeoutMessage = new com.daxi.domain.dto.OrderTimeoutMessageDTO();
                timeoutMessage.setOrderId(order.getOrderId());
                timeoutMessage.setUserId(currentUserId);
                timeoutMessage.setShopId(spu.getShopId());
                
                // 延迟级别 16 表示 30 分钟
                orderTimeoutProducer.sendOrderTimeoutMessage(timeoutMessage, 16);
                
                log.info("订单超时检查延时消息已发送, orderSn: {}, 将在30分钟后检查支付状态", orderSn);
            } catch (Exception e) {
                log.error("发送订单超时检查延时消息失败，但不影响订单创建, orderSn: {}", orderSn, e);
            }

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(CREATE_ORDER_ERROR);
        } finally {
            stringRedisTemplate.delete(lockKey);
        }
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserOrderDTO getDetailOrderById(Long orderId) {
        Long userId=UserUtil.getLocalUserId();
        if(userId==null){
            throw new BusinessException(NOT_LOGIN);
        }
        return  userOrderMapper.getDetailOrder(orderId,userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserOrderDTO getOrderForReceiveOrderAndOkStatus(Long orderId, Long userId) {

        LambdaQueryWrapper<UserOrder> userOrderLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userOrderLambdaQueryWrapper
                .eq(UserOrder::getOrderId,orderId)
                .eq(UserOrder::getUserId,userId)
                .select(UserOrder::getShopId, UserOrder::getPayTime, UserOrder::getPrice);
        UserOrder userOrder = userOrderMapper.selectOne(userOrderLambdaQueryWrapper);
        if(userOrder==null){
            return null;
        }

        LambdaQueryWrapper<UserOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserOrder::getOrderId, orderId)
                .eq(UserOrder::getUserId, userId)
                .select(UserOrder::getShopId, UserOrder::getOrderStatus);

        UserOrder order = userOrderMapper.selectOne(queryWrapper);
        if (order == null) {
            throw new BusinessException(ORDER_NOT_EXIST);
        }

        Integer oldStatus = order.getOrderStatus();
        Long shopId = order.getShopId();
        int newStatus = ORDER_STATUS_COMPLETED;

        LambdaUpdateChainWrapper<UserOrder> updateWrapper = new LambdaUpdateChainWrapper<>(userOrderMapper);
        updateWrapper.eq(UserOrder::getOrderId, orderId)
                .eq(UserOrder::getUserId, userId)
                .set(UserOrder::getOrderStatus, newStatus)
                .set(UserOrder::getFinishTime, LocalDateTime.now())
                .update();

        redisUtil.changeOrderIndex(
                orderId.toString(),
                userId.toString(),
                shopId.toString(),
                oldStatus,
                newStatus
        );
        //改detail的缓存
        LambdaQueryWrapper<UserOrderItem> userOrderItemLambdaQueryWrapper = new LambdaQueryWrapper<UserOrderItem>();
        userOrderItemLambdaQueryWrapper
                .eq(UserOrderItem::getOrderId,orderId)
                .select(UserOrderItem::getSpuId);
        UserOrderItem userOrderItem = userOrderItemMapper.selectOne(userOrderItemLambdaQueryWrapper);
        UserOrderDTO userOrderDTO = new UserOrderDTO();
        userOrderDTO.setShopId(userOrder.getShopId());
        userOrderDTO.setPayTime(userOrder.getPayTime());
        userOrderDTO.setPrice(userOrder.getPrice());
        userOrderDTO.setSpuId(userOrderItem.getSpuId());
        return userOrderDTO;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId) {
        Long userId = UserUtil.getLocalUserId();
        if (userId == null){
            throw new BusinessException(NOT_LOGIN);
        }
        boolean update = new LambdaUpdateChainWrapper<UserOrder>(userOrderMapper)
                .eq(UserOrder::getOrderId, orderId)
                .eq(UserOrder::getUserId, userId)
                .set(UserOrder::getOrderStatus, ORDER_STATUS_CANCELLED)
                .update();
        if(!update){
            throw new BusinessException(TRY_AGAIN);
        }
        redisUtil.deleteOrderCache(orderId.toString(), userId.toString());
    }

    @Override
    public OrderStatusCountDTO getOrderEveryStatusNumberForShop(Long shopId) {
        OrderStatusCountDTO dto = userOrderMapper.countOrderByStatusForShop(shopId);
        if (dto == null) {
            dto = new OrderStatusCountDTO();
        }

        // 在 Service 层计算总和
        int total = Optional.ofNullable(dto.getPendingPayment()).orElse(0) +
                Optional.ofNullable(dto.getPendingShipment()).orElse(0) +
                Optional.ofNullable(dto.getPendingReceipt()).orElse(0) +
                Optional.ofNullable(dto.getPendingReview()).orElse(0) +
                Optional.ofNullable(dto.getCompleted()).orElse(0) +
                Optional.ofNullable(dto.getCancelled()).orElse(0) +
                Optional.ofNullable(dto.getRefundAfterSales()).orElse(0);

        dto.setTotal(total);
        return dto;
    }

    /**
     * 用户选择状态然后给出简易订单列表
     *
     * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<UserSimpleOrderDTO> getSimpleOrderByStatusForUser(Long userId, Integer status, Integer pageNum, Integer pageSize) {
        String Statuskey;
        if(status==-1){
            Statuskey=ORDER_USER_ALL.format(userId);
        }else{
            Statuskey=ORDER_USER_STATUS.format(userId, status);
        }
        int start = (pageNum-1)*pageSize;
        int end = start+pageSize-1;
        /**
         * 这里的策略是id的缓存是半年，如果说id缓存过期了那么detail基本上也全部过期了
         * 所以如果缓存id没有的话直接用数据库查询分页
         * 因为距离下单过了半年了所以不补充缓存
         * */
        Set<String> orderIdsSet = stringRedisTemplate.opsForZSet().reverseRange(Statuskey, start, end);
        if (orderIdsSet == null || orderIdsSet.isEmpty()) {
            return userOrderMapper.selectSimpleOrderPage(userId, status, start, pageSize);
        }
        List<String> orderIds = new ArrayList<>(orderIdsSet);
        if (orderIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 拼接所有订单详情key
        List<String> keys = new ArrayList<>(orderIds.size());
        for (String orderId : orderIds) {
            keys.add(ORDER_DETAIL.format(orderId));
        }

        // 2. ================= 核心：Redis 管道批量执行 =================
        List<Object> pipelineResults = stringRedisTemplate.executePipelined( (RedisCallback<?>)(connection) -> {
            for (String key : keys) {
                connection.stringCommands().get(key.getBytes(StandardCharsets.UTF_8));
            }
            return null;
        });

        // 3. 解析管道结果 + 处理缓存未命中
        // 3. 统计 Miss 并收集 Miss 的 ID
        List<String> missIds = new ArrayList<>();
        for (int i = 0; i < pipelineResults.size(); i++) {
            if (pipelineResults.get(i) == null) {
                missIds.add(orderIds.get(i));
            }
        }

        int missNum = missIds.size();
        int totalCount = pipelineResults.size();

        // 4. 【决策】Miss 率 >= 50%，整页降级
        if (missNum * 2 >= totalCount) {
            return userOrderMapper.selectSimpleOrderPage(userId, status, start, pageSize);
        }

        // 5. 【决策】小规模 Miss，批量查库回填
        Map<String, UserSimpleOrderDTO> missOrderMap = new HashMap<>();
        if (!missIds.isEmpty()) {
            //  关键点：一次性查出所有 Miss 的订单，避免 N+1 问题
            List<UserSimpleOrderDTO> dbOrders = userOrderMapper.selectSimpleOrderByIds(missIds);



            for (UserSimpleOrderDTO dto : dbOrders) {
                missOrderMap.put(dto.getOrderId().toString(), dto);
                redisUtil.setOrderCache(dto); // 异步或同步回填
            }


        }

        // 6. 组装最终结果（保持 ZSet 顺序）
        List<UserSimpleOrderDTO> orderDetailList = new ArrayList<>(totalCount);
        for (int i = 0; i < pipelineResults.size(); i++) {
            Object result = pipelineResults.get(i);
            String orderId = orderIds.get(i);

            if (result == null) {
                // 从批量查库的结果里取
                UserSimpleOrderDTO dbOrder = missOrderMap.get(orderId);
                if (dbOrder != null) {
                    orderDetailList.add(dbOrder);
                }
                // 如果 dbOrder 也是 null，说明订单被删了，直接跳过，保证数据干净
            } else {
                try {
                    String json = new String((byte[]) result, StandardCharsets.UTF_8);
                    UserSimpleOrderDTO dto = objectMapper.readValue(json, UserSimpleOrderDTO.class);
                    dto.setOrderId(Long.valueOf(orderId));
                    orderDetailList.add(dto);
                } catch (Exception e) {
                    log.error("无法从redis缓存里面读取数据", e);
                }
            }
        }
        return orderDetailList;
    }

    /**
     * 商家根据状态获取订单列表
     * */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<UserSimpleOrderDTO> getSimpleOrderByStatusForShop(Long shopId, Integer status, Integer pageNum, Integer pageSize) {
        String statusKey;
        if(status == -1){
            throw new BusinessException(PARAM_ERROR);
        }else{
            statusKey = ORDER_SHOP_STATUS.format(shopId, status);
        }
        
        int start = (pageNum - 1) * pageSize;
        int end = start + pageSize - 1;

        Set<String> orderIdsSet = stringRedisTemplate.opsForZSet().reverseRange(statusKey, start, end);
        if (orderIdsSet == null || orderIdsSet.isEmpty()) {
            int offset = (pageNum - 1) * pageSize;
            return userOrderMapper.selectSimpleOrderPageForShop(shopId, status, offset, pageSize);
        }
        
        List<String> orderIds = new ArrayList<>(orderIdsSet);
        if (orderIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 拼接所有订单详情key
        List<String> keys = new ArrayList<>(orderIds.size());
        for (String orderId : orderIds) {
            keys.add(ORDER_DETAIL.format(orderId));
        }

        // 2. ================= 核心：Redis 管道批量执行 =================
        List<Object> pipelineResults = stringRedisTemplate.executePipelined((RedisCallback<?>)(connection) -> {
            for (String key : keys) {
                connection.stringCommands().get(key.getBytes(StandardCharsets.UTF_8));
            }
            return null;
        });

        // 3. 解析管道结果 + 处理缓存未命中
        // 3. 统计 Miss 并收集 Miss 的 ID
        List<String> missIds = new ArrayList<>();
        for (int i = 0; i < pipelineResults.size(); i++) {
            if (pipelineResults.get(i) == null) {
                missIds.add(orderIds.get(i));
            }
        }

        int missNum = missIds.size();
        int totalCount = pipelineResults.size();

        // 4. 【决策】Miss 率 >= 50%，整页降级
        if (missNum * 2 >= totalCount) {
            int offset = (pageNum - 1) * pageSize;
            return userOrderMapper.selectSimpleOrderPageForShop(shopId, status, offset, pageSize);
        }

        // 5. 【决策】小规模 Miss，批量查库回填
        Map<String, UserSimpleOrderDTO> missOrderMap = new HashMap<>();
        if (!missIds.isEmpty()) {
            // 关键点：一次性查出所有 Miss 的订单，避免 N+1 问题
            List<UserSimpleOrderDTO> dbOrders = userOrderMapper.selectSimpleOrderByIds(missIds);

            for (UserSimpleOrderDTO dto : dbOrders) {
                missOrderMap.put(dto.getOrderId().toString(), dto);
                redisUtil.setOrderCache(dto); // 异步或同步回填
            }
        }

        // 6. 组装最终结果（保持 ZSet 顺序）
        List<UserSimpleOrderDTO> orderDetailList = new ArrayList<>(totalCount);
        for (int i = 0; i < pipelineResults.size(); i++) {
            Object result = pipelineResults.get(i);
            String orderId = orderIds.get(i);

            if (result == null) {
                // 从批量查库的结果里取
                UserSimpleOrderDTO dbOrder = missOrderMap.get(orderId);
                if (dbOrder != null) {
                    orderDetailList.add(dbOrder);
                }
                // 如果 dbOrder 也是 null，说明订单被删了，直接跳过，保证数据干净
            } else {
                try {
                    String json = new String((byte[]) result, StandardCharsets.UTF_8);
                    UserSimpleOrderDTO dto = objectMapper.readValue(json, UserSimpleOrderDTO.class);
                    dto.setOrderId(Long.valueOf(orderId));
                    orderDetailList.add(dto);
                } catch (Exception e) {
                    log.error("无法从redis缓存里面读取数据", e);
                }
            }
        }
        return orderDetailList;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderStatusCountDTO getOrderEveryStatusNumberForUser(Long userId) {
        OrderStatusCountDTO dto = userOrderMapper.countOrderByStatusForUser(userId);
        if (dto == null) {
            dto = new OrderStatusCountDTO();
        }

        // 在 Service 层计算总和
        int total = Optional.ofNullable(dto.getPendingPayment()).orElse(0) +
                Optional.ofNullable(dto.getPendingShipment()).orElse(0) +
                Optional.ofNullable(dto.getPendingReceipt()).orElse(0) +
                Optional.ofNullable(dto.getPendingReview()).orElse(0) +
                Optional.ofNullable(dto.getCompleted()).orElse(0) +
                Optional.ofNullable(dto.getCancelled()).orElse(0) +
                Optional.ofNullable(dto.getRefundAfterSales()).orElse(0);

        dto.setTotal(total);
        return dto;
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAddress(UserOrderAddressAO address) {
        Long userId=UserUtil.getLocalUserId();
        if(userId==null){
            throw new BusinessException(NOT_LOGIN);
        }
        LambdaQueryWrapper<UserOrder> queryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<UserOrder> select = queryWrapper
                .eq(UserOrder::getOrderId, address.getOrderId())
                .eq(UserOrder::getUserId,userId)
                .select(
                        UserOrder::getShopId,
                        UserOrder::getOrderStatus,
                        UserOrder::getOperateStatus,
                        UserOrder::getReceiverName,
                        UserOrder::getReceiverPhone,
                        UserOrder::getReceiverAddress
                );

        UserOrder order = userOrderMapper.selectOne(select);

        if(order== null){
            throw new BusinessException(ORDER_NOT_EXIST);
        }
        if(order.getOperateStatus()!=OPERATE_STATUS_NONE){
            throw new BusinessException(ORDER_STATUS_ERROR);
        }
        Integer orderStatus = order.getOrderStatus();

        String newDetailAddress = String.join("",
                address.getCurrentProvince(),
                address.getCurrentCity(),
                address.getCurrentDistrict(),
                address.getCurrentDetailAddress());
        if(orderStatus==ORDER_STATUS_PENDING_PAYMENT||orderStatus==ORDER_STATUS_PENDING_SHIPMENT){
            LambdaUpdateChainWrapper<UserOrder> userOrderLambdaUpdateChainWrapper = new LambdaUpdateChainWrapper<>(userOrderMapper);
            userOrderLambdaUpdateChainWrapper
                    .eq(UserOrder::getOrderId, address.getOrderId())
                    .set(UserOrder::getReceiverName, address.getCurrentReceiverName())
                    .set(UserOrder::getReceiverPhone, address.getCurrentReceiverPhone())
                    .set(UserOrder::getReceiverAddress, newDetailAddress)
                    .set(UserOrder::getAddressStatus,ADDRESS_STATUS_CHANGE)
                    .update();
        }else if(orderStatus==ORDER_STATUS_PENDING_RECEIPT){
            /**
             * 这里就发送请求等待商家同意
             * 如果同意的话，因为物流那边是会实时更新状态的，发现改了他们会改发
             * 如果说商家的晚了的话，要么是申请过期了或者是货已经到了，那就是正常进行，
             * 货到了商家自己会拒绝，不拒绝也会自动过期
             * */

            OrderAddressModify orderAddressModify = new OrderAddressModify();
            orderAddressModify.setOrderId(address.getOrderId());
            orderAddressModify.setUserId(userId);
            orderAddressModify.setShopId(order.getShopId());
            orderAddressModify.setOldName(order.getReceiverName());
            orderAddressModify.setOldPhone(order.getReceiverPhone());
            orderAddressModify.setOldDetail(order.getReceiverAddress());
            orderAddressModify.setNewName(address.getCurrentReceiverName());
            orderAddressModify.setNewPhone(address.getCurrentReceiverPhone());
            orderAddressModify.setNewDetail(newDetailAddress);
            orderAddressModify.setExpireTime(LocalDateTime.now().plusHours(ORDER_CHANGE_ADDRESS_REQUEST_HOURS));
            orderAddressModify.setStatus(ORDER_ADDRESS_MODIFY_STATUS_WAIT);
            orderAddressModifyMapper.insert(orderAddressModify); // Fixed method call

            LambdaUpdateChainWrapper<UserOrder> userOrderLambdaUpdateChainWrapper = new LambdaUpdateChainWrapper<>(userOrderMapper);
            userOrderLambdaUpdateChainWrapper
                    .eq(UserOrder::getOrderId, address.getOrderId())
                    .set(UserOrder::getOperateStatus, OPERATE_STATUS_APPLY_ADDRESS_CHANGE)
                    .update();

            String requestShopKey = ORDER_CHANGE_ADDRESS_SHOP_REQUEST.format(order.getShopId(), ORDER_ADDRESS_MODIFY_STATUS_WAIT);
            String requestUserKey=ORDER_CHANGE_ADDRESS_USER_REQUEST.format(userId, ORDER_ADDRESS_MODIFY_STATUS_WAIT);
            long current=System.currentTimeMillis();

            byte[] ttlBytes = String.valueOf(ORDER_CHANGE_ADDRESS_REQUEST_SECONDS).getBytes();
            stringRedisTemplate.executePipelined((RedisCallback<Object>)connection->{
                connection.zAdd(requestShopKey.getBytes(StandardCharsets.UTF_8),current,JSON.toJSONString(address).getBytes(StandardCharsets.UTF_8));
                connection.zAdd(requestUserKey.getBytes(StandardCharsets.UTF_8),current,JSON.toJSONString(address).getBytes(StandardCharsets.UTF_8));
                connection.execute("EXPIRE",
                        requestUserKey.getBytes(StandardCharsets.UTF_8),
                        ttlBytes);
                connection.execute("EXPIRE",
                        requestShopKey.getBytes(StandardCharsets.UTF_8),
                        ttlBytes);
                return null;
            } );

            redisUtil.setAddressModifyCache(
                    orderAddressModifyToDto.orderAddressModefyToDto(orderAddressModify));
            
            // 发送地址修改超时检查延时消息（2小时）
            try {
                com.daxi.domain.dto.AddressModifyTimeoutMessageDTO timeoutMessage = new com.daxi.domain.dto.AddressModifyTimeoutMessageDTO();
                timeoutMessage.setOrderId(address.getOrderId());
                timeoutMessage.setUserId(userId);
                timeoutMessage.setShopId(order.getShopId());
                
                // 这里使用最大延迟级别 18 (2小时)
                addressModifyTimeoutProducer.sendAddressModifyTimeoutMessage(timeoutMessage, 18);
                
                log.info("地址修改超时检查延时消息已发送, orderId: {}, 将在2小时后检查", address.getOrderId());
            } catch (Exception e) {
                log.error("发送地址修改超时检查延时消息失败，但不影响地址修改申请, orderId: {}", address.getOrderId(), e);
            }
        }else{
            throw new BusinessException(ORDER_NOT_CHANGE_ADDRESS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendRefunds(OrderRefundAO refunds) {
        Long orderId = refunds.getOrderId();
        Long userId = UserUtil.getLocalUserId();
        if(userId==null){
            throw new BusinessException(NOT_LOGIN);
        }

        LambdaQueryWrapper<UserOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(UserOrder::getOrderId, orderId)
                .eq(UserOrder::getUserId, userId)
                .select(
                        UserOrder::getShopId,
                        UserOrder::getPrice,
                        UserOrder::getOrderStatus,
                        UserOrder::getOperateStatus
                );
        UserOrder order = userOrderMapper.selectOne(queryWrapper);

        if(order == null){
            throw new BusinessException(ORDER_NOT_EXIST);
        }
        if(order.getOperateStatus()!=OPERATE_STATUS_NONE){
            throw new BusinessException(ORDER_STATUS_ERROR);
        }
        Integer dbOrderStatus = order.getOrderStatus();

        if(dbOrderStatus == ORDER_STATUS_PENDING_SHIPMENT&&
                refunds.getRefundType()==ONLY_REFUND ){
            /**直接退款，同时把退款申请写入数据库，状态设为已完成,还有购买事实表*/
            RefundDTO refundDTO = payUtil.payBack(orderId);
            if(refundDTO==null){
                throw new BusinessException(REFUNDS_ERROR);
            }
            userFeignClient.invalidateOrderData(orderId);
            String refundNo = IdWorker.getIdStr();

            OrderRefund orderRefund = new OrderRefund();
            orderRefund.setRefundNo(refundNo);
            orderRefund.setOrderId(orderId);
            orderRefund.setUserId(userId);
            orderRefund.setShopId(order.getShopId());
            orderRefund.setRefundType(refunds.getRefundType());
            orderRefund.setRefundAmount(order.getPrice());
            orderRefund.setRefundReason(refunds.getRefundReason());
            orderRefund.setEvidenceImages(refunds.getEvidenceImages());
            orderRefund.setStatus(REFUND_SUCCESS);
            orderRefund.setApplyTime(LocalDateTime.now());
            orderRefund.setRefundChannel(refundDTO.getRefundChannel());
            orderRefund.setRefundNoChannel(refundDTO.getRefundNoChannel());
            orderRefund.setRefundTime(LocalDateTime.now());

            orderRefundMapper.insert(orderRefund);

            LambdaUpdateChainWrapper<UserOrder> userOrderLambdaUpdateChainWrapper = new LambdaUpdateChainWrapper<>(userOrderMapper);
            userOrderLambdaUpdateChainWrapper.eq(UserOrder::getOrderId, orderId)
                    .set(UserOrder::getOrderStatus, ORDER_STATUS_REFUND_AFTER_SALES)
                    .update();
            //缓存更改
            redisUtil.cacheRefund(orderId.toString(),userId.toString(),order.getShopId().toString(),orderRefund.getStatus());

            redisUtil.setRefundCache(
                    refundToDto.refundToSimpleDto(orderRefund)
            );
        }else if((dbOrderStatus ==ORDER_STATUS_PENDING_SHIPMENT
                ||dbOrderStatus==ORDER_STATUS_PENDING_RECEIPT
                ||dbOrderStatus==ORDER_STATUS_PENDING_REVIEW
                ||dbOrderStatus==ORDER_STATUS_COMPLETED)
                &&refunds.getRefundType()==REFUND_AND_RETURN){
            if(refunds.getRefundReason()== null&& refunds.getEvidenceImages()== null){
                throw new BusinessException(NO_REASON_AND_IMG);
            }
            String refundNo = IdWorker.getIdStr();
            OrderRefund orderRefund = new OrderRefund();
            orderRefund.setRefundNo(refundNo);
            orderRefund.setOrderId(orderId);
            orderRefund.setUserId(userId);
            orderRefund.setShopId(order.getShopId());
            orderRefund.setRefundType(refunds.getRefundType());
            orderRefund.setRefundAmount(order.getPrice());
            orderRefund.setRefundReason(refunds.getRefundReason());
            orderRefund.setEvidenceImages(refunds.getEvidenceImages());
            orderRefund.setStatus(WAIT_MERCHANT_AUDIT);
            orderRefund.setApplyTime(LocalDateTime.now());
            orderRefund.setExpireTime(LocalDateTime.now().plusHours(ORDER_REFUND_REQUEST_HOURS));
            /**先落库*/
            orderRefundMapper.insert(orderRefund);

            LambdaUpdateChainWrapper<UserOrder> userOrderLambdaUpdateChainWrapper = new LambdaUpdateChainWrapper<>(userOrderMapper);
            userOrderLambdaUpdateChainWrapper
                    .eq(UserOrder::getOrderId, orderId)
                    .set(UserOrder::getOperateStatus, OPERATE_STATUS_REFUND_AND_RETURN)
                    .update();
            //缓存
            redisUtil.cacheRefund(orderId.toString(),userId.toString(),order.getShopId().toString(),orderRefund.getStatus());
            redisUtil.setRefundCache(
                    refundToDto.refundToSimpleDto(orderRefund)
            );
            
            // 发送退款超时检查延时消息（2h）
            try {
                com.daxi.domain.dto.RefundTimeOutMessage timeoutMessage = new com.daxi.domain.dto.RefundTimeOutMessage();
                timeoutMessage.setOrderId(orderId);
                timeoutMessage.setUserId(userId);
                timeoutMessage.setShopId(order.getShopId());
                
                // 这里使用最大延迟级别 18 (2小时)
                refundTimeOutProducer.sendRefundTimeOutMessage(timeoutMessage, 18);
                
                log.info("退款超时检查延时消息已发送, refundId: {}, orderId: {}",
                        orderRefund.getId(), orderId);
            } catch (Exception e) {
                log.error("发送退款超时检查延时消息失败，但不影响退款申请, refundId: {}", orderRefund.getId(), e);
            }
        }else{
            throw new BusinessException(REFUND_ORDER_STATUS_NOT_SUPPORT);
        }

    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditAddressModify(AddressModifyAuditAO audit) {
        Long shopId=UserUtil.getLocalShopId();
        if(shopId==null){
            throw new BusinessException(NOT_LOGIN);
        }

        Long orderId = audit.getOrderId();
        LambdaQueryWrapper<OrderAddressModify> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(OrderAddressModify::getOrderId, String.valueOf(audit.getOrderId()))
                .eq(OrderAddressModify::getShopId, shopId)
                .select(
                        OrderAddressModify::getUserId,
                        OrderAddressModify::getExpireTime,
                        OrderAddressModify::getNewName,
                        OrderAddressModify::getNewPhone,
                        OrderAddressModify::getNewDetail
                );

        OrderAddressModify addressModify = orderAddressModifyMapper.selectOne(queryWrapper);

        if(addressModify == null||addressModify.getStatus()!=ORDER_ADDRESS_MODIFY_STATUS_WAIT){
            throw new BusinessException(ADDRESS_MODIFY_NOT_EXIST);
        }
        if(addressModify.getExpireTime().isBefore(LocalDateTime.now())){
            //到时候要拆走
            LambdaUpdateChainWrapper<OrderAddressModify> updateWrapper = new LambdaUpdateChainWrapper<>(orderAddressModifyMapper);
            updateWrapper
                    .eq(OrderAddressModify::getOrderId, audit.getOrderId())
                    .set(OrderAddressModify::getStatus, ORDER_ADDRESS_MODIFY_STATUS_EXPIRE)
                    .update();
            new LambdaUpdateChainWrapper<>(userOrderMapper)
                    .eq(UserOrder::getOrderId, audit.getOrderId())
                    .set(UserOrder::getOperateStatus, OPERATE_STATUS_NONE)
                    .update();
            redisUtil.changeAddressRequestCache(
                    audit.getOrderId().toString(),
                    addressModify.getUserId().toString(),
                    shopId.toString(),
                    ORDER_ADDRESS_MODIFY_STATUS_WAIT,
                    ORDER_ADDRESS_MODIFY_STATUS_EXPIRE
                    );
            redisUtil.deleteAddressModifyCache(audit.getOrderId().toString());
            return;
        }
        if(audit.getAuditResult() == AUDIT_RESULT_AGREE){
            LambdaUpdateChainWrapper<UserOrder> updateWrapper = new LambdaUpdateChainWrapper<>(userOrderMapper);
            updateWrapper
                    .eq(UserOrder::getOrderId, audit.getOrderId())
                    .set(UserOrder::getReceiverName, addressModify.getNewName())
                    .set(UserOrder::getReceiverPhone, addressModify.getNewPhone())
                    .set(UserOrder::getReceiverAddress, addressModify.getNewDetail())
                    .set(UserOrder::getOperateStatus,OPERATE_STATUS_NONE)
                    .set(UserOrder::getAddressStatus,ORDER_ADDRESS_CHANGE)
                    .update();

            LambdaUpdateChainWrapper<OrderAddressModify> modifyUpdateWrapper = new LambdaUpdateChainWrapper<>(orderAddressModifyMapper);
            modifyUpdateWrapper
                    .eq(OrderAddressModify::getOrderId, orderId)
                    .set(OrderAddressModify::getStatus, ORDER_ADDRESS_MODIFY_STATUS_SUCCESS)
                    .update();
            redisUtil.changeAddressRequestCache(
                    audit.getOrderId().toString(),
                    addressModify.getUserId().toString(),
                    shopId.toString(),
                    ORDER_ADDRESS_MODIFY_STATUS_WAIT,
                    ORDER_ADDRESS_MODIFY_STATUS_SUCCESS
            );
            redisUtil.deleteAddressModifyCache(audit.getOrderId().toString());

        } else if(audit.getAuditResult() == ORDER_ADDRESS_MODIFY_STATUS_REJECT){
            new LambdaUpdateChainWrapper<>(orderAddressModifyMapper)
                    .eq(OrderAddressModify::getOrderId, orderId)
                    .set(OrderAddressModify::getStatus, ORDER_ADDRESS_MODIFY_STATUS_REJECT)
                    .update();

            new LambdaUpdateChainWrapper<>(userOrderMapper)
                    .eq(UserOrder::getOrderId, audit.getOrderId())
                    .set(UserOrder::getOperateStatus, OPERATE_STATUS_NONE)
                    .update();
            redisUtil.changeAddressRequestCache(
                    audit.getOrderId().toString(),
                    addressModify.getUserId().toString(),
                    shopId.toString(),
                    ORDER_ADDRESS_MODIFY_STATUS_WAIT,
                    ORDER_ADDRESS_MODIFY_STATUS_REJECT
            );
            redisUtil.deleteAddressModifyCache(audit.getOrderId().toString());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditRefund(RefundAuditAO audit) {
        Long shopId=UserUtil.getLocalShopId();
        if(shopId==null){
            throw new BusinessException(NOT_LOGIN);
        }
        LambdaQueryWrapper<OrderRefund> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(OrderRefund::getId, audit.getRefundId())
                .eq(OrderRefund::getShopId, shopId)
                .select(
                        OrderRefund::getOrderId,
                        OrderRefund::getExpireTime,
                        OrderRefund::getUserId
                );

        OrderRefund refund = orderRefundMapper.selectOne(queryWrapper);
        if(refund == null){
            throw new BusinessException(REFUND_NOT_EXIST);
        }
        LambdaQueryWrapper<UserOrder> userOrderLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userOrderLambdaQueryWrapper
                .eq(UserOrder::getOrderId,refund.getOrderId())
                .select(
                        UserOrder::getOrderStatus
                );
        UserOrder userOrder = userOrderMapper.selectOne(userOrderLambdaQueryWrapper);


        if(refund.getExpireTime().isBefore(LocalDateTime.now())){
            return;
        }
        if(!(
                refund.getStatus().equals(WAIT_MERCHANT_AUDIT) ||
                (
                        refund.getStatus().equals(WAIT_MERCHANT_RECEIVE)&&
                        refund.getRefundType()!=REFUND_AND_RETURN
                )
        )
        ){
            throw new BusinessException(REFUND_STATUS_ERROR);
        }

        if(audit.getAuditResult() == AUDIT_RESULT_AGREE){

            if((refund.getRefundType() == ONLY_REFUND)||
                    (refund.getRefundType() == REFUND_AND_RETURN&&
                            refund.getStatus().equals(WAIT_MERCHANT_RECEIVE))
            ){
                RefundDTO refundDTO = payUtil.payBack(refund.getOrderId());
                if(refundDTO == null){
                    throw new BusinessException(REFUNDS_ERROR);
                }
                LambdaUpdateChainWrapper<OrderRefund> refundUpdateWrapper = new LambdaUpdateChainWrapper<>(orderRefundMapper);
                refundUpdateWrapper.eq(OrderRefund::getId, audit.getRefundId())
                        .set(OrderRefund::getStatus, REFUND_SUCCESS)
                        .set(OrderRefund::getAuditTime, LocalDateTime.now())
                        .set(OrderRefund::getRefundChannel, refundDTO.getRefundChannel())
                        .set(OrderRefund::getRefundNoChannel, refundDTO.getRefundNoChannel())
                        .set(OrderRefund::getRefundTime, LocalDateTime.now())
                        .update();
                new LambdaUpdateChainWrapper<>(userOrderMapper)
                        .eq(UserOrder::getOrderId, refund.getOrderId())
                        .set(UserOrder::getOrderStatus, ORDER_STATUS_REFUND_AFTER_SALES)
                        .set(UserOrder::getOperateStatus, OPERATE_STATUS_NONE)
                        .update();
                userFeignClient.invalidateOrderData(refund.getOrderId());

                redisUtil.changeRefundCache(
                        refund.getOrderId().toString(),
                        refund.getUserId().toString(),
                        shopId.toString(),
                        refund.getStatus(),
                        REFUND_SUCCESS
                );
                redisUtil.changeOrderIndex(
                        refund.getOrderId().toString(),
                        refund.getUserId().toString(),
                        shopId.toString(),
                        userOrder.getOrderStatus(),
                        ORDER_STATUS_REFUND_AFTER_SALES
                );
                redisUtil.deleteRefundCache(audit.getRefundId());

            } else if(refund.getRefundType() == REFUND_AND_RETURN){
                LambdaUpdateChainWrapper<OrderRefund> refundUpdateWrapper = new LambdaUpdateChainWrapper<>(orderRefundMapper);
                refundUpdateWrapper
                        .eq(OrderRefund::getId, audit.getRefundId())
                        .set(OrderRefund::getStatus, WAIT_USER_RETURN)
                        .set(OrderRefund::getAuditTime, LocalDateTime.now())
                        .update();
                redisUtil.changeRefundCache(
                        refund.getOrderId().toString(),
                        refund.getUserId().toString(),
                        shopId.toString(),
                        refund.getStatus(),
                        WAIT_USER_RETURN
                );
            }
            redisUtil.deleteRefundCache(audit.getRefundId());

        } else if(audit.getAuditResult() == AUDIT_RESULT_REJECT){
            new LambdaUpdateChainWrapper<>(orderRefundMapper)
                    .eq(OrderRefund::getId, audit.getRefundId())
                    .set(OrderRefund::getStatus, MERCHANT_REJECT)
                    .set(OrderRefund::getFailReason, audit.getRejectReason())
                    .set(OrderRefund::getAuditTime, LocalDateTime.now())
                    .update();

            new LambdaUpdateChainWrapper<>(userOrderMapper)
                    .eq(UserOrder::getOrderId, refund.getOrderId())
                    .set(UserOrder::getOperateStatus, OPERATE_STATUS_NONE)
                    .update();
            redisUtil.changeRefundCache(
                    refund.getOrderId().toString(),
                    refund.getUserId().toString(),
                    shopId.toString(),
                    refund.getStatus(),
                    MERCHANT_REJECT
            );
            redisUtil.deleteRefundCache(audit.getRefundId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void userShipForRefund(Long refundId, String expressCompany, String waybill) {
        Long orderId = UserUtil.getOrderId();
        if(orderId == null){
            throw new BusinessException(ORDER_NOT_EXIST);
        }
        LambdaQueryWrapper<OrderRefund> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(OrderRefund::getId, refundId)
                .select(OrderRefund::getStatus, OrderRefund::getUserId, OrderRefund::getShopId);
        OrderRefund refund = orderRefundMapper.selectById(queryWrapper);

        if (refund == null) {
            throw new BusinessException(REFUND_NOT_EXIST);
        }

        if (!refund.getStatus().equals(WAIT_USER_RETURN)) {
            throw new BusinessException(REFUND_STATUS_ERROR);
        }

        boolean result = payUtil.executeUserShipLogic(refundId, expressCompany, waybill);

        if (!result) {
            throw new BusinessException(REFUNDS_ERROR);
        }

        LambdaUpdateChainWrapper<OrderRefund> updateWrapper = new LambdaUpdateChainWrapper<>(orderRefundMapper);
        updateWrapper
                .eq(OrderRefund::getId, refundId)
                .set(OrderRefund::getStatus, WAIT_MERCHANT_RECEIVE)
                .set(OrderRefund::getReturnExpressCompany, expressCompany)
                .set(OrderRefund::getReturnWaybill, waybill)
                .update();

        redisUtil.changeRefundCache(
                refund.getOrderId().toString(),
                refund.getUserId().toString(),
                refund.getShopId().toString(),
                refund.getStatus(),
                WAIT_USER_RETURN
        );
        redisUtil.deleteRefundCache(refundId);
    }



    @Override
    @Transactional(readOnly = true)
    public List<AddressModifyRequestDTO> getAddressModifyRequestsForUser(Long userId, Integer status, Integer pageNum, Integer pageSize) {
        String statusKey;
        if(status>=ORDER_ADDRESS_MODIFY_STATUS_WAIT
                && status<=ORDER_ADDRESS_MODIFY_STATUS_CANCEL){
            statusKey = ORDER_CHANGE_ADDRESS_USER_REQUEST.format(userId, status);
        }else{
            throw new BusinessException(ORDER_ADDRESS_MODIFY_STATUS_ERROR);
        }
        
        int start = (pageNum - 1) * pageSize;
        int end = start + pageSize - 1;

        Set<String> orderIdsSet = stringRedisTemplate.opsForZSet().reverseRange(statusKey, start, end);
        if (orderIdsSet == null || orderIdsSet.isEmpty()) {
            int offset = (pageNum - 1) * pageSize;
            return orderAddressModifyMapper.selectAddressModifyRequestsForUser(userId, status, offset, pageSize);
        }
        
        List<String> orderIds = new ArrayList<>(orderIdsSet);
        if (orderIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 拼接所有订单地址修改申请key
        List<String> keys = new ArrayList<>(orderIds.size());
        for (String orderId : orderIds) {
            keys.add(ORDER_CHANGE_ADDRESS_DETAIL.format(orderId));
        }

        // 2. ================= 核心：Redis 管道批量执行 =================
        List<Object> pipelineResults = stringRedisTemplate.executePipelined((RedisCallback<?>)(connection) -> {
            for (String key : keys) {
                connection.hashCommands().hGetAll(key.getBytes(StandardCharsets.UTF_8));
            }
            return null;
        });

        // 3. 解析管道结果 + 处理缓存未命中
        // 3. 统计 Miss 并收集 Miss 的 ID
        List<String> missIds = new ArrayList<>();
        for (int i = 0; i < pipelineResults.size(); i++) {
            if (pipelineResults.get(i) == null || ((Map<?, ?>) pipelineResults.get(i)).isEmpty()) {
                missIds.add(orderIds.get(i));
            }
        }

        int missNum = missIds.size();
        int totalCount = pipelineResults.size();

        // 4. 【决策】Miss 率 >= 50%，整页降级
        if (missNum * 2 >= totalCount) {
            int offset = (pageNum - 1) * pageSize;
            return orderAddressModifyMapper.selectAddressModifyRequestsForUser(userId, status, offset, pageSize);
        }

        // 5. 【决策】小规模 Miss，批量查库回填
        Map<String, AddressModifyRequestDTO> missOrderMap = new HashMap<>();
        if (!missIds.isEmpty()) {
            // 关键点：一次性查出所有 Miss 的订单，避免 N+1 问题
            List<AddressModifyRequestDTO> dbOrders = orderAddressModifyMapper.selectAddressModifyRequestByIds(missIds);

            for (AddressModifyRequestDTO dto : dbOrders) {
                missOrderMap.put(dto.getOrderId().toString(), dto);
                redisUtil.setAddressModifyCache(dto); // 异步或同步回填
            }
        }

        // 6. 组装最终结果（保持 ZSet 顺序）
        List<AddressModifyRequestDTO> resultList = new ArrayList<>(totalCount);
        for (int i = 0; i < pipelineResults.size(); i++) {
            Object result = pipelineResults.get(i);
            String orderId = orderIds.get(i);

            if (result == null || ((Map<?, ?>) result).isEmpty()) {
                // 从批量查库的结果里取
                AddressModifyRequestDTO dbOrder = missOrderMap.get(orderId);
                if (dbOrder != null) {
                    resultList.add(dbOrder);
                }
                // 如果 dbOrder 也是 null，说明记录被删了，直接跳过，保证数据干净
            } else {
                try {
                    AddressModifyRequestDTO dto = redisUtil.getAddressModifyCache((Map<Object, Object>) result);
                    resultList.add(dto);
                } catch (Exception e) {
                    // 解析失败，跳过
                }
            }
        }
        return resultList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefundRequestSimpleDTO> getRefundRequestsForUser(Long userId, Integer status, Integer pageNum, Integer pageSize) {
        String statusKey;
        if(status>=WAIT_MERCHANT_AUDIT &&
                status<=MERCHANT_REJECT){
            statusKey = REFUND_USER_LIST.format(userId, status);
        }else {
            throw new BusinessException(ORDER_REFUND_STATUS_ERROR);
        }
        
        int start = (pageNum - 1) * pageSize;
        int end = start + pageSize - 1;
        

        Set<String> refundIdsSet = stringRedisTemplate.opsForZSet().reverseRange(statusKey, start, end);
        if (refundIdsSet == null || refundIdsSet.isEmpty()) {
            int offset = (pageNum - 1) * pageSize;
            return orderRefundMapper.selectRefundRequestsForUser(userId, status, offset, pageSize);
        }
        
        List<String> orderIds = new ArrayList<>(refundIdsSet);
        if (orderIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 拼接所有退款申请key
        List<String> keys = new ArrayList<>(orderIds.size());
        for (String orderId : orderIds) {
            keys.add(REFUND_DETAIL.format(orderId));
        }

        // 2. ================= 核心：Redis 管道批量执行 =================
        List<Object> pipelineResults = stringRedisTemplate.executePipelined((RedisCallback<?>)(connection) -> {
            for (String key : keys) {
                connection.hashCommands().hGetAll(key.getBytes(StandardCharsets.UTF_8));
            }
            return null;
        });

        // 3. 解析管道结果 + 处理缓存未命中
        // 3. 统计 Miss 并收集 Miss 的 ID
        List<Long> missIds = new ArrayList<>();
        for (int i = 0; i < pipelineResults.size(); i++) {
            if (pipelineResults.get(i) == null || ((Map<?, ?>) pipelineResults.get(i)).isEmpty()) {
                missIds.add(Long.valueOf(orderIds.get(i)));
            }
        }

        int missNum = missIds.size();
        int totalCount = pipelineResults.size();

        // 4. 【决策】Miss 率 >= 50%，整页降级
        if (missNum * 2 >= totalCount) {
            int offset = (pageNum - 1) * pageSize;
            return orderRefundMapper.selectRefundRequestsForUser(userId, status, offset, pageSize);
        }

        // 5. 【决策】小规模 Miss，批量查库回填
        Map<Long, RefundRequestSimpleDTO> missRefundMap = new HashMap<>();
        if (!missIds.isEmpty()) {
            // 关键点：一次性查出所有 Miss 的退款，避免 N+1 问题
            List<RefundRequestSimpleDTO> dbRefunds = orderRefundMapper.selectRefundRequestByIds(missIds);

            for (RefundRequestSimpleDTO dto : dbRefunds) {
                missRefundMap.put(dto.getId(), dto);
                redisUtil.setRefundCache(dto); // 异步或同步回填
            }
        }

        // 6. 组装最终结果（保持 ZSet 顺序）
        List<RefundRequestSimpleDTO> resultList = new ArrayList<>(totalCount);
        for (int i = 0; i < pipelineResults.size(); i++) {
            Object result = pipelineResults.get(i);
            String orderId = orderIds.get(i);

            if (result == null || ((Map<?, ?>) result).isEmpty()) {
                // 从批量查库的结果里取
                RefundRequestSimpleDTO dbRefund = missRefundMap.get(Long.valueOf(orderId));
                if (dbRefund != null) {
                    resultList.add(dbRefund);
                }
                // 如果 dbRefund 也是 null，说明记录被删了，直接跳过，保证数据干净
            } else {
                try {
                    RefundRequestSimpleDTO dto = redisUtil.getRefundCache((Map<Object, Object>) result);
                    resultList.add(dto);
                } catch (Exception e) {
                    // 解析失败，跳过
                }
            }
        }
        return resultList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressModifyRequestDTO> getAddressModifyRequestsForShop(Long shopId, Integer status, Integer pageNum, Integer pageSize) {
        String statusKey;
        if(status>=ORDER_ADDRESS_MODIFY_STATUS_WAIT
                && status<=ORDER_ADDRESS_MODIFY_STATUS_CANCEL){
            statusKey = ORDER_CHANGE_ADDRESS_SHOP_REQUEST.format(shopId, status);
        }else{
            throw new BusinessException(ORDER_ADDRESS_MODIFY_STATUS_ERROR);
        }
        
        int start = (pageNum - 1) * pageSize;
        int end = start + pageSize - 1;
        
        /**
         * 这里的策略是id的缓存是半年，如果说id缓存过期了那么detail基本上也全部过期了
         * 所以如果缓存id没有的话直接用数据库查询分页
         * 因为距离下单过了半年了所以不补充缓存
         * */
        Set<String> orderIdsSet = stringRedisTemplate.opsForZSet().reverseRange(statusKey, start, end);
        if (orderIdsSet == null || orderIdsSet.isEmpty()) {
            int offset = (pageNum - 1) * pageSize;
            return orderAddressModifyMapper.selectAddressModifyRequestsForShop(shopId, status, offset, pageSize);
        }
        
        List<String> orderIds = new ArrayList<>(orderIdsSet);
        if (orderIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 拼接所有订单地址修改申请key
        List<String> keys = new ArrayList<>(orderIds.size());
        for (String orderId : orderIds) {
            keys.add(ORDER_CHANGE_ADDRESS_DETAIL.format(orderId));
        }

        // 2. ================= 核心：Redis 管道批量执行 =================
        List<Object> pipelineResults = stringRedisTemplate.executePipelined((RedisCallback<?>)(connection) -> {
            for (String key : keys) {
                connection.hashCommands().hGetAll(key.getBytes(StandardCharsets.UTF_8));
            }
            return null;
        });

        // 3. 解析管道结果 + 处理缓存未命中
        // 3. 统计 Miss 并收集 Miss 的 ID
        List<String> missIds = new ArrayList<>();
        for (int i = 0; i < pipelineResults.size(); i++) {
            if (pipelineResults.get(i) == null || ((Map<?, ?>) pipelineResults.get(i)).isEmpty()) {
                missIds.add(orderIds.get(i));
            }
        }

        int missNum = missIds.size();
        int totalCount = pipelineResults.size();

        // 4. 【决策】Miss 率 >= 50%，整页降级
        if (missNum * 2 >= totalCount) {
            int offset = (pageNum - 1) * pageSize;
            return orderAddressModifyMapper.selectAddressModifyRequestsForShop(shopId, status, offset, pageSize);
        }

        // 5. 【决策】小规模 Miss，批量查库回填
        Map<String, AddressModifyRequestDTO> missOrderMap = new HashMap<>();
        if (!missIds.isEmpty()) {
            // 关键点：一次性查出所有 Miss 的订单，避免 N+1 问题
            List<AddressModifyRequestDTO> dbOrders = orderAddressModifyMapper.selectAddressModifyRequestByIds(missIds);

            for (AddressModifyRequestDTO dto : dbOrders) {
                missOrderMap.put(dto.getOrderId().toString(), dto);
                redisUtil.setAddressModifyCache(dto); // 异步或同步回填
            }
        }

        // 6. 组装最终结果（保持 ZSet 顺序）
        List<AddressModifyRequestDTO> resultList = new ArrayList<>(totalCount);
        for (int i = 0; i < pipelineResults.size(); i++) {
            Object result = pipelineResults.get(i);
            String orderId = orderIds.get(i);

            if (result == null || ((Map<?, ?>) result).isEmpty()) {
                // 从批量查库的结果里取
                AddressModifyRequestDTO dbOrder = missOrderMap.get(orderId);
                if (dbOrder != null) {
                    resultList.add(dbOrder);
                }
                // 如果 dbOrder 也是 null，说明记录被删了，直接跳过，保证数据干净
            } else {
                try {
                    AddressModifyRequestDTO dto = redisUtil.getAddressModifyCache((Map<Object, Object>) result);
                    resultList.add(dto);
                } catch (Exception e) {
                    // 解析失败，跳过
                }
            }
        }
        return resultList;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefundRequestSimpleDTO> getRefundRequestsForShop(Long shopId, Integer status, Integer pageNum, Integer pageSize) {

        
        int start = (pageNum - 1) * pageSize;
        int end = start + pageSize - 1;
        String statusKey;
        if(status>=WAIT_MERCHANT_AUDIT &&
                status<=MERCHANT_REJECT){
            statusKey = REFUND_SHOP_LIST.format(shopId, status);
        }else {
            throw new BusinessException(ORDER_REFUND_STATUS_ERROR);
        }
        /**
         * 这里的策略是id的缓存是半年，如果说id缓存过期了那么detail基本上也全部过期了
         * 所以如果缓存id没有的话直接用数据库查询分页
         * 因为距离下单过了半年了所以不补充缓存
         * */
        Set<String> refundIdsSet = stringRedisTemplate.opsForZSet().reverseRange(statusKey, start, end);
        if (refundIdsSet == null || refundIdsSet.isEmpty()) {
            int offset = (pageNum - 1) * pageSize;
            return orderRefundMapper.selectRefundRequestsForShop(shopId, status, offset, pageSize);
        }
        
        List<String> refundIds = new ArrayList<>(refundIdsSet);
        if (refundIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 拼接所有退款申请key
        List<String> keys = new ArrayList<>(refundIds.size());
        for (String orderId : refundIds) {
            keys.add(REFUND_DETAIL.format(orderId));
        }

        // 2. ================= 核心：Redis 管道批量执行 =================
        List<Object> pipelineResults = stringRedisTemplate.executePipelined((RedisCallback<?>)(connection) -> {
            for (String key : keys) {
                connection.hashCommands().hGetAll(key.getBytes(StandardCharsets.UTF_8));
            }
            return null;
        });

        // 3. 解析管道结果 + 处理缓存未命中
        // 3. 统计 Miss 并收集 Miss 的 ID
        List<Long> missIds = new ArrayList<>();
        for (int i = 0; i < pipelineResults.size(); i++) {
            if (pipelineResults.get(i) == null || ((Map<?, ?>) pipelineResults.get(i)).isEmpty()) {
                missIds.add(Long.valueOf(refundIds.get(i)));
            }
        }

        int missNum = missIds.size();
        int totalCount = pipelineResults.size();

        // 4. 【决策】Miss 率 >= 50%，整页降级
        if (missNum * 2 >= totalCount) {
            int offset = (pageNum - 1) * pageSize;
            return orderRefundMapper.selectRefundRequestsForShop(shopId, status, offset, pageSize);
        }

        // 5. 【决策】小规模 Miss，批量查库回填
        Map<Long, RefundRequestSimpleDTO> missRefundMap = new HashMap<>();
        if (!missIds.isEmpty()) {
            // 关键点：一次性查出所有 Miss 的退款，避免 N+1 问题
            List<RefundRequestSimpleDTO> dbRefunds = orderRefundMapper.selectRefundRequestByIds(missIds);

            for (RefundRequestSimpleDTO dto : dbRefunds) {
                missRefundMap.put(dto.getId(), dto);
                redisUtil.setRefundCache(dto); // 异步或同步回填
            }
        }

        // 6. 组装最终结果（保持 ZSet 顺序）
        List<RefundRequestSimpleDTO> resultList = new ArrayList<>(totalCount);
        for (int i = 0; i < pipelineResults.size(); i++) {
            Object result = pipelineResults.get(i);
            String refundId = refundIds.get(i);

            if (result == null || ((Map<?, ?>) result).isEmpty()) {
                // 从批量查库的结果里取
                RefundRequestSimpleDTO dbRefund = missRefundMap.get(Long.valueOf(refundId));
                if (dbRefund != null) {
                    resultList.add(dbRefund);
                }
                // 如果 dbRefund 也是 null，说明记录被删了，直接跳过，保证数据干净
            } else {
                try {
                    resultList.add(redisUtil.getRefundCache((Map<Object, Object>) result));
                } catch (Exception e) {
                    // 解析失败，跳过
                }
            }
        }
        return resultList;
    }

    @Override
    @Transactional(readOnly = true)
    public RefundRequestDetailDTO getRefundRequestDetailById(Long orderId) {
        Long userId=UserUtil.getLocalUserId();
        if(userId==null){
            throw new BusinessException(NOT_LOGIN);
        }
        LambdaQueryWrapper<OrderRefund> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(OrderRefund::getOrderId,orderId)
                .eq(OrderRefund::getUserId,userId);
        OrderRefund orderRefund = orderRefundMapper.selectOne(queryWrapper);
        return orderRefund==null?null:orderRefundToRefundDetailDto.orderRefundToRefundRequestDetailDto(orderRefund);

    }

    @Override
    @Transactional(readOnly = true)
    public com.daxi.domain.dto.PaymentPageDTO getPaymentPage(Long orderId) {
        Long userId = UserUtil.getLocalUserId();
        if (userId == null) {
            throw new BusinessException(NOT_LOGIN);
        }

        LambdaQueryWrapper<UserOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(UserOrder::getOrderId, orderId)
                .eq(UserOrder::getUserId, userId)
                .select(
                        UserOrder::getOrderSn,
                        UserOrder::getPayStatus,
                        UserOrder::getCreateTime,
                        UserOrder::getPrice
                );
        
        UserOrder order = userOrderMapper.selectOne(queryWrapper);
        if (order == null) {
            throw new BusinessException(PAYMENT_ORDER_NOT_EXIST);
        }

        if (order.getPayStatus() != null && order.getPayStatus() == PAY_STATUS_PAID) {
            throw new BusinessException(PAYMENT_ALREADY_PAID);
        }

        LocalDateTime createTime = order.getCreateTime();
        if (createTime != null && 
            createTime.plusMinutes(PAYMENT_EXPIRE_MINUTES).isBefore(LocalDateTime.now())) {
            throw new BusinessException(PAYMENT_EXPIRED);
        }

        //添加缓存,时间比支付允许的时间略长一些，防止关单之前再次获取支付连接从而导致多次付款
        stringRedisTemplate.opsForValue().set(
                ORDER_PAY_LOCK.format(orderId),
                String.valueOf(OrderLimit.PAY_LOCK_ON_STATUS),
                PAY_TIME_MINUTES+PAY_TIME_CACHE_MINUTES,
                TimeUnit.MINUTES);
        return payUtil.generatePaymentPage(
                order.getOrderId(),
                order.getOrderSn(),
                order.getPrice()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public com.daxi.domain.dto.PaymentStatusDTO checkPaymentStatus(Long orderId) {
       LambdaQueryWrapper<UserOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(UserOrder::getOrderId, orderId)
                .select(
                        UserOrder::getPayStatus,
                        UserOrder::getPaySn,
                        UserOrder::getPayTime
                        );
        UserOrder userOrder = userOrderMapper.selectOne(queryWrapper);
        if (userOrder == null) {
            throw new BusinessException(PAYMENT_ORDER_NOT_EXIST);
        }
        PaymentStatusDTO paymentStatusDTO = new PaymentStatusDTO();
        paymentStatusDTO.setOrderId(orderId);

        if(userOrder.getPayStatus()==PAY_NO_STATUS){
            PaymentStatusDTO queryPaymentStatusDTO = payUtil.queryPaymentStatus(orderId);
            if(queryPaymentStatusDTO== null||
                    queryPaymentStatusDTO.getPayStatus()==PAY_NO_STATUS){
                paymentStatusDTO.setPayStatus(PAY_NO_STATUS);
                paymentStatusDTO.setMessage(PAYMENT_NOT_PAID);
                return paymentStatusDTO;
            }
            return queryPaymentStatusDTO;
        }
        paymentStatusDTO.setPayStatus(userOrder.getPayStatus());
        paymentStatusDTO.setPaySn(userOrder.getPaySn());
        paymentStatusDTO.setPayTime(userOrder.getPayTime());
        paymentStatusDTO.setMessage(PAYMENT_ALREADY_PAID);
        return paymentStatusDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatusToPaySuccess(PaymentAO pay) {
        Long userId = UserUtil.getLocalUserId();
        if (userId == null) {
            throw new BusinessException(NOT_LOGIN);
        }
        //第三方服务自会用orderId帮我校验幂等，肯定不会重复一个id调用这个接口
        //也就意味着订单肯定没支付
        LambdaQueryWrapper<UserOrder> queryWrapper = new LambdaQueryWrapper<>();
        Long orderId = pay.getOrderId();
        queryWrapper
                .eq(UserOrder::getOrderId, orderId)
                .eq(UserOrder::getUserId, userId)
                .select(UserOrder::getShopId);

        UserOrder order = userOrderMapper.selectOne(queryWrapper);
        if (order == null) {
            throw new BusinessException(PAYMENT_ORDER_NOT_EXIST);
        }

        LambdaUpdateChainWrapper<UserOrder> updateWrapper = new LambdaUpdateChainWrapper<>(userOrderMapper);
        updateWrapper
                .eq(UserOrder::getOrderId, orderId)
                .eq(UserOrder::getUserId, userId)
                .set(UserOrder::getPayStatus, PAY_STATUS_PAID)
                .set(UserOrder::getPaySn, pay.getPaySn())
                .set(UserOrder::getPayTime, pay.getPayTime())
                .set(UserOrder::getOrderStatus, ORDER_STATUS_PENDING_SHIPMENT)
                .update();

        String shopStatusKey = ORDER_SHOP_STATUS.format(order.getShopId(), order.getOrderStatus());
        String userStatusKey = ORDER_USER_STATUS.format(order.getUserId(), order.getOrderStatus());
        String newUserStatusKey = ORDER_USER_STATUS.format(order.getUserId(), ORDER_STATUS_PENDING_SHIPMENT);
        byte[] bytes = orderId.toString().getBytes(StandardCharsets.UTF_8);
        stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            connection.zSetCommands().zAdd(shopStatusKey.getBytes(StandardCharsets.UTF_8), System.currentTimeMillis(), bytes);
            connection.zSetCommands().zRem(userStatusKey.getBytes(StandardCharsets.UTF_8), bytes);
            connection.zSetCommands().zAdd(newUserStatusKey.getBytes(StandardCharsets.UTF_8), System.currentTimeMillis(), bytes);
            byte[] ttlBytes = String.valueOf(ORDERID_EXPIRE_SECONDS).getBytes(StandardCharsets.UTF_8);
            connection.execute("EXPIRE",
                    shopStatusKey.getBytes(StandardCharsets.UTF_8),
                    ttlBytes);

            connection.execute("EXPIRE",
                    newUserStatusKey.getBytes(StandardCharsets.UTF_8),
                    ttlBytes);
            return null;
        });
        redisUtil.deleteDetailOrderCache(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public SendCommentDTO getInformationForSendComment(Long userId, Long orderId, Long spuId, Long skuId) {
        return userOrderMapper.getInformationForSendComment(userId, orderId, spuId, skuId);
    }


}
