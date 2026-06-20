package com.daxi.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessageDTO {
    private Long id;
    private Long sessionId;
    private Integer senderType;
    private Long senderId;
    private String content;
    private LocalDateTime createTime;
}
