package com.daxi.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * MQ死信消息记录表
 * 用于记录进入死信队列的消息，便于人工介入处理
 */
@Data
@TableName("mq_dead_letter_message")
public class MqDeadLetterMessage {
    
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /** 消息ID */
    private String msgId;
    
    /** Topic名称 */
    private String topic;
    
    /** Tag标签 */
    private String tag;
    
    /** 消费者组 */
    private String consumerGroup;
    
    /** 业务键（订单号/退款ID等） */
    private String businessKey;
    
    /** 消息体内容 */
    private String messageBody;
    
    /** 重试次数 */
    private Integer retryCount;
    
    /** 错误信息 */
    private String errorMessage;
    
    /** 业务类型：ORDER_TIMEOUT-订单超时, REFUND_TIMEOUT-退款超时, ADDRESS_MODIFY-地址修改超时 */
    private String businessType;
    
    /** 处理状态：0-待处理, 1-已处理, 2-忽略 */
    private Integer status;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
    
    /** 处理人 */
    private String handler;
    
    /** 处理时间 */
    private LocalDateTime handleTime;
    
    /** 处理备注 */
    private String handleRemark;
}
