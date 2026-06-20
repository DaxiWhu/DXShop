package com.daxi.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("chat_message")
@Data
public class ChatMessage {
    @TableId(type = IdType.INPUT)
    private Long id;
    private Long sessionId;
    private Integer senderType;
    private Long senderId;
    private String content;
    private LocalDateTime createTime;
}
