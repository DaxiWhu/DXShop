-- ============================================================
-- DXShop 业务库建表脚本 (00-schema.sql)
-- 首次启动 MySQL 容器时由 /docker-entrypoint-initdb.d 自动执行
-- 与代码实体类 @TableName 一一对应，共 36 张业务表
-- 来源:
--   1) 桌面 DXshop.md 设计文档（商品/订单/关系/购物车模块）的建表 DDL
--   2) 代码实体类生成: chat 模块(cs_agent/chat_session/chat_message)、
--      mq_dead_letter_message（沿用 order 模块既有 SQL）、
--      shop_daily_repurchase_stat / spu_daily_repurchase_stat（用实体字段生成 MySQL 版）
-- 排除项（项目无对应实体 / 非 MySQL 交易库）:
--   - user_follow           : 项目把"关注人"拆分为 user_goods_follow + user_shop_follow
--   - user_shop_pay_detail  : Hive 数仓事实表，非交易库
--   - user_spu_pay_detail   : Hive 数仓事实表，非交易库
-- 修正项:
--   - user_shop_extend      : 删除引用了不存在列 seller_id 的索引（建表会失败）
--   - shop/spu_daily_repurchase_stat : 用实体字段生成 MySQL 版（原设计为 Hive 版，不可用）
-- 代码侧配套修正见 SpuDailyRepurchaseStat.java（@TableName 由 shop_daily_repurchase_stat 改为 spu_daily_repurchase_stat）
-- ============================================================

USE dxshop;

-- ============================================================
-- 商品模块
-- ============================================================

