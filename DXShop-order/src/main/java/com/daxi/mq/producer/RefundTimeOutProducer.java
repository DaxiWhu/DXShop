package com.daxi.mq.producer;

import com.daxi.constants.RocketMQConstants;
import com.daxi.domain.dto.RefundTimeOutMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefundTimeOutProducer {
    private final RocketMQTemplate rocketMQTemplate;
    public void sendRefundTimeOutMessage(RefundTimeOutMessage refundTimeOutMessage, int delayLevel) {
        try {
            String destination = RocketMQConstants.TOPIC_REFUND + ":" + RocketMQConstants.TAG_REFUND_TIMEOUT;

            Message<RefundTimeOutMessage> springMessage = MessageBuilder
                    .withPayload(refundTimeOutMessage)
                    .setHeader("KEYS", String.valueOf(refundTimeOutMessage.getOrderId()))
                    .build();

            // 延时级别 30s
            SendResult sendResult = rocketMQTemplate.syncSend(destination, springMessage, 3000, delayLevel);
            log.info("退款超时检查延时消息发送成功, orderSn: {}, msgId: {}, delayLevel: {}",
                    refundTimeOutMessage.getOrderId(), sendResult.getMsgId(), delayLevel);
        }catch (Exception e){
            log.error("退款超时检查延时消息发送失败, orderSn: {}", refundTimeOutMessage.getOrderId(), e);
        }
    }
}
