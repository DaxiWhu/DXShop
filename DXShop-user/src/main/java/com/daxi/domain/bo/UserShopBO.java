package com.daxi.domain.bo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class UserShopBO {
    /**
     * 店铺ID（主键）
     * 注意：非自增，手动赋值/分布式ID
     */
    private Long shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 店铺类型：1-旗舰店 2-专卖店 3-专营店 4-个人店
     */
    private Integer shopType;

    /**
     * 店铺状态：0-关闭 1-正常 2-审核中 3-冻结
     */
    private Integer shopStatus;

    /**
     * 店铺logo的url地址
     */
    private String logoUrl;

    /**
     * 店铺评分
     */
    private BigDecimal shopScore;

    /**
     * 开店时间
     */
    private LocalDateTime createTime;
    /**
     * 店铺ID（主键，关联主表 shop_id）
     * 与主表共用同一个ID，一对一绑定
     */

    /**
     * 卖家ID（关联用户表user_id）
     */
    private Long sellerId;

    /**
     * 店铺简介
     */
    private String shopDesc;

    /**
     * 营业时间
     */
    private String businessHours;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * 店铺地址
     */
    private String address;

    /**
     * 更新时间（数据库自动更新）
     */
    private LocalDateTime updateTime;

}
