package com.daxi.domain.dto;

import lombok.Data;

@Data
public class OrderStatusCountDTO {
    /**
     * 待付款订单数
     */
    private Integer pendingPayment;

    /**
     * 待发货订单数
     */
    private Integer pendingShipment;

    /**
     * 待收货订单数
     */
    private Integer pendingReceipt;

    /**
     * 待评价订单数
     */
    private Integer pendingReview;

    /**
     * 已完成订单数
     */
    private Integer completed;

    /**
     * 已取消订单数
     */
    private Integer cancelled;

    /**
     * 售后/退款订单数
     */
    private Integer refundAfterSales;

    /**
     * 订单总数
     */
    private Integer total;
}
