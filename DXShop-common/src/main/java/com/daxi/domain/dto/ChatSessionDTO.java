package com.daxi.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChatSessionDTO {
    private Long id;
    private Long userId;
    private Long spuId;
    private Integer status;
    private Long csAgentId;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private LocalDateTime createTime;
    private List<ChatMessageDTO> messages;
}
