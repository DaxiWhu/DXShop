package com.daxi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.daxi.domain.entity.MqDeadLetterMessage;
import com.daxi.mapper.order.MqDeadLetterMessageMapper;
import com.daxi.service.MqAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * MQ告警服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MqAlertServiceImpl implements MqAlertService {
    
    private final MqDeadLetterMessageMapper deadLetterMessageMapper;
    private final RestTemplate restTemplate;
    
    @Value("${mq.alert.dingtalk.webhook:}")
    private String dingTalkWebhook;
    
    @Value("${mq.alert.enabled:true}")
    private boolean alertEnabled;
    
    @Override
    public void recordAndAlert(MessageExt messageExt, String consumerGroup, String businessType, String errorMessage) {
        try {
            // 构建死信消息对象
            MqDeadLetterMessage deadLetterMessage = new MqDeadLetterMessage();
            deadLetterMessage.setMsgId(messageExt.getMsgId());
            deadLetterMessage.setTopic(messageExt.getTopic());
            deadLetterMessage.setTag(messageExt.getTags());
            deadLetterMessage.setConsumerGroup(consumerGroup);
            deadLetterMessage.setBusinessKey(messageExt.getKeys());
            deadLetterMessage.setMessageBody(new String(messageExt.getBody()));
            deadLetterMessage.setRetryCount(messageExt.getReconsumeTimes());
            deadLetterMessage.setErrorMessage(errorMessage);
            deadLetterMessage.setBusinessType(businessType);
            deadLetterMessage.setStatus(0); // 待处理
            deadLetterMessage.setCreateTime(LocalDateTime.now());
            deadLetterMessage.setUpdateTime(LocalDateTime.now());
            
            // 记录到数据库
            deadLetterMessageMapper.insert(deadLetterMessage);
            
            log.error("【MQ死信】消息已记录到数据库, msgId: {}, businessType: {}, businessKey: {}", 
                    messageExt.getMsgId(), businessType, messageExt.getKeys());
            
            // 发送告警
            if (alertEnabled) {
                sendAlertNotification(deadLetterMessage);
            }
            
        } catch (Exception e) {
            log.error("记录死信消息失败", e);
        }
    }
    
    @Override
    public void recordAndAlert(MqDeadLetterMessage deadLetterMessage) {
        try {
            deadLetterMessage.setStatus(0);
            deadLetterMessage.setCreateTime(LocalDateTime.now());
            deadLetterMessage.setUpdateTime(LocalDateTime.now());
            
            deadLetterMessageMapper.insert(deadLetterMessage);
            
            log.error("【MQ死信】消息已记录到数据库, msgId: {}, businessType: {}, businessKey: {}", 
                    deadLetterMessage.getMsgId(), 
                    deadLetterMessage.getBusinessType(), 
                    deadLetterMessage.getBusinessKey());
            
            if (alertEnabled) {
                sendAlertNotification(deadLetterMessage);
            }
            
        } catch (Exception e) {
            log.error("记录死信消息失败", e);
        }
    }
    
    @Override
    public void sendDingTalkAlert(String title, String content) {
        if (dingTalkWebhook == null || dingTalkWebhook.isEmpty()) {
            log.warn("钉钉Webhook未配置，跳过告警发送");
            return;
        }
        
        try {
            // 构建钉钉消息
            Map<String, Object> message = new HashMap<>();
            message.put("msgtype", "markdown");
            
            Map<String, String> markdown = new HashMap<>();
            markdown.put("title", title);
            markdown.put("text", content);
            message.put("markdown", markdown);
            
            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(message, headers);
            
            // 发送请求
            ResponseEntity<String> response = restTemplate.postForEntity(dingTalkWebhook, entity, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("钉钉告警发送成功: {}", title);
            } else {
                log.error("钉钉告警发送失败, 状态码: {}", response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("发送钉钉告警异常", e);
        }
    }
    
    @Override
    public long getUnprocessedCount() {
        LambdaQueryWrapper<MqDeadLetterMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MqDeadLetterMessage::getStatus, 0);
        return deadLetterMessageMapper.selectCount(wrapper);
    }
    
    /**
     * 发送告警通知
     */
    private void sendAlertNotification(MqDeadLetterMessage message) {
//        String title = "【DXShop】MQ死信消息告警";
        
//        StringBuilder content = new StringBuilder();
//        content.append("### ").append(title).append("\n\n");
//        content.append("**业务类型**: ").append(getBusinessTypeName(message.getBusinessType())).append("\n\n");
//        content.append("**消息ID**: `").append(message.getMsgId()).append("` \n\n");
//        content.append("**业务键**: `").append(message.getBusinessKey()).append("` \n\n");
//        content.append("**重试次数**: ").append(message.getRetryCount()).append(" 次\n\n");
//        content.append("**错误信息**: \n```\n").append(message.getErrorMessage()).append("\n```\n\n");
//        content.append("**发生时间**: ").append(message.getCreateTime()).append("\n\n");
//        content.append("> 请尽快登录系统查看并处理！");
//
        // 发送钉钉告警
//        sendDingTalkAlert(title, content.toString());
        
        // 同时记录到日志（确保即使钉钉发送失败也有记录）
        log.error("========== MQ死信消息告警 ==========");
        log.error("业务类型: {}", getBusinessTypeName(message.getBusinessType()));
        log.error("消息ID: {}", message.getMsgId());
        log.error("业务键: {}", message.getBusinessKey());
        log.error("重试次数: {}", message.getRetryCount());
        log.error("错误信息: {}", message.getErrorMessage());
        log.error("发生时间: {}", message.getCreateTime());
        log.error("=====================================");
    }
    
    /**
     * 获取业务类型名称
     */
    private String getBusinessTypeName(String businessType) {
        if (businessType == null) {
            return "未知";
        }
        switch (businessType) {
            case "ORDER_TIMEOUT":
                return "订单超时取消";
            case "REFUND_TIMEOUT":
                return "退款超时关闭";
            case "ADDRESS_MODIFY_TIMEOUT":
                return "地址修改超时关闭";
            default:
                return businessType;
        }
    }
}
