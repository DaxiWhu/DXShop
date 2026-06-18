package com.daxi.constants;

/**
 * RocketMQ Topic 和 Tag 常量
 */
public class RocketMQConstants {
    
    /**
     * 订单相关 Topic
     */
    public static final String TOPIC_ORDER = "TOPIC_ORDER";
    
    /**
     * 订单创建 Tag
     */
    public static final String TAG_ORDER_CREATE = "TAG_ORDER_CREATE";
    
    /**
     * 订单超时检查 Tag
     */
    public static final String TAG_ORDER_TIMEOUT = "TAG_ORDER_TIMEOUT";
    
    /**
     * 订单支付 Tag
     */
    public static final String TAG_ORDER_PAY = "TAG_ORDER_PAY";
    
    /**
     * 订单取消 Tag
     */
    public static final String TAG_ORDER_CANCEL = "TAG_ORDER_CANCEL";

    public static final String TOPIC_REFUND = "TOPIC_REFUND";
    
    public static final String TAG_REFUND_TIMEOUT = "TAG_REFUND_TIMEOUT";

    /**
     * 地址修改相关 Topic
     */
    public static final String TOPIC_ADDRESS_MODIFY = "TOPIC_ADDRESS_MODIFY";
    
    /**
     * 地址修改超时检查 Tag
     */
    public static final String TAG_ADDRESS_MODIFY_TIMEOUT = "TAG_ADDRESS_MODIFY_TIMEOUT";
}
