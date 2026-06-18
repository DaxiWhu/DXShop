package com.daxi.mq.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.daxi.constants.RocketMQConstants;
import com.daxi.domain.dto.RefundTimeOutMessage;
import com.daxi.domain.entity.OrderRefund;
import com.daxi.domain.entity.UserOrder;
import com.daxi.mapper.order.OrderRefundMapper;
import com.daxi.mapper.order.UserOrderMapper;
import com.daxi.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import static com.daxi.limit.OrderLimit.OPERATE_STATUS_NONE;
import static com.daxi.limit.OrderLimit.REFUND_EXPIRE;
import static com.daxi.limit.OrderLimit.WAIT_MERCHANT_AUDIT;

/**
 * 退款超时消息消费者
 * 收到延时消息后检查退款申请状态，如果超时未处理则自动关闭退款申请
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = RocketMQConstants.TOPIC_REFUND,
        selectorExpression = RocketMQConstants.TAG_REFUND_TIMEOUT,
        consumerGroup = "refund-timeout-consumer-group",
        consumeMode = org.apache.rocketmq.spring.annotation.ConsumeMode.CONCURRENTLY,
        messageModel = org.apache.rocketmq.spring.annotation.MessageModel.CLUSTERING
)
@RequiredArgsConstructor
public class RefundTimeOutComsumer implements RocketMQListener<RefundTimeOutMessage> {
    
    private final OrderRefundMapper orderRefundMapper;
    private final UserOrderMapper userOrderMapper;
    private final RedisUtil redisUtil;
    
    @Override
    public void onMessage(RefundTimeOutMessage message) {
        try {
            log.info("收到退款超时检查消息, orderId: {}, userId: {}, shopId: {}",
                    message.getOrderId(),
                    message.getUserId(), 
                    message.getShopId());

            // 查询退款申请当前状态
            LambdaQueryWrapper<OrderRefund> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper
                    .eq(OrderRefund::getOrderId, message.getOrderId())
                    .select(OrderRefund::getId, OrderRefund::getStatus);

            OrderRefund refund = orderRefundMapper.selectOne(queryWrapper);

            if (refund == null) {
                log.warn("退款申请不存在，跳过超时处理, orderId: {}", message.getOrderId());
                return;
            }

            // 检查退款状态是否为待审核或待用户退货
            if (refund.getStatus() != WAIT_MERCHANT_AUDIT) {
                log.info("退款申请状态不需要超时处理, refundId: {}, status: {}",
                        message.getOrderId(), refund.getStatus());
                return;
            }
            
            // 超时关闭退款申请
            expireRefund(refund.getId(), message.getOrderId(), message.getUserId(), message.getShopId());

            log.info("退款申请超时自动关闭成功, orderId: {}",
                    message.getOrderId());
            
        } catch (Exception e) {
            log.error("退款超时检查处理失败, orderId: {}",
                    message.getOrderId(), e);
            // 抛出异常会触发重试
            throw new RuntimeException("处理退款超时消息失败", e);
        }
    }
    
    /**
     * 超时关闭退款申请
     */
    private void expireRefund(Long refundId, Long orderId, Long userId, Long shopId) {
        // 更新退款状态为已过期
        LambdaUpdateChainWrapper<OrderRefund> refundUpdateWrapper = new LambdaUpdateChainWrapper<>(orderRefundMapper);
        boolean refundUpdated = refundUpdateWrapper
                .eq(OrderRefund::getId, refundId)
                .set(OrderRefund::getStatus, REFUND_EXPIRE)
                .update();
        
        if (refundUpdated) {
            log.info("退款状态已更新为已过期, refundId: {}", refundId);

            // 更新订单操作状态
            LambdaUpdateChainWrapper<UserOrder> orderUpdateWrapper = new LambdaUpdateChainWrapper<>(userOrderMapper);
            orderUpdateWrapper
                    .eq(UserOrder::getOrderId, orderId)
                    .set(UserOrder::getOperateStatus, OPERATE_STATUS_NONE)
                    .update();

            // 更新 Redis 缓存
            try {
                redisUtil.changeRefundCache(
                        orderId.toString(),
                        userId.toString(),
                        shopId.toString(),
                        WAIT_MERCHANT_AUDIT,
                        REFUND_EXPIRE
                );
                redisUtil.deleteRefundCache(refundId);
                log.info("退款缓存已更新, refundId: {}", refundId);
            } catch (Exception e) {
                log.error("更新退款缓存失败, refundId: {}", refundId, e);
            }
        } else {
            log.warn("退款状态更新失败，可能退款已被处理, refundId: {}", refundId);
        }
    }
}
