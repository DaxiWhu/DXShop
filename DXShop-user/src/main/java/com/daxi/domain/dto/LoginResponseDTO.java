package com.daxi.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * 登录响应数据传输对象(DTO)类
 * 用于封装登录操作后返回给客户端的数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    private Long userId;
    private Long shopId;
    private String token; // JWT Token
}
