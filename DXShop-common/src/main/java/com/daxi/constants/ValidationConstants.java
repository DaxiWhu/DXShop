package com.daxi.constants;

/**
 * 校验常量接口
 * 用于AO类中的注解校验,提供统一的数字常量定义
 * 
 * @author DXShop
 */
public interface ValidationConstants {
    
    // ==================== ID相关常量 ====================
    /** 最小ID值(用于主键、外键等) */
    int MIN_ID = 1;
    
    // ==================== 分页相关常量 ====================
    /** 最小页码 */
    int MIN_PAGE_NUM = 1;
    /** 最大页码 */
    int MAX_PAGE_NUM = 100;
    /** 最小每页数量 */
    int MIN_PAGE_SIZE = 1;
    /** 最大每页数量(防止前端一次查太多拖垮数据库) */
    int MAX_PAGE_SIZE = 100;
    /** 商品查询最大每页数量 */
    int MAX_SPU_PAGE_SIZE = 20;
    
    // ==================== 长度限制常量 - 通用 ====================
    /** 名称最大长度(姓名、标签名等) */
    int MAX_NAME_LENGTH = 50;
    /** 手机号/电话最大长度 */
    int MAX_PHONE_LENGTH = 20;
    /** 邮箱最大长度 */
    int MAX_EMAIL_LENGTH = 30;
    /** 地址最大长度(省市区) */
    int MAX_ADDRESS_LENGTH = 50;
    /** 详细地址最大长度 */
    int MAX_DETAIL_ADDRESS_LENGTH = 50;
    /** 省份/城市/区县最大长度 */
    int MAX_REGION_LENGTH = 10;
    /** 备注最大长度 */
    int MAX_REMARK_LENGTH = 100;
    /** 原因说明最大长度(退款原因、拒绝原因等) */
    int MAX_REASON_LENGTH = 100;
    /** 标签最大长度 */
    int MAX_TAG_LENGTH = 10;
    /** 属性名称最大长度 */
    int MAX_ATTR_NAME_LENGTH = 20;
    /** 属性值最大长度 */
    int MAX_ATTR_VALUE_LENGTH = 20;
    /** 图片url最大长度 */
    int MAX_URL_LENGTH = 100;
    /** 图片描述最大长度 */
    int MAX_IMG_DESCRIPTION_LENGTH= 100;

    // ==================== 长度限制常量 - 商品相关 ====================
    /** 商品标题最大长度 */
    int MAX_GOODS_TITLE_LENGTH = 100;
    /** 商品副标题最大长度 */
    int MAX_GOODS_SUBTITLE_LENGTH = 200;
    /** 品牌名称最大长度 */
    int MAX_BRAND_LENGTH = 50;
    /** 店铺名称最大长度 */
    int MAX_SHOP_NAME_LENGTH = 50;
    /** SKU描述最大长度 */
    int MAX_SKU_DESC_LENGTH = 100;
    /** 评论最大长度 */
    int MAX_COMMENT_LENGTH = 1000;
    /** 规格名称最大长度 */
    int MAX_SPEC_NAME_LENGTH = 10;
    /** 规格值最大长度 */
    int MAX_SPEC_VALUE_LENGTH = 10;
    /** 物流公司最大长度 */
    int MAX_LOGISTICS_COMPANY_LENGTH = 20;

    // ==================== 长度限制常量 - 用户相关 ====================
    int SHOP_NAME_MAX_LENGTH = 50;
    /** 物流单号最大长度 */
    int MAX_WAYBILL_LENGTH = 64;
    /** 店铺描述最大长度 */
    int MAX_SHOP_DESC_LENGTH = 200;
    /** 联系邮箱最大长度 */
    int MAX_CONTACT_EMAIL_LENGTH = 100;
    /** 营业时间最大长度 */
    int MAX_BUSINESS_HOURS_LENGTH = 50;
    /** 地址最大长度(店铺地址) */
    int MAX_SHOP_ADDRESS_LENGTH = 200;

    // ==================== 数量限制常量 - 商品相关 ====================
    /** 商品图片最大数量 */
    int MAX_GOODS_IMAGE_NUMBER = 10;
    /** 商品标签最大数量 */
    int MAX_GOODS_TAG_NUMBER = 20;
    /** 商品自定义属性最大数量 */
    int MAX_GOODS_CUSTOM_ATTR_NUMBER = 10;
    /** 规格值最大数量 */
    int MAX_SPEC_VALUE_NUMBER = 10;
    /**商品分类属性的最少数量*/
    int MIN_CATEGORY_ATTR_SIZE=0;
    // ==================== 状态枚举常量 ====================
    /** 订单状态-全部 */
    int ORDER_STATUS_ALL = -1;
    /** 订单状态-售后 */
    int ORDER_STATUS_REFUND_AFTER_SALES = 6;
    /** 是否默认-是 */
    int IS_DEFAULT = 1;
    /** 是否默认-否 */
    int IS_NOT_DEFAULT = 0;
    /** 审核结果-同意/通过 */
    int AUDIT_RESULT_AGREE = 1;
    /** 审核结果-拒绝 */
    int AUDIT_RESULT_REJECT = 2;
    /** 退款类型-仅退款 */
    int REFUND_TYPE_ONLY_REFUND = 1;
    /** 退款类型-退货退款 */
    int REFUND_TYPE_RETURN_REFUND = 2;
    /** 退款审批-通过 */
    int REFUND_APPROVE = 1;
    /** 退款审批-拒绝 */
    int REFUND_REJECT = 2;
    /** 审核结果最小值 */
    int MIN_AUDIT_RESULT = 1;
    /** 审核结果最大值 */
    int MAX_AUDIT_RESULT = 2;
    /** 退款类型最小值 */
    int MIN_REFUND_TYPE = 1;
    /** 退款类型最大值 */
    int MAX_REFUND_TYPE = 2;
    
    // ==================== 排序权重常量 ====================
    /** 最小排序值 */
    int MIN_SORT = 0;
    
    // ==================== 购买数量常量 ====================
    /** 最小购买数量 */
    int MIN_BUY_NUM = 1;

    // ==================== 订单相关 ====================
    int MAX_REJECT_REASON_LENGTH = 100;
    int MAX_LOGISTICS_NO_LENGTH = 64;
    int MAX_REFUND_REASON_LENGTH = 100;
}
