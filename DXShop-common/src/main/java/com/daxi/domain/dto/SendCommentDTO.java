package com.daxi.domain.dto;

import lombok.Data;

@Data
public class SendCommentDTO {
    private String skuSpec;
    private String userName;
    private String userAvatar;
    /** 是否商品复购：1=是 0=否 */
    private Integer isRepurchase;
    /** 是否店铺回头客：1=是 0=否 */
    private Integer isShopReturnCustomer;
}
