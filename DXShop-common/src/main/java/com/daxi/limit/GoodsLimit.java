package com.daxi.limit;

/**
 * 商品模块限制常量类
 * 包含校验参数、状态标志、缓存配置等常量
 */
public class GoodsLimit {
    
    // ==================== 校验相关常量 ====================
    
    /** 图片最大数量（用于注解校验） */
    public static final int MAX_IMAGE_NUMBER = 10;
    
    /** 标签最大数量（用于注解校验） */
    public static final int MAX_TAG_NUMBER = 20;
    
    /** 自定义属性最大数量（用于注解校验） */
    public static final int MAX_CUSTOM_ATTR_NUMBER = 10;
    
    /** 一次性查询商品最大数量 */
    public static final int ONCE_GOODS_QUERY_NUMBER = 20;
    
    // 分页相关
    /** 页码最大值 */
    public static final int MAX_PAGENUM = 100;
    /** 页码最小值 */
    public static final int MIN_PAGENUM = 1;
    /** 每页大小最大值 */
    public static final int MAX_PAGESIZE = 20;
    /** 每页大小最小值 */
    public static final int MIN_PAGESIZE = 1;
    
    /** ID 最小值（用于校验） */
    public static final int MIN_ID_SCALE = 1;
    
    // 字段长度限制
    /** 商品标题最大长度 */
    public static final int MAX_TITLE_LENGTH = 100;
    /** 商品副标题最大长度 */
    public static final int MAX_SUBTITLE_LENGTH = 200;
    /** 品牌名称最大长度 */
    public static final int MAX_BRAND_LENGTH = 50;
    /** 图片 URL 最大长度 */
    public static final int MAX_IMG_URL_LENGTH = 500;
    /** ID 字段最大长度 */
    public static final int ID_LENGTH = 20;
    
    // 规格相关
    /** 规格名称最大长度 */
    public static final int SPEC_NAME_MAX_LENGTH = 10;
    /** 规格名称最大数量 */
    public static final int SPEC_NAME_MAX_NUMBER = 5;
    /** 规格名称最小排序值 */
    public static final int SPEC_NAME_MIN_SORT = 0;
    /** 规格值最大长度 */
    public static final int SPEC_VALUE_MAX_LENGTH = 10;
    /** 规格值最大数量 */
    public static final int SPEC_VALUE_MAX_NUMBER = 10;
    /** 规格值最小排序值 */
    public static final int SPEC_VALUE_MIN_SORT = 0;
    
    /** SKU 描述最大长度 */
    public static final int MAX_SKU_DSC_LENGTH = 100;

    
    // ==================== 状态标志常量 ====================
    
    /** SKU 下架状态 */
    public static final int SKU_NOT_ON_SALE = 0;
    /** SKU 上架状态 */
    public static final int SKU_ON_SALE = 1;
    
    /** SPU 下架状态 */
    public static final int SPU_NOT_ON_SALE = 0;
    /** SPU 上架状态 */
    public static final int SPU_ON_SALE = 1;
    /**
     * 评论状态1是有效0是隐藏
     */
    public static final int COMMENT_SHOW = 1;
    public static final int COMMENT_HIDE = 0;
    // ==================== Redis 缓存相关常量 ====================
    
    /** SKU 详情缓存过期时间（分钟） */
    public static final int SKU_DETAIL_CACHE_EXPIRE_MINUTES = 5;
}
