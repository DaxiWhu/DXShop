package com.daxi.mq.producer;

import com.daxi.constants.RocketMQConstants;
import com.daxi.domain.dto.AddressModifyTimeoutMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * 地址修改超时消息生产者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AddressModifyTimeoutProducer {
    
    private final RocketMQTemplate rocketMQTemplate;
    
    /**
     * 发送地址修改超时检查延时消息
     * @param message 消息内容
     * @param delayLevel 延迟级别 (1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h)
     *                   对应级别 1-18
     *                   3小时对应需要自定义延迟或使用定时任务
     */
    public void sendAddressModifyTimeoutMessage(AddressModifyTimeoutMessageDTO message, int delayLevel) {
        try {
            String destination = RocketMQConstants.TOPIC_ADDRESS_MODIFY + ":" + RocketMQConstants.TAG_ADDRESS_MODIFY_TIMEOUT;
            
            Message<AddressModifyTimeoutMessageDTO> springMessage = MessageBuilder
                    .withPayload(message)
                    .setHeader("KEYS", String.valueOf(message.getOrderId()))
                    .build();
            
            // 发送延时消息
            SendResult sendResult = rocketMQTemplate.syncSend(destination, springMessage, 3000, delayLevel);
            
            log.info("地址修改超时检查延时消息发送成功, orderId: {}, msgId: {}, delayLevel: {}", 
                    message.getOrderId(), sendResult.getMsgId(), delayLevel);
        } catch (Exception e) {
            log.error("地址修改超时检查延时消息发送失败, orderId: {}", message.getOrderId(), e);
            // 注意：这里不抛出异常，避免影响地址修改申请主流程
        }
    }
}
