package com.daxi.limit;

public class UserLimit {
    public static final int MAX_USER_NAME_LENGTH=20;
    public static final int MAX_COMMENT_LENGTH=1000;
    public static final int MAX_COMMENT_IMAGE_LENGTH=20;
    /**首次购买标记，回头客标记*/
    public static final int USER_FIRST_BUY=1;
    public static final int USER_RETURN_BUY=0;
    public static final int USER_COMMENT_STATUS_WAIT=1;
    /**关注状态*/
    public static final int USER_FOLLOW=1;
    public static final int USER_NOT_FOLLOW=0;
    /**购物车勾选状态*/
    public static final int USER_CART_CHECKED=1;
    public static final int USER_CART_NOT_CHECKED=0;
    /**购物车商品状态*/
    public static final int SKU_EFFECTIVE=1;
    public static final int SKU_NOT_EFFECTIVE=0;
    /**店铺类型*/
    public static final int SHOP_TYPE_FLAGSHIP = 1;      // 旗舰店
    public static final int SHOP_TYPE_SPECIALTY = 2;     // 专卖店
    public static final int SHOP_TYPE_FRANCHISE = 3;     // 专营店
    public static final int SHOP_TYPE_PERSONAL = 4;      // 个人店
    
    /**店铺状态*/
    public static final int SHOP_STATUS_CLOSED = 0;      // 关闭
    public static final int SHOP_STATUS_NORMAL = 1;      // 正常
    public static final int SHOP_STATUS_AUDITING = 2;    // 审核中
    public static final int SHOP_STATUS_FROZEN = 3;      // 冻结
    /**店铺约束*/
    public static final int MAX_SHOP_NAME_LENGTH = 20;
    /**店铺修改申请状态*/
    public static final int SHOP_CHANGE_REQUEST_STATUS_WAIT = 1;    // 待审核
    public static final int SHOP_CHANGE_REQUEST_STATUS_APPROVED = 2;    // 审核通过
    public static final int SHOP_CHANGE_REQUEST_STATUS_REJECTED = 3;    // 审核拒绝
    /**验证码相关*/
    /** 验证码长度 */
    public static final int VERIFY_CODE_LENGTH = 6;
    /** 验证码过期时间（秒） */
    public static final long VERIFY_CODE_EXPIRE_SECONDS = 120L;
    /** 验证码发送间隔（秒），行业标准60秒防刷 */
    public static final long VERIFY_CODE_INTERVAL_SECONDS = 60L;
    /**用户登录一次有效时间*/
    public static final long USER_LOGIN_EFFECTIVE_DAY=7;
    /**LOGIN token的前缀*/
    public static final String LOGIN_TOKEN_PREFIX="Bearer ";
    /**自动生成用户名的前缀*/
    public static final String AUTO_USER_NAME_PREFIX="用户";
    /**用户性别*/
    public static final int USER_SEX_FEMALE=2;  //女
    public static final int USER_SEX_MALE=1;    //男
    public static final int USER_SEX_UNKNOWN=0; //未知
    /**用户账号状态*/
    public static final int USER_STATUS_NORMAL=1;   //正常
    public static final int USER_STATUS_LOCKED=2;   //冻结
    public static final int USER_STATUS_LOGOUT=3;   //注销
    /**用户是否实名*/
    public static final int USER_REAL_NAME=1;   //已实名
    public static final int USER_NOT_REAL_NAME=0;   //未实名

}
