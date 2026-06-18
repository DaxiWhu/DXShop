package com.daxi.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OssPolicyDTO {
    private String accessKeyId;
    private String policy;
    private String signature;
    private String host;
    private String dir;
    private Long expire;
    private String securityToken; // 私有Bucket必传
}
