package com.daxi.service;

import com.daxi.domain.entity.MqDeadLetterMessage;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * MQ告警服务
 * 负责死信消息的记录和告警通知
 */
public interface MqAlertService {
    
    /**
     * 记录死信消息并发送告警
     * @param messageExt RocketMQ原始消息
     * @param consumerGroup 消费者组
     * @param businessType 业务类型
     * @param errorMessage 错误信息
     */
    void recordAndAlert(MessageExt messageExt, String consumerGroup, String businessType, String errorMessage);
    
    /**
     * 记录死信消息并发送告警（简化版）
     * @param deadLetterMessage 死信消息对象
     */
    void recordAndAlert(MqDeadLetterMessage deadLetterMessage);
    
    /**
     * 发送钉钉告警
     * @param title 告警标题
     * @param content 告警内容
     */
    void sendDingTalkAlert(String title, String content);
    
    /**
     * 获取未处理的死信消息数量
     * @return 未处理数量
     */
    long getUnprocessedCount();
}
