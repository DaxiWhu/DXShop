package com.daxi.limit;

public class OrderLimit {

    public static final int MIN_ID_SCALE = 1;
    public static final int MIN_BUY_NUM = 1;
    public static final int MAX_NAME_LENGTH = 50;
    public static final int MAX_PHONE_LENGTH = 20;
    public static final int MAX_TAG_LENGTH = 10;

    public static final int MAX_ADDRESS_LENGTH = 50;
    public static final int MAX_REMARK_LENGTH = 100;
    public static final int MAX_PROVINCE_LENGTH = 10;
    public static final int MAX_CITY_LENGTH = 10;
    public static final int MAX_DISTRICT_LENGTH = 10;
    public static final int MAX_DETAIL_ADDRESS_LENGTH = 20;

    public static final int MIN_REFUND_TYPE = 1;
    public static final int MAX_REFUND_TYPE = 2;
    public static final int MAX_REFUND_REASON_LENGTH = 100;
    public static final int MAX_EVIDENCE_IMAGES_LENGTH = 500;

    public static final int MAX_LOGISTICS_COMPANY_LENGTH = 20;
    public static final int MAX_WAYBILL_LENGTH = 64;

    public static final int PAY_TIME_MINUTES = 30;
    public static final int PAY_TIME_CACHE_MINUTES=1;
    public static final int PAY_LOCK_ON_STATUS=1;
    public static final int PAY_LOCK_OFF_STATUS=0;

    //以下是订单的状态以及操作状态
    public static final int ORDER_STATUS_PENDING_PAYMENT = 0;
    public static final int ORDER_STATUS_PENDING_SHIPMENT = 1;
    public static final int ORDER_STATUS_PENDING_RECEIPT = 2;
    public static final int ORDER_STATUS_PENDING_REVIEW = 3;
    public static final int ORDER_STATUS_COMPLETED = 4;
    public static final int ORDER_STATUS_CANCELLED = 5;
    public static final int ORDER_STATUS_REFUND_AFTER_SALES = 6;
    public static final int MIN_PAGE_NUM = 1;
    public static final int MIN_PAGE_SIZE = 1;
    public static final int MAX_PAGE_SIZE = 100; // 防止前端一次查太多拖垮数据库
    public static final int STATUS_ALL = -1;     // 代表查询全部状态
    //payStatus
    public static final int PAY_OK_STATUS=1;
    public static final int PAY_NO_STATUS=0;

    public static final int OPERATE_STATUS_NONE = 0;
    public static final int OPERATE_STATUS_APPLY_ADDRESS_CHANGE = 1;
    public static final int OPERATE_STATUS_APPLY_REFUND = 2;
    public static final int OPERATE_STATUS_REFUND_AND_RETURN = 3;

    public static final int ADDRESS_STATUS_CHANGE=1;
    public static final int ADDRESS_STATUS_NOT_CHANGE=0;

    /**OrderAddressModify*/
    public static final int ORDER_ADDRESS_MODIFY_STATUS_WAIT = 1;
    public static final int ORDER_ADDRESS_MODIFY_STATUS_SUCCESS = 2;
    public static final int ORDER_ADDRESS_MODIFY_STATUS_REJECT = 3;
    public static final int ORDER_ADDRESS_MODIFY_STATUS_CANCEL = 5;
    public static final int ORDER_ADDRESS_MODIFY_STATUS_EXPIRE = 4;
    /**AddressStatus*/
    public static final int ORDER_ADDRESS_CHANGE=1;
    public static final int ORDER_ADDRESS_NOT_CHANGE=0;

    /**订单有效状态*/
    public static final int ORDER_EFFECTIVE = 1;
    public static final int ORDER_INVALID = 0;
    /**退款类型*/

    public static final int ONLY_REFUND=1;
    public static final int REFUND_AND_RETURN=2;
    /**退款状态*/
    /** 1-待商家审核 */
    public static final int WAIT_MERCHANT_AUDIT = 1;
    /** 2-待用户退货 */
    public static final int WAIT_USER_RETURN = 2;
    /** 3-待商家收货 */
    public static final int WAIT_MERCHANT_RECEIVE = 3;
    /** 4-退款成功 */
    public static final int REFUND_SUCCESS = 4;
    /** 5-退款关闭 */
    public static final int REFUND_EXPIRE = 5;
    /** 6-商家拒绝 */
    public static final int MERCHANT_REJECT = 6;

    //和redis key有关的
    public static final String ORDER_SUBMIT_LOCK_ON = "1";
    public static final String ORDER_SUBMIT_LOCK_OFF = "0";
    public static final int ORDER_SUBMIT_LOCK_EXPIRE_SECONDS=5;
    public static final int REFUND_CACHE_EXPIRE_SECONDS=2880*60;
    public static final int ORDERID_EXPIRE_SECONDS=15552000;//半年
    public static final int SPU_CACHE_MINUTE=5;
    public static final int SKU_CACHE_MINUTE=5;
    public static final int ORDER_CHANGE_ADDRESS_REQUEST_SECONDS=86400;//24小时
    public static final int ORDER_CHANGE_ADDRESS_REQUEST_HOURS=2;
    public static final int ORDER_DETAIL_EXPIRE_MINUTES=30;
    public static final int ORDER_REFUND_REQUEST_HOURS=2;

    // 支付状态常量
    public static final int PAY_STATUS_UNPAID = 0;
    public static final int PAY_STATUS_PAID = 1;
    public static final int PAY_STATUS_REFUNDED = 2;

    // 支付相关配置
    public static final int PAYMENT_EXPIRE_MINUTES = 30;
    public static final String PAYMENT_SUCCESS_MSG = "支付成功";
    public static final String PAYMENT_PENDING_MSG = "等待支付";
    public static final String PAYMENT_FAILED_MSG = "支付失败";
}
