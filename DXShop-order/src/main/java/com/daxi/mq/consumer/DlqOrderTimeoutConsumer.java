package com.daxi.mq.consumer;

import com.daxi.service.MqAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/**
 * 订单超时死信消息消费者
 * 监听进入死信队列的订单超时消息，记录并告警
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = "%DLQ%order-timeout-consumer-group",
        consumerGroup = "dlq-order-timeout-consumer-group",
        consumeMode = org.apache.rocketmq.spring.annotation.ConsumeMode.CONCURRENTLY,
        messageModel = org.apache.rocketmq.spring.annotation.MessageModel.CLUSTERING
)
public class DlqOrderTimeoutConsumer implements RocketMQListener<MessageExt> {
    
    private final MqAlertService mqAlertService;
    
    @Override
    public void onMessage(MessageExt message) {
        log.error("【严重】订单超时消息进入死信队列, msgId: {}, keys: {}, body: {}", 
                message.getMsgId(), 
                message.getKeys(),
                new String(message.getBody()));
        
        // 记录死信消息并发送告警
        mqAlertService.recordAndAlert(
                message, 
                "order-timeout-consumer-group",
                "ORDER_TIMEOUT",
                "订单超时处理失败，重试16次后进入死信队列，需要人工介入处理"
        );
        
        // 注意：这里不抛出异常，避免死信消息无限循环
        // 死信消息只需要记录和告警，不需要再重试
    }
}
