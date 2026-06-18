package com.daxi.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_order")
public class UserOrder {
    @TableId(type = IdType.ASSIGN_ID)
    private Long orderId;

    // 订单编号(全局唯一)
    private String orderSn;

    // 用户ID
    private Long userId;

    // 店铺ID
    private Long shopId;

    // 实付金额
    private BigDecimal price;

    // 收货人姓名
    private String receiverName;

    // 收货人手机号
    private String receiverPhone;

    // 收货地址
    private String receiverAddress;

    private Integer addressStatus;

    // 物流公司
    private String logisticsCompany;

    // 运单号
    private String logisticsNo;

    // 发货时间
    private LocalDateTime sendTime;

    // 确认收货时间
    private LocalDateTime finishTime;

    // 订单状态
    private Integer orderStatus;
    private Integer operateStatus;
    // 支付状态
    private Integer payStatus;

    // 支付时间
    private LocalDateTime payTime;

    // 支付流水号
    private String paySn;

    // 创建时间
    private LocalDateTime createTime;

    // 更新时间
    private LocalDateTime updateTime;

    // 逻辑删除
    @TableLogic
    private Integer isDeleted;

    // 乐观锁
    @Version
    private Integer version;

    // 备注
    private String remark;
}
