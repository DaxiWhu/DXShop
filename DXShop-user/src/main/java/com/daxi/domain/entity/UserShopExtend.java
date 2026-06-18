package com.daxi.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_shop_extend")
public class UserShopExtend {
    /**
     * 店铺ID（主键，关联主表 shop_id）
     * 与主表共用同一个ID，一对一绑定
     */
    @TableId(type = IdType.INPUT)
    private Long shopId;


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
