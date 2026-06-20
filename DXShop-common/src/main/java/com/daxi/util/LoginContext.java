package com.daxi.util;

import lombok.Data;

@Data
public class LoginContext {
    // 登录人ID
    private Long userId;
    // 当前操作对应的店铺ID
    private Long shopId;
    //客服id
    private Long agentId;
}
