package com.daxi.mq.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.daxi.constants.RocketMQConstants;
import com.daxi.domain.dto.AddressModifyTimeoutMessageDTO;
import com.daxi.domain.entity.OrderAddressModify;
import com.daxi.domain.entity.UserOrder;
import com.daxi.mapper.order.OrderAddressModifyMapper;
import com.daxi.mapper.order.UserOrderMapper;
import com.daxi.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import static com.daxi.limit.OrderLimit.OPERATE_STATUS_NONE;
import static com.daxi.limit.OrderLimit.ORDER_ADDRESS_MODIFY_STATUS_EXPIRE;
import static com.daxi.limit.OrderLimit.ORDER_ADDRESS_MODIFY_STATUS_WAIT;

/**
 * 地址修改超时消息消费者
 * 收到延时消息后检查地址修改申请状态，如果超时未处理则自动关闭申请
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = RocketMQConstants.TOPIC_ADDRESS_MODIFY,
        selectorExpression = RocketMQConstants.TAG_ADDRESS_MODIFY_TIMEOUT,
        consumerGroup = "address-modify-timeout-consumer-group",
        consumeMode = org.apache.rocketmq.spring.annotation.ConsumeMode.CONCURRENTLY,
        messageModel = org.apache.rocketmq.spring.annotation.MessageModel.CLUSTERING
)
@RequiredArgsConstructor
public class AddressModifyTimeoutConsumer implements RocketMQListener<AddressModifyTimeoutMessageDTO> {
    
    private final OrderAddressModifyMapper orderAddressModifyMapper;
    private final UserOrderMapper userOrderMapper;
    private final RedisUtil redisUtil;
    
    @Override
    public void onMessage(AddressModifyTimeoutMessageDTO message) {
        try {
            log.info("收到地址修改超时检查消息, orderId: {}, userId: {}, shopId: {}", 
                    message.getOrderId(),
                    message.getUserId(), 
                    message.getShopId());
            
            // 查询地址修改申请当前状态
            LambdaQueryWrapper<OrderAddressModify> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OrderAddressModify::getOrderId, message.getOrderId())
                    .select(OrderAddressModify::getOrderId, OrderAddressModify::getStatus);
            
            OrderAddressModify addressModify = orderAddressModifyMapper.selectOne(queryWrapper);
            
            if (addressModify == null) {
                log.warn("地址修改申请不存在，跳过超时处理, orderId: {}", message.getOrderId());
                return;
            }


            // 检查地址修改状态是否为待审核
            if (addressModify.getStatus() != ORDER_ADDRESS_MODIFY_STATUS_WAIT) {
                log.info("地址修改申请状态不需要超时处理, orderId: {}, status: {}", 
                        message.getOrderId(), addressModify.getStatus());
                return;
            }
            
            // 超时关闭地址修改申请
            expireAddressModify(addressModify.getOrderId(), message.getUserId(), message.getShopId());

            log.info("地址修改申请超时自动关闭成功, orderId: {}", message.getOrderId());
            
        } catch (Exception e) {
            log.error("地址修改超时检查处理失败, orderId: {}", message.getOrderId(), e);
            // 抛出异常会触发重试
            throw new RuntimeException("处理地址修改超时消息失败", e);
        }
    }
    
    /**
     * 超时关闭地址修改申请
     */
    private void expireAddressModify(Long orderId, Long userId, Long shopId) {
        // 更新地址修改状态为已过期
        LambdaUpdateChainWrapper<OrderAddressModify> modifyUpdateWrapper = new LambdaUpdateChainWrapper<>(orderAddressModifyMapper);
        boolean modifyUpdated = modifyUpdateWrapper
                .eq(OrderAddressModify::getOrderId, orderId)
                .set(OrderAddressModify::getStatus, ORDER_ADDRESS_MODIFY_STATUS_EXPIRE)
                .update();
        
        if (modifyUpdated) {
            log.info("地址修改状态已更新为已过期, orderId: {}", orderId);
            
            // 更新订单操作状态
            LambdaUpdateChainWrapper<UserOrder> orderUpdateWrapper = new LambdaUpdateChainWrapper<>(userOrderMapper);
            orderUpdateWrapper
                    .eq(UserOrder::getOrderId, orderId)
                    .set(UserOrder::getOperateStatus, OPERATE_STATUS_NONE)
                    .update();

            // 更新 Redis 缓存
            try {
                redisUtil.changeAddressRequestCache(
                        orderId.toString(),
                        userId.toString(),
                        shopId.toString(),
                        ORDER_ADDRESS_MODIFY_STATUS_WAIT,
                        ORDER_ADDRESS_MODIFY_STATUS_EXPIRE
                );
                redisUtil.deleteAddressModifyCache(orderId.toString());
                log.info("地址修改缓存已更新, orderId: {}", orderId);
            } catch (Exception e) {
                log.error("更新地址修改缓存失败, orderId: {}", orderId, e);
            }
        } else {
            log.warn("地址修改状态更新失败，可能申请已被处理, orderId: {}", orderId);
        }
    }
}
