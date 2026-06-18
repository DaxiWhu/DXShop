package com.daxi.mq.producer;

import com.daxi.constants.RocketMQConstants;
import com.daxi.domain.dto.OrderTimeoutMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * 订单超时消息生产者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutProducer {
    
    private final RocketMQTemplate rocketMQTemplate;
    
    /**
     * 发送订单超时检查延时消息
     * @param message 消息内容
     * @param delayLevel 延迟级别 (1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h)
     *                   对应级别 1-18
     *                   30分钟对应级别 16
     */
    public void sendOrderTimeoutMessage(OrderTimeoutMessageDTO message, int delayLevel) {
        try {
            String destination = RocketMQConstants.TOPIC_ORDER + ":" + RocketMQConstants.TAG_ORDER_TIMEOUT;
            
            Message<OrderTimeoutMessageDTO> springMessage = MessageBuilder
                    .withPayload(message)
                    .setHeader("KEYS", String.valueOf(message.getOrderId()))
                    .build();
            
            // 发送延时消息
            SendResult sendResult = rocketMQTemplate.syncSend(destination, springMessage, 3000, delayLevel);
            
            log.info("订单超时检查延时消息发送成功, orderSn: {}, msgId: {}, delayLevel: {}", 
                    message.getOrderId(), sendResult.getMsgId(), delayLevel);
        } catch (Exception e) {
            log.error("订单超时检查延时消息发送失败, orderSn: {}", message.getOrderId(), e);
            // 注意：这里不抛出异常，避免影响订单创建主流程
        }
    }
}