-- 抽象商品表SPU
CREATE TABLE `goods_spu` (
  `spu_id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
  `shop_id` BIGINT NOT NULL COMMENT '店铺ID',
  `shop_name`   varchar(100) not null comment '店铺名称（冗余，避免跨服务查询）',
  `category_id` BIGINT NOT NULL COMMENT '类目ID',
  `brand` VARCHAR(20) DEFAULT NULL COMMENT '品牌',
  `title` VARCHAR(255) NOT NULL COMMENT '商品标题',
  `sub_title` VARCHAR(255) DEFAULT '' COMMENT '副标题/卖点',
  `main_img` VARCHAR(255) NOT NULL COMMENT '主图URL',
  `price` BIGINT NOT NULL COMMENT '商品sku最低价格',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1=上架 2=下架 3=删除',
  `is_hot` TINYINT DEFAULT 0 COMMENT '是否热销',
  `sort` INT DEFAULT 0 COMMENT '排序权重，数字大靠前',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_shop_id` (shop_id),
  KEY `idx_category_id` (category_id),
  KEY `idx_status` (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品SPU主表';

-- 具体规格表SKU
CREATE TABLE `goods_sku` (
  `sku_id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `spu_id` BIGINT NOT NULL,
  `price` BIGINT NOT NULL COMMENT '售价',
  `stock` INT NOT NULL DEFAULT 0 COMMENT '库存',
  `bar_code` VARCHAR(30) DEFAULT '' COMMENT '条码',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序权重',
  `sku_spec` VARCHAR(255) NOT NULL COMMENT '规格展示文案（如：黑色 256G，冗余字段用于前端展示）',
  `status` TINYINT DEFAULT 1 COMMENT '1有效 0无效',
  `version` INT DEFAULT 0 COMMENT '乐观锁(防超卖)',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_bar_code` (`bar_code`),
  KEY `idx_spu_id` (spu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品SKU表';

-- 规格名称表
CREATE TABLE `goods_spec_name` (
  `id` BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '规格名ID',
  `spu_id` BIGINT NOT NULL COMMENT '关联商品SPU ID',
  `spec_name` VARCHAR(50) NOT NULL COMMENT '规格名称，如：颜色、内存、尺寸',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序权重，数字越大越靠前（前端展示顺序）',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY `idx_spu_id` (`spu_id`) COMMENT '按SPU查询规格名的索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品规格名称表';

-- 规格值表
CREATE TABLE `goods_spec_value` (
  `id` BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '规格值ID',
  `spec_id` BIGINT NOT NULL COMMENT '关联规格名称表ID（goods_spec_name.id）',
  `spec_value` VARCHAR(100) NOT NULL COMMENT '规格值，如：黑色、256G、XL',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序权重，数字越小越靠前（前端展示顺序）',
  `status` TINYINT DEFAULT 1 COMMENT '1有效 0无效',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  KEY `idx_spec_id` (`spec_id`) COMMENT '按规格名查询规格值的索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品规格值表';

-- SKU 规格关联表
CREATE TABLE `goods_sku_spec_ref` (
  `id` BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
  `sku_id` BIGINT NOT NULL COMMENT '关联商品SKU ID（goods_sku.sku_id）',
  `spec_value_id` BIGINT NOT NULL COMMENT '关联规格值表ID（goods_spec_value.id）',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_sku_spec_value` (`sku_id`, `spec_value_id`) COMMENT '唯一索引：防止SKU重复关联同一规格值',
  KEY `idx_sku_id` (`sku_id`) COMMENT '按SKU查询所有规格值的索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='SKU规格关联表';

-- 商品详情（大文本）
CREATE TABLE `goods_detail` (
  `detail_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `spu_id` BIGINT NOT NULL,
  `content` LONGTEXT COMMENT '详情富文本',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`detail_id`),
  UNIQUE KEY `uk_spu_id` (`spu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品详情表';

-- 商品图片列表
CREATE TABLE `goods_image` (
  `img_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `spu_id` BIGINT NOT NULL,
  `img_url` VARCHAR(255) NOT NULL COMMENT '图片URL',
  `description` varchar(255) COMMENT '图片描述',
  `sort` INT DEFAULT 0 COMMENT '排序',
  `is_main` TINYINT DEFAULT 0 COMMENT '1=主图 0=普通图',
  PRIMARY KEY (`img_id`),
  KEY `idx_spu_id` (`spu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品图片表';

-- 分类表
CREATE TABLE `goods_category` (
  `category_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '分类ID（主键）',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父分类ID：0=顶级分类，>0=子分类',
  `category_name` varchar(50) NOT NULL COMMENT '分类名称（如：手机、数码、苹果手机）',
  `level` tinyint NOT NULL COMMENT '分类层级：1=一级分类 2=二级分类 3=三级分类',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序权重（数字越大越靠前）',
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `uk_parent_name` (`parent_id`,`category_name`) COMMENT '同层级分类名称唯一（防重复）',
  KEY `idx_parent_id` (`parent_id`) COMMENT '按父ID查子分类（如查一级分类下的所有二级分类）',
  KEY `idx_sort` (`sort`) COMMENT '按排序权重查询（前端分类列表展示）'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品分类主表';

-- 分类属性模板表（只存 1 次属性名）
CREATE TABLE `goods_category_param` (
  `param_id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `category_id` BIGINT NOT NULL COMMENT '分类ID',
  `param_name` VARCHAR(50) NOT NULL COMMENT '属性名：CPU、内存、颜色...',
  `sort` INT DEFAULT 0,
  UNIQUE KEY `uk_cat_param` (`category_id`,`param_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='分类属性模板（固定属性定义，不重复）';

-- 商品参数（键值对）
CREATE TABLE `goods_param` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '参数值ID',
  `spu_id` BIGINT NOT NULL COMMENT '关联商品SPU ID',
  `template_id` BIGINT NOT NULL COMMENT '关联参数模板ID(category_param.param_id)',
  `param_value` VARCHAR(30) NOT NULL COMMENT '参数值，如：骁龙8 Gen3、5000mAh',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_spu_template` (`spu_id`, `template_id`),
  KEY `idx_spu_template` (`spu_id`, `template_id`),
  KEY `idx_template_id` (`template_id`) COMMENT '按模板ID查询所有商品的该参数值'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品参数值表（键值对，详情参数）';

-- 商家自定义参数
CREATE TABLE `goods_custom_param` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `spu_id` BIGINT NOT NULL,
  `attr_name` VARCHAR(50) NOT NULL,
  `attr_value` VARCHAR(255) NOT NULL,
  `sort` INT NOT NULL comment '展示顺序',
  PRIMARY KEY (`id`),
  KEY `idx_spu_id` (`spu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家自定义参数';

-- 商品 - 标签关联
CREATE TABLE `goods_tag_relation` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `spu_id` BIGINT NOT NULL,
  `tag_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_spu_tag` (`spu_id`,`tag_id`),
  KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品标签关联';

-- 标签主表
CREATE TABLE `goods_tag` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `tag_name` VARCHAR(64) NOT NULL COMMENT '标签名称',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序权重',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tag_name` (`tag_name`) -- 保证标签名唯一
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='标签主表';

-- 商品统计
CREATE TABLE `goods_stat` (
  `spu_id` BIGINT NOT NULL,
  `collect_num` INT DEFAULT 0 COMMENT '收藏数',
  `click_num` BIGINT DEFAULT 0 COMMENT '点击量',
  `buy_num` INT DEFAULT 0 COMMENT '销量',
  `comment_num` INT DEFAULT 0 COMMENT '评论数',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`spu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品统计表';

-- 商品评论
CREATE TABLE `goods_comment` (
  `comment_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `spu_id` BIGINT NOT NULL COMMENT '商品SPU ID',
  `sku_id` BIGINT NOT NULL COMMENT '商品SKU ID',
  `sku_dsc` varchar(100) NOT NULL comment 'sku描述',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `user_name` VARCHAR(30) NOT NULL COMMENT '用户名',
  `user_avatar` VARCHAR(50) NOT NULL COMMENT '用户头像',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `content` VARCHAR(1000) NOT NULL COMMENT '评价内容',
  `score` TINYINT NOT NULL COMMENT '1-5分',
  `is_anonymous` TINYINT DEFAULT 0 COMMENT '是否匿名：1=是 0=否（淘宝匿名评论功能）',
  `pictures` VARCHAR(1000) DEFAULT '' COMMENT '评论图片，JSON数组格式，如["https://xxx.com/1.jpg"]',
  `is_repurchase` TINYINT DEFAULT 0 COMMENT '是否商品复购：1=是 0=否（同一用户再次购买该商品）',
  `is_shop_return_customer` TINYINT DEFAULT 0 COMMENT '是否店铺回头客：1=是 0=否（同一用户再次购买该店铺任意商品）',
  `status` TINYINT DEFAULT 1 COMMENT '1=显示 0=隐藏',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`comment_id`),
  KEY `idx_spu_id` (`spu_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_score` (`score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品评论表';

-- 评论标签统计
CREATE TABLE `goods_comment_stat` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `spu_id` BIGINT NOT NULL,
  `tag_name` VARCHAR(30) NOT NULL COMMENT '标签:外观好看',
  `count` INT NOT NULL DEFAULT 0 COMMENT '数量',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_spu_tag` (`spu_id`,`tag_name`),
  KEY `idx_spu_id` (`spu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论标签统计';

-- 评论 - 标签关联
CREATE TABLE `goods_tag_comment_relation` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `spu_id` BIGINT NOT NULL,
  `comment_id` BIGINT NOT NULL,
  `tag_id` BIGINT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_spu_tag` (`comment_id`,`tag_id`),
  KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品标签关联';

-- ============================================================
-- 订单模块
-- ============================================================

-- 订单主表
CREATE TABLE `user_order` (
  `order_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID(内部主键)',
  `order_sn` VARCHAR(32) NOT NULL COMMENT '订单编号(用户可见，全局唯一)',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `shop_id` BIGINT NOT NULL COMMENT '店铺ID',
  `price`  DECIMAL(12,2) NOT NULL COMMENT '实付',
  `receiver_name` VARCHAR(20) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人手机号',
  `receiver_address` VARCHAR(255) NOT NULL COMMENT '完整收货地址(省市区+详细)',
  `address_status` TINYINT NOT NULL DEFAULT 0 COMMENT '收货信息是否改过',
  `logistics_company` VARCHAR(50) DEFAULT '' COMMENT '物流公司(中通/顺丰等)',
  `logistics_no` VARCHAR(50) DEFAULT '' COMMENT '运单号(发货时填写)',
  `send_time` DATETIME  COMMENT '发货时间',
  `finish_time` DATETIME COMMENT '用户确认收货时间',
  `order_status` TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态:0待付款 1待发货 2待收货 3待评价 4已完成 5已取消 6退款/售后' ,
  `operate_status` TINYINT NOT NULL DEFAULT 0 COMMENT '操作状态:0无 1申请换地址 2申请退款 3退款退货' ,
  `pay_status` TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态:0未支付 1已支付',
  `pay_time` DATETIME COMMENT '支付成功时间',
  `pay_sn` VARCHAR(64) DEFAULT '' COMMENT '第三方支付流水号(幂等防重复支付)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  `version` INT DEFAULT 0 COMMENT '乐观锁(防并发修改，如同时支付/取消)',
  `remark` VARCHAR(255) DEFAULT '' COMMENT '用户下单备注',
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_order_sn` (`order_sn`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_order_status` (`order_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单主表';

-- 订单商品明细表
CREATE TABLE `user_order_item` (
  `order_id` BIGINT NOT NULL COMMENT '关联订单主表 user_order',
  `spu_id` BIGINT NOT NULL COMMENT '关联商品SPU表 goods_spu',
  `sku_id` BIGINT NOT NULL COMMENT '关联商品SKU表 goods_sku',
  `goods_name` VARCHAR(255) NOT NULL COMMENT '商品名称(下单时的名称)',
  `goods_img` VARCHAR(255) NOT NULL COMMENT '商品主图(下单时的图片)',
  `sku_spec` VARCHAR(255) NOT NULL COMMENT 'SKU规格(如:黑色 256G)',
  `per_price` DECIMAL(12,2) NOT NULL COMMENT '商品单价(下单时的价格，永久不变)',
  `buy_num` INT NOT NULL DEFAULT 1 COMMENT '购买数量',
  `is_comment` TINYINT DEFAULT 0 COMMENT '是否评价:0未评价 1已评价',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除:0正常 1删除',
  PRIMARY KEY (`order_id`),
  KEY `idx_sku_id` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单商品明细表';

-- 收货地址
CREATE TABLE `user_address` (
  `address_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '地址主键ID',
  `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
  `receiver_name` VARCHAR(20) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人手机号',
  `province` VARCHAR(20) NOT NULL COMMENT '省份',
  `city` VARCHAR(20) NOT NULL COMMENT '城市',
  `district` VARCHAR(20) NOT NULL COMMENT '区/县',
  `detail_address` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '1=默认地址 0=普通地址',
  `tag` VARCHAR(10) DEFAULT '' COMMENT '地址标签：家/公司/学校',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`address_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_default_create_time` (`is_default`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户收货地址表';

-- 订单退款申请表
CREATE TABLE `order_refund` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `refund_no` VARCHAR(64) NOT NULL COMMENT '退款单号（全局唯一，幂等键）',
  `order_id` BIGINT NOT NULL COMMENT '关联订单号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `shop_id` BIGINT NOT NULL COMMENT '店铺id',
  `refund_type` TINYINT NOT NULL COMMENT '1-仅退款 2-退货退款',
  `refund_amount` DECIMAL(10,2)  COMMENT '退款金额',
  `refund_reason` VARCHAR(255)  COMMENT '退款原因',
  `evidence_images` VARCHAR(255) DEFAULT NULL COMMENT '退款图片',
  `status` TINYINT NOT NULL COMMENT '1-待商家审核 2-待用户退货 3-待商家收货 4-退款成功 5-退款关闭 6-商家拒绝',
  `apply_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `audit_time` DATETIME DEFAULT NULL COMMENT '商家审核时间',
  `expire_time` DATETIME  COMMENT '当前状态超时时间（用于定时任务处理）',
  `return_waybill` VARCHAR(32) DEFAULT NULL COMMENT '退货物流单号',
  `return_express_company` VARCHAR(32) DEFAULT NULL COMMENT '退货快递公司',
  `receive_time` DATETIME DEFAULT NULL COMMENT '商家收货时间',
  `refund_channel` VARCHAR(32) DEFAULT NULL COMMENT '退款渠道（支付宝/微信）',
  `refund_no_channel` VARCHAR(64) DEFAULT NULL COMMENT '支付渠道退款单号',
  `refund_time` DATETIME DEFAULT NULL COMMENT '退款成功时间',
  `fail_reason` VARCHAR(255) DEFAULT NULL COMMENT '退款失败原因',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status_expire` (`status`, `expire_time`) COMMENT '定时任务查询索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单退款申请表';

-- 订单地址修改申请表
CREATE TABLE `order_address_modify` (
  `order_id` bigint NOT NULL COMMENT '订单号',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `shop_id` BIGINT NOT NULL COMMENT '店铺id',
  `old_name` varchar(32) DEFAULT NULL COMMENT '原收货人姓名',
  `old_phone` varchar(20) DEFAULT NULL COMMENT '原收货人手机号',
  `old_detail` varchar(255) DEFAULT NULL COMMENT '原详细地址',
  `new_name` varchar(32) DEFAULT NULL COMMENT '新收货人姓名',
  `new_phone` varchar(20) DEFAULT NULL COMMENT '新收货人手机号',
  `new_detail` varchar(255) DEFAULT NULL COMMENT '新详细地址',
  `status` tinyint DEFAULT NULL COMMENT '状态 1=待审核 2=已同意 3=已拒绝 4=已过期 5=已撤销',
  `expire_time` datetime DEFAULT NULL COMMENT '申请过期时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`order_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单地址修改申请表';

-- ============================================================
-- 用户 / 关系模块
-- ============================================================

-- 用户公开信息表
CREATE TABLE `user_display` (
  `user_id`        BIGINT        NOT NULL COMMENT '用户ID(主键)',
  `nickname`       VARCHAR(32)   NULL DEFAULT '' COMMENT '用户昵称',
  `gender`         INT           NULL DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女',
  `avatar_url`     VARCHAR(255)  NULL DEFAULT '' COMMENT '头像URL',
  `account_status` INT           NULL DEFAULT 1 COMMENT '账号状态：1-正常 2-冻结 3-注销',
  `is_real_name`   INT           NULL DEFAULT 0 COMMENT '是否实名：0-未实名 1-已实名',
  `register_time`  DATETIME      NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `update_time`    DATETIME      NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户公开信息表';

-- 用户隐私信息表
CREATE TABLE `user_private` (
  `user_id`          BIGINT        NOT NULL COMMENT '用户ID(主键)',
  `birthday`         DATE          NULL COMMENT '生日',
  `real_name`        VARCHAR(64)   NULL DEFAULT '' COMMENT '真实姓名(加密)',
  `id_card`          VARCHAR(64)   NULL DEFAULT '' COMMENT '身份证号(加密)',
  `last_login_time`  DATETIME      NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后登录时间',
  `last_login_ip`    VARCHAR(128)  NULL DEFAULT '' COMMENT '最后登录IP',
  `phone`            VARCHAR(64)   NULL DEFAULT '' COMMENT '手机号(加密)',
  `email`            VARCHAR(64)   NULL DEFAULT '' COMMENT '邮箱',
  `password_hash`    VARCHAR(128)  NOT NULL COMMENT '密码哈希值',
  `password_salt`    VARCHAR(64)   NOT NULL COMMENT '密码盐值',
  `create_time`      DATETIME      NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_update_time` DATETIME      NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户隐私信息表';

-- 内容主要供展示的店铺表
CREATE TABLE `user_shop_display` (
  `shop_id` BIGINT NOT NULL COMMENT '店铺ID（主键）',
  `uesr_id` BIGINT NOT NULL,
  `shop_name` VARCHAR(100) NOT NULL COMMENT '店铺名称',
  `shop_type` TINYINT NOT NULL COMMENT '店铺类型：1-旗舰店 2-专卖店 3-专营店 4-个人店',
  `shop_status` TINYINT NOT NULL DEFAULT 1 COMMENT '店铺状态：0-关闭 1-正常 2-审核中 3-冻结',
  `logo_url` VARCHAR(255) COMMENT '店铺logo的url地址',
  `shop_score` DECIMAL(3,2) DEFAULT 5.00 COMMENT '店铺评分',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开店时间',
  PRIMARY KEY (`shop_id`),
  KEY `idx_shop_name` (`shop_name`),
  KEY `idx_shop_type` (`shop_type`),
  KEY `idx_shop_score` (`shop_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='内容主要供展示的店铺表';

-- 店铺扩展信息表（修正：原设计引用了不存在的 seller_id 列，已删除该索引）
CREATE TABLE `user_shop_extend` (
  `shop_id` BIGINT NOT NULL COMMENT '店铺ID（外键关联 user_shop_display.shop_id）',
  `shop_desc` TEXT COMMENT '店铺简介',
  `business_hours` VARCHAR(100) COMMENT '营业时间',
  `contact_phone` VARCHAR(20) COMMENT '联系电话',
  `contact_email` VARCHAR(50) COMMENT '联系邮箱',
  `address` VARCHAR(255) COMMENT '店铺地址',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`shop_id`),
  KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='店铺扩展信息表';

-- 店铺变更申请表
CREATE TABLE `shop_change_request` (
  `id` BIGINT NOT NULL PRIMARY KEY comment  '请求id',
  `shop_id` BIGINT NOT NULL COMMENT '店铺ID',
  `uesr_id` BIGINT NOT NULL comment '店铺所有者id',
  `shop_type` TINYINT NOT NULL COMMENT '店铺类型：1-旗舰店 2-专卖店 3-专营店 4-个人店',
  `status` TINYINT NOT NULL COMMENT '申请状态：1未审核 2已同意 3已拒绝',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  UNIQUE KEY `uk_shop_id` (`shop_id`),
  KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='店铺变更申请表';

-- 用户关注商品表
CREATE TABLE `user_goods_follow` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '关注主键ID',
  `user_id` BIGINT NOT NULL COMMENT '关注用户ID',
  `spu_id` BIGINT NOT NULL COMMENT '被关注商品SPU_ID（核心！不存SKU）',
  `follow_status` TINYINT NOT NULL DEFAULT 1 COMMENT '关注状态：1=已关注 0=已取关',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_spu` (`user_id`, `spu_id`) COMMENT '一个用户只能关注同一商品一次',
  KEY `idx_user_id` (`user_id`) COMMENT '快速查用户的关注商品',
  KEY `idx_spu_id` (`spu_id`) COMMENT '快速查商品的关注人数'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='淘宝风格-用户关注商品表';

-- 用户关注店铺表
CREATE TABLE `user_shop_follow` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '关注主键ID',
  `user_id` BIGINT NOT NULL COMMENT '关注用户ID',
  `shop_id` BIGINT NOT NULL COMMENT '被关注店铺ID',
  `follow_status` TINYINT NOT NULL DEFAULT 1 COMMENT '关注状态：1=已关注 0=已取关',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_shop` (`user_id`, `shop_id`) COMMENT '一个用户只能关注同一店铺一次',
  KEY `idx_user_id` (`user_id`) COMMENT '快速查用户的关注店铺',
  KEY `idx_shop_id` (`shop_id`) COMMENT '快速查店铺的粉丝'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='淘宝风格-用户关注店铺表';

-- 店铺日复购统计表（由实体字段生成 MySQL 版，替代设计文档中的 Hive 版）
CREATE TABLE `shop_daily_repurchase_stat` (
  `shop_id` BIGINT NOT NULL COMMENT '店铺ID',
  `stat_date` VARCHAR(10) NOT NULL COMMENT '统计日期 yyyy-MM-dd',
  `total_buy_user` BIGINT DEFAULT 0 COMMENT '当日有效购买总用户数',
  `first_buy_user` BIGINT DEFAULT 0 COMMENT '当日店铺新客数(首购用户)',
  `repurchase_user` BIGINT DEFAULT 0 COMMENT '当日店铺复购用户数(二次及以上)',
  `repurchase_rate` DECIMAL(5,4) DEFAULT 0 COMMENT '复购率=复购用户数/总购买用户数',
  `total_order_cnt` BIGINT DEFAULT 0 COMMENT '当日有效订单总数',
  `repurchase_order_cnt` BIGINT DEFAULT 0 COMMENT '当日复购订单数',
  `total_pay_amount` DECIMAL(16,2) DEFAULT 0 COMMENT '当日实付总金额',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '统计创建时间',
  PRIMARY KEY (`shop_id`),
  UNIQUE KEY `uk_shop_date` (`shop_id`, `stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='店铺日复购统计表';

-- 商品SPU日复购统计表（由实体字段生成 MySQL 版，替代设计文档中的 Hive 版）
CREATE TABLE `spu_daily_repurchase_stat` (
  `spu_id` BIGINT NOT NULL COMMENT '商品SPU_ID',
  `stat_date` VARCHAR(10) NOT NULL COMMENT '统计日期 yyyy-MM-dd',
  `total_buy_user` BIGINT DEFAULT 0 COMMENT '当日有效购买总用户数',
  `first_buy_user` BIGINT DEFAULT 0 COMMENT '当日商品新客数(首购用户)',
  `repurchase_user` BIGINT DEFAULT 0 COMMENT '当日商品复购用户数(二次及以上)',
  `repurchase_rate` DECIMAL(5,4) DEFAULT 0 COMMENT '复购率=复购用户数/总购买用户数',
  `total_order_cnt` BIGINT DEFAULT 0 COMMENT '当日有效订单总数',
  `total_pay_amount` DECIMAL(16,2) DEFAULT 0 COMMENT '当日实付总金额',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '统计创建时间',
  PRIMARY KEY (`spu_id`),
  UNIQUE KEY `uk_spu_date` (`spu_id`, `stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品SPU日复购统计表';

-- 仿淘宝购物车表
CREATE TABLE `user_cart` (
  `cart_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '购物车主键ID',
  `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
  `sku_id` BIGINT NOT NULL COMMENT '关联商品SKU ID',
  `spu_id` BIGINT NOT NULL COMMENT '关联商品SPU ID',
  `buy_num` INT NOT NULL DEFAULT 1 COMMENT '购买数量',
  `price` DECIMAL(10,2) NOT NULL COMMENT '加入购物车时的商品价格',
  `sku_name` VARCHAR(255) NOT NULL COMMENT '商品名称',
  `sku_img` VARCHAR(255) COMMENT '商品图片',
  `sku_spec` VARCHAR(255) COMMENT '规格描述（如“红色 XL”）',
  `checked` TINYINT NOT NULL DEFAULT 0 COMMENT '勾选状态：1=已勾选 0=未勾选',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '商品状态：1=正常 0=下架/失效',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加购时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`cart_id`),
  UNIQUE KEY `uk_user_sku` (`user_id`, `sku_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_user_checked` (`user_id`, `checked`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='仿淘宝购物车表';

-- ============================================================
-- 客服(chat)模块 —— 由实体类生成
-- ============================================================

-- 客服坐席表
CREATE TABLE `cs_agent` (
  `id` BIGINT NOT NULL COMMENT '客服ID',
  `username` VARCHAR(64) NOT NULL COMMENT '登录名',
  `password_hash` VARCHAR(128) NOT NULL COMMENT '密码哈希',
  `nickname` VARCHAR(64) DEFAULT '' COMMENT '昵称',
  `avatar_url` VARCHAR(255) DEFAULT '' COMMENT '头像URL',
  `status` INT DEFAULT 0 COMMENT '状态 0-离线 1-在线',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客服坐席表';

-- 客服会话表
CREATE TABLE `chat_session` (
  `id` BIGINT NOT NULL COMMENT '会话ID',
  `user_id` BIGINT DEFAULT NULL COMMENT '用户ID',
  `spu_id` BIGINT DEFAULT NULL COMMENT '商品SPU_ID',
  `status` INT DEFAULT 0 COMMENT '会话状态 0-进行中 1-已结束',
  `cs_agent_id` BIGINT DEFAULT NULL COMMENT '客服坐席ID',
  `last_message` VARCHAR(1024) DEFAULT '' COMMENT '最后一条消息',
  `last_message_time` DATETIME DEFAULT NULL COMMENT '最后消息时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_spu_id` (`spu_id`),
  KEY `idx_cs_agent_id` (`cs_agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客服会话表';

-- 客服消息表
CREATE TABLE `chat_message` (
  `id` BIGINT NOT NULL COMMENT '消息ID',
  `session_id` BIGINT DEFAULT NULL COMMENT '会话ID',
  `sender_type` INT DEFAULT 0 COMMENT '发送方类型 1-用户 2-客服 3-系统',
  `sender_id` BIGINT DEFAULT NULL COMMENT '发送方ID',
  `content` TEXT COMMENT '消息内容',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客服消息表';

-- ============================================================
-- MQ 死信消息表 —— 沿用 order 模块既有 SQL
-- ============================================================

CREATE TABLE IF NOT EXISTS `mq_dead_letter_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `msg_id` VARCHAR(64) NOT NULL COMMENT '消息ID',
  `topic` VARCHAR(128) NOT NULL COMMENT 'Topic名称',
  `tag` VARCHAR(64) DEFAULT NULL COMMENT 'Tag标签',
  `consumer_group` VARCHAR(128) NOT NULL COMMENT '消费者组',
  `business_key` VARCHAR(128) DEFAULT NULL COMMENT '业务键（订单号/退款ID等）',
  `message_body` TEXT COMMENT '消息体内容',
  `retry_count` INT DEFAULT 0 COMMENT '重试次数',
  `error_message` TEXT COMMENT '错误信息',
  `business_type` VARCHAR(64) NOT NULL COMMENT '业务类型：ORDER_TIMEOUT-订单超时, REFUND_TIMEOUT-退款超时, ADDRESS_MODIFY-地址修改超时',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '处理状态：0-待处理, 1-已处理, 2-忽略',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `handler` VARCHAR(64) DEFAULT NULL COMMENT '处理人',
  `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
  `handle_remark` TEXT COMMENT '处理备注',
  PRIMARY KEY (`id`),
  KEY `idx_msg_id` (`msg_id`) COMMENT '消息ID索引',
  KEY `idx_business_key` (`business_key`) COMMENT '业务键索引',
  KEY `idx_status` (`status`) COMMENT '状态索引',
  KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引',
  KEY `idx_business_type` (`business_type`) COMMENT '业务类型索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='MQ死信消息记录表';
