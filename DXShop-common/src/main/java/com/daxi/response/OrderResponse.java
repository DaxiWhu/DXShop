package com.daxi.response;

public class OrderResponse {
    public static final String ORDER_NOT_EXIST="订单不存在";
    public static final String ADDRESS_NOT_EXIST="地址不存在";
    public static final String PARAM_ERROR="参数错误";
    public static final String NO_PRIVILEGE="无权限";
    public static final String ORDER_NOT_REPEAT="请勿重复提交订单";
    public static final String ORDER_NOT_CHANGE_ADDRESS="不允许修改地址";
    public static final String ORDER_STATUS_ERROR="订单状态错误";
    public static final String ORDER_QUERY_MIN_SIZE_ERROR="每页数量最小为1";
    public static final String ORDER_QUERY_MAX_SIZE_ERROR="每页数量最大为100";
    public static final String ORDER_QUERY_MIN_PAGE_ERROR="页码最小为1";
    public static final String ORDER_STATUS_NOT_MATCH="订单状态已变化，请刷新后重试";
    public static final String REFUND_ORDER_STATUS_NOT_SUPPORT="当前订单状态不支持退款";
    public static final String NO_REASON_AND_IMG="请填写退款原因和图片";
    public static final String ADDRESS_MODIFY_NOT_EXIST="地址修改申请不存在";
    public static final String ADDRESS_MODIFY_STATUS_ERROR="地址修改申请状态不正确";
    public static final String REFUND_NOT_EXIST="退款申请不存在";
    public static final String REFUND_STATUS_ERROR="退款申请状态不正确";
    public static final String REFUNDS_ERROR="退款失败";
    public static final String SKU_SOLDED_OUT="商品已售罄";
    public static final String CREATE_ORDER_ERROR="下单失败，请稍后重试";
    public static final String RECEIVER_NAME_ERROR="收货人姓名长度不能超过20";
    public static final String RECEIVER_PHONE_ERROR="收货人手机号长度不能超过20";
    public static final String RECEIVER_ADDRESS_ERROR="收货地址长度不能超过50";
    public static final String REMARK_ERROR="备注长度不能超过500";
    public static final String BUY_NUMBER_ERROR= "购买数量错误";
    public static final String ORDER_ADDRESS_MODIFY_STATUS_ERROR = "地址修改申请状态异常";
    public static final String ORDER_REFUND_STATUS_ERROR="退款状态异常";
    public static final String PAYMENT_ORDER_NOT_EXIST="支付订单不存在";
    public static final String PAYMENT_ALREADY_PAID="订单已支付";
    public static final String PAYMENT_EXPIRED="支付已过期";
    public static final String PAYMENT_QUERY_FAILED="支付状态查询失败";
    public static final String PAYMENT_NOT_PAID="订单未支付";

}
