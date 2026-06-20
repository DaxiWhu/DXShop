package com.daxi.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("chat_session")
@Data
public class ChatSession {
    @TableId(type = IdType.INPUT)
    private Long id;
    private Long userId;
    private Long spuId;
    private Integer status;
    private Long csAgentId;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
