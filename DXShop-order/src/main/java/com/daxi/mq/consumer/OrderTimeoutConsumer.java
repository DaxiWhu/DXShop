package com.daxi.mq.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.daxi.constants.RocketMQConstants;
import com.daxi.domain.dto.OrderTimeoutMessageDTO;
import com.daxi.domain.entity.UserOrder;
import com.daxi.limit.OrderLimit;
import com.daxi.mapper.order.UserOrderMapper;
import com.daxi.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 订单超时消息消费者
 * 收到延时消息后检查订单支付状态，如果未支付则取消订单
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = RocketMQConstants.TOPIC_ORDER,
        selectorExpression = RocketMQConstants.TAG_ORDER_TIMEOUT,
        consumerGroup = "order-timeout-consumer-group",
        consumeMode = org.apache.rocketmq.spring.annotation.ConsumeMode.CONCURRENTLY,
        messageModel = org.apache.rocketmq.spring.annotation.MessageModel.CLUSTERING
)
@RequiredArgsConstructor
public class OrderTimeoutConsumer implements RocketMQListener<OrderTimeoutMessageDTO> {
    
    private final UserOrderMapper userOrderMapper;
    private final RedisUtil redisUtil;
    
    @Override
    public void onMessage(OrderTimeoutMessageDTO message) {
        try {
            log.info("收到订单超时检查消息, orderSn: {}, userId: {}, shopId: {}", 
                    message.getOrderId(),
                    message.getUserId(), 
                    message.getShopId());
            
            // 查询订单当前状态
            LambdaQueryWrapper<UserOrder> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserOrder::getOrderSn, String.valueOf(message.getOrderId()))
                    .select(UserOrder::getOrderId, UserOrder::getOrderStatus);
            
            UserOrder order = userOrderMapper.selectOne(queryWrapper);
            
            if (order == null) {
                log.warn("订单不存在，跳过超时处理, orderSn: {}", message.getOrderId());
                return;
            }

            // 检查订单状态是否为待支付
            if (order.getOrderStatus() == null || order.getOrderStatus() != OrderLimit.ORDER_STATUS_PENDING_PAYMENT) {
                log.info("订单状态不是待支付，无需取消, orderSn: {}, orderStatus: {}", 
                        message.getOrderId(), order.getOrderStatus());
                return;
            }
            
            // 取消订单
            cancelOrder(order.getOrderId(), message.getUserId(), message.getShopId(), message.getOrderId());

            log.info("订单超时自动取消成功, orderSn: {}", message.getOrderId());
            
        } catch (Exception e) {
            log.error("订单超时检查处理失败, orderSn: {}", message.getOrderId(), e);
            // 抛出异常会触发重试
            throw new RuntimeException("处理订单超时消息失败", e);
        }
    }
    
    /**
     * 取消订单
     */
    private void cancelOrder(Long orderId, Long userId, Long shopId, Long orderSn) {
        // 更新订单状态为已取消
        LambdaUpdateChainWrapper<UserOrder> updateWrapper = new LambdaUpdateChainWrapper<>(userOrderMapper);
        boolean updated = updateWrapper
                .eq(UserOrder::getOrderId, orderId)
                .set(UserOrder::getOrderStatus, OrderLimit.ORDER_STATUS_CANCELLED)
                .update();
        
        if (updated) {
            log.info("订单状态已更新为已取消, orderId: {}, orderSn: {}", orderId, orderSn);
            
            // 删除 Redis 缓存
            try {
                redisUtil.deleteOrderCache(String.valueOf(orderId), String.valueOf(userId));
                log.info("订单缓存已删除, orderId: {}", orderId);
            } catch (Exception e) {
                log.error("删除订单缓存失败, orderId: {}", orderId, e);
            }
            
            // TODO: 这里可以添加其他业务逻辑
            // 1. 恢复库存（如果需要）
            //先数据库后redis
            // 2. 发送取消通知给用户
            // 3. 记录订单取消日志
        } else {
            log.warn("订单状态更新失败，可能订单已被处理, orderId: {}, orderSn: {}", orderId, orderSn);
        }
    }
}
