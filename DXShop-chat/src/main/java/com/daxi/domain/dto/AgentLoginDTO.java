package com.daxi.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentLoginDTO {
    private Long agentId;
    private String nickname;
    private String avatarUrl;
    private String token;
}
