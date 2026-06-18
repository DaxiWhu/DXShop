package com.daxi.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("order_address_modify")
public class OrderAddressModify {

    /**
     * 订单号主键
     */
    @TableId()
    private Long orderId;

    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 店铺ID
     */
    private Long shopId;
    // ===================== 原地址（下单快照） =====================
    /**
     * 原收货人姓名
     */
    private String oldName;

    /**
     * 原收货人手机号
     */
    private String oldPhone;

    /**
     * 原详细地址
     */
    private String oldDetail;

    // ===================== 新申请地址 =====================
    /**
     * 新收货人姓名
     */
    private String newName;

    /**
     * 新收货人手机号
     */
    private String newPhone;

    /**
     * 新详细地址
     */
    private String newDetail;

    /**
     * 状态
     * 1=待审核 2=已同意 3=已拒绝 4=已过期 5=已撤销
     */
    private Integer status;

    /**
     * 申请过期时间
     */
    private LocalDateTime expireTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
