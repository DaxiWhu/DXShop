/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_message` (
  `id` bigint NOT NULL,
  `session_id` bigint NOT NULL,
  `sender_type` int NOT NULL,
  `sender_id` bigint NOT NULL,
  `content` text,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_session` (
  `id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `spu_id` bigint NOT NULL,
  `status` int DEFAULT '0',
  `cs_agent_id` bigint DEFAULT NULL,
  `last_message` varchar(500) DEFAULT NULL,
  `last_message_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cs_agent` (
  `id` bigint NOT NULL,
  `username` varchar(50) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `nickname` varchar(50) DEFAULT NULL,
  `avatar_url` varchar(500) DEFAULT NULL,
  `status` int DEFAULT '0',
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=5005 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品分类主表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods_category_param` (
  `param_id` bigint NOT NULL AUTO_INCREMENT,
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `param_name` varchar(50) NOT NULL COMMENT '属性名：CPU、内存、颜色...',
  `sort` int DEFAULT '0',
  PRIMARY KEY (`param_id`),
  UNIQUE KEY `uk_cat_param` (`category_id`,`param_name`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='分类属性模板（固定属性定义，不重复）';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods_comment` (
  `comment_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `spu_id` bigint NOT NULL COMMENT '商品SPU ID',
  `sku_id` bigint NOT NULL COMMENT '商品SKU ID',
  `sku_dsc` varchar(100) NOT NULL COMMENT 'sku描述',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `user_name` varchar(30) NOT NULL COMMENT '用户名',
  `user_avatar` varchar(100) NOT NULL COMMENT '用户头像',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `content` varchar(1000) NOT NULL COMMENT '评价内容',
  `score` tinyint NOT NULL COMMENT '1-5分',
  `is_anonymous` tinyint DEFAULT '0' COMMENT '是否匿名：1=是 0=否（淘宝匿名评论功能）',
  `pictures` varchar(1000) DEFAULT '' COMMENT '评论图片，JSON数组格式，如["https://xxx.com/1.jpg"]',
  `is_repurchase` tinyint DEFAULT '0' COMMENT '是否商品复购：1=是 0=否（同一用户再次购买该商品）',
  `is_shop_return_customer` tinyint DEFAULT '0' COMMENT '是否店铺回头客：1=是 0=否（同一用户再次购买该店铺任意商品）',
  `status` tinyint DEFAULT '1' COMMENT '1=显示 0=隐藏',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`comment_id`),
  KEY `idx_spu_id` (`spu_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_score` (`score`)
) ENGINE=InnoDB AUTO_INCREMENT=62 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品评论表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods_comment_stat` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `spu_id` bigint NOT NULL,
  `tag_name` varchar(30) NOT NULL COMMENT '标签:外观好看',
  `count` int NOT NULL DEFAULT '0' COMMENT '数量',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_spu_tag` (`spu_id`,`tag_name`),
  KEY `idx_spu_id` (`spu_id`)
) ENGINE=InnoDB AUTO_INCREMENT=92 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='评论标签统计';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods_custom_param` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `spu_id` bigint NOT NULL,
  `attr_name` varchar(50) NOT NULL,
  `attr_value` varchar(255) NOT NULL,
  `sort` int NOT NULL COMMENT '展示顺序',
  PRIMARY KEY (`id`),
  KEY `idx_spu_id` (`spu_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2066509521483526147 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家自定义参数';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods_detail` (
  `detail_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `spu_id` bigint NOT NULL,
  `content` longtext COMMENT '详情富文本',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`detail_id`),
  UNIQUE KEY `uk_spu_id` (`spu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品详情表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods_image` (
  `img_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `spu_id` bigint NOT NULL,
  `img_url` varchar(255) NOT NULL COMMENT '图片URL',
  `description` varchar(255) DEFAULT NULL COMMENT '图片描述',
  `sort` int DEFAULT '0' COMMENT '排序',
  `is_main` tinyint DEFAULT '0' COMMENT '1=主图 0=普通图',
  PRIMARY KEY (`img_id`),
  KEY `idx_spu_id` (`spu_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2066509521387057155 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品图片表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods_param` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '参数值ID',
  `spu_id` bigint NOT NULL COMMENT '关联商品SPU ID',
  `template_id` bigint NOT NULL COMMENT '关联参数模板ID（tb_goods_param_template.template_id）',
  `param_value` varchar(30) NOT NULL COMMENT '参数值，如：骁龙8 Gen3、5000mAh',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_spu_template` (`spu_id`,`template_id`),
  KEY `idx_spu_template` (`spu_id`,`template_id`),
  KEY `idx_template_id` (`template_id`) COMMENT '按模板ID查询所有商品的该参数值'
) ENGINE=InnoDB AUTO_INCREMENT=2066844408547840002 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品参数值表（键值对，详情参数）';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods_sku` (
  `sku_id` bigint NOT NULL AUTO_INCREMENT,
  `spu_id` bigint NOT NULL,
  `price` bigint NOT NULL COMMENT '售价',
  `stock` int NOT NULL DEFAULT '0' COMMENT '库存',
  `bar_code` varchar(30) DEFAULT '' COMMENT '条码',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序权重',
  `sku_spec` varchar(255) NOT NULL COMMENT '规格展示文案（如：黑色 256G，冗余字段用于前端展示）',
  `status` tinyint DEFAULT '1' COMMENT '1有效 0无效',
  `version` int DEFAULT '0' COMMENT '乐观锁(防超卖)',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`sku_id`),
  UNIQUE KEY `uk_bar_code` (`bar_code`),
  KEY `idx_spu_id` (`spu_id`)
) ENGINE=InnoDB AUTO_INCREMENT=238 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品SKU表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods_sku_spec_ref` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `sku_id` bigint NOT NULL COMMENT '关联商品SKU ID（goods_sku.sku_id）',
  `spec_value_id` bigint NOT NULL COMMENT '关联规格值表ID（goods_spec_value.id）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sku_spec_value` (`sku_id`,`spec_value_id`) COMMENT '唯一索引：防止SKU重复关联同一规格值',
  KEY `idx_sku_id` (`sku_id`) COMMENT '按SKU查询所有规格值的索引'
) ENGINE=InnoDB AUTO_INCREMENT=4136 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='SKU规格关联表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods_spec_name` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '规格名ID',
  `spu_id` bigint NOT NULL COMMENT '关联商品SPU ID',
  `spec_name` varchar(50) NOT NULL COMMENT '规格名称，如：颜色、内存、尺寸',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序权重，数字越小越靠前（前端展示顺序）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_spu_id` (`spu_id`) COMMENT '按SPU查询规格名的索引'
) ENGINE=InnoDB AUTO_INCREMENT=78 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品规格名称表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods_spec_value` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '规格值ID',
  `spec_id` bigint NOT NULL COMMENT '关联规格名称表ID（goods_spec_name.id）',
  `spec_value` varchar(100) NOT NULL COMMENT '规格值，如：黑色、256G、XL',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序权重，数字越小越靠前（前端展示顺序）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '是否启用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_spec_id` (`spec_id`) COMMENT '按规格名查询规格值的索引'
) ENGINE=InnoDB AUTO_INCREMENT=185 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品规格值表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods_spu` (
  `spu_id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `shop_id` bigint NOT NULL COMMENT '店铺ID',
  `shop_name` varchar(100) NOT NULL COMMENT '店铺名称（冗余，避免跨服务查询）',
  `category_id` bigint NOT NULL COMMENT '类目ID',
  `brand` varchar(20) DEFAULT NULL COMMENT '品牌',
  `title` varchar(255) NOT NULL COMMENT '商品标题',
  `sub_title` varchar(255) DEFAULT '' COMMENT '副标题/卖点',
  `main_img` varchar(255) NOT NULL COMMENT '主图URL',
  `price` bigint NOT NULL DEFAULT '0' COMMENT '商品最低价（单位：分）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1=上架 2=下架 3=删除',
  `is_hot` tinyint DEFAULT '0' COMMENT '是否热销',
  `sort` int DEFAULT '0' COMMENT '排序权重',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`spu_id`),
  KEY `idx_shop_id` (`shop_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=2066844407440543747 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品SPU主表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods_stat` (
  `spu_id` bigint NOT NULL,
  `collect_num` int DEFAULT '0' COMMENT '收藏数',
  `click_num` bigint DEFAULT '0' COMMENT '点击量',
  `buy_num` int DEFAULT '0' COMMENT '销量',
  `comment_num` int DEFAULT '0' COMMENT '评论数',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`spu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品统计表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods_tag` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `tag_name` varchar(64) NOT NULL COMMENT '标签名称',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序权重',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tag_name` (`tag_name`)
) ENGINE=InnoDB AUTO_INCREMENT=2066509521458360322 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='标签主表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods_tag_comment_relation` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `spu_id` bigint NOT NULL,
  `comment_id` bigint NOT NULL,
  `tag_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_spu_tag` (`comment_id`,`tag_id`),
  KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB AUTO_INCREMENT=203 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品标签关联';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `goods_tag_relation` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `spu_id` bigint NOT NULL,
  `tag_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_spu_tag` (`spu_id`,`tag_id`),
  KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2066509521466748930 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品标签关联';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shop_change_request` (
  `id` bigint NOT NULL COMMENT '请求id',
  `shop_id` bigint NOT NULL COMMENT '店铺ID',
  `user_id` bigint DEFAULT NULL COMMENT '店铺所有者id',
  `shop_type` tinyint DEFAULT NULL COMMENT '店铺类型：1-旗舰店 2-专卖店 3-专营店 4-个人店',
  `status` tinyint NOT NULL COMMENT '申请状态：1未审核 2已同意 3已拒绝',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shop_id` (`shop_id`),
  KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `undo_log` (
  `branch_id` bigint NOT NULL COMMENT 'branch transaction id',
  `xid` varchar(128) NOT NULL COMMENT 'global transaction id',
  `context` varchar(128) NOT NULL COMMENT 'undo_log context,such as serialization',
  `rollback_info` longblob NOT NULL COMMENT 'rollback info',
  `log_status` int NOT NULL COMMENT '0:normal status,1:defense status',
  `log_created` datetime(6) NOT NULL COMMENT 'create datetime',
  `log_modified` datetime(6) NOT NULL COMMENT 'modify datetime',
  UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`),
  KEY `ix_log_created` (`log_created`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AT transaction mode undo table';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_cart` (
  `cart_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '购物车主键ID',
  `user_id` bigint NOT NULL COMMENT '所属用户ID',
  `sku_id` bigint NOT NULL COMMENT '关联商品SKU ID',
  `spu_id` bigint NOT NULL COMMENT '关联商品SPU ID',
  `buy_num` int NOT NULL DEFAULT '1' COMMENT '购买数量',
  `price` decimal(10,2) NOT NULL COMMENT '加入购物车时的商品价格',
  `title` varchar(255) NOT NULL COMMENT '商品名称',
  `main_img` varchar(255) DEFAULT NULL COMMENT '商品图片',
  `sku_spec` varchar(255) DEFAULT NULL COMMENT '规格描述（如“红色 XL”）',
  `checked` tinyint NOT NULL DEFAULT '0' COMMENT '勾选状态：1=已勾选 0=未勾选',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '商品状态：1=正常 0=下架/失效',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加购时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`cart_id`),
  UNIQUE KEY `uk_user_sku` (`user_id`,`sku_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_user_checked` (`user_id`,`checked`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='仿淘宝购物车表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_display` (
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID，与user_private表一一对应',
  `nickname` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户昵称',
  `gender` tinyint unsigned DEFAULT '0' COMMENT '性别：0-未知 1-男 2-女',
  `avatar_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '用户头像URL',
  `account_status` tinyint unsigned NOT NULL DEFAULT '1' COMMENT '账号状态：1-正常 2-冻结 3-注销',
  `is_real_name` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '是否实名：0-未实名 1-已实名',
  `register_time` datetime NOT NULL COMMENT '注册时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_account_status` (`account_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户公开信息表（对外展示，非敏感字段）';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_follow` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `follower_id` bigint NOT NULL COMMENT '关注者ID（我）',
  `followed_id` bigint NOT NULL COMMENT '被关注者ID（对方）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1=关注中 0=取消关注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follower_followed` (`follower_id`,`followed_id`) COMMENT '防重复关注',
  KEY `idx_follower_id` (`follower_id`) COMMENT '查我关注的人',
  KEY `idx_followed_id` (`followed_id`) COMMENT '查我的粉丝'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户关注关系表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_goods_follow` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '关注主键ID',
  `user_id` bigint NOT NULL COMMENT '关注用户ID',
  `spu_id` bigint NOT NULL COMMENT '被关注商品SPU_ID（核心！不存SKU）',
  `follow_status` tinyint NOT NULL DEFAULT '1' COMMENT '关注状态：1=已关注 0=已取关',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_spu` (`user_id`,`spu_id`) COMMENT '一个用户只能关注同一商品一次',
  KEY `idx_user_id` (`user_id`) COMMENT '快速查用户的关注商品',
  KEY `idx_spu_id` (`spu_id`) COMMENT '快速查商品的关注人数'
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='淘宝风格-用户关注商品表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_private` (
  `user_id` bigint unsigned NOT NULL COMMENT '用户唯一ID，主键，与user_display表一一对应',
  `birthday` date DEFAULT NULL COMMENT '用户生日',
  `real_name` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '真实姓名（加密存储）',
  `id_card` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '身份证号（加密存储）',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '最后登录IP（支持IPv6）',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '绑定手机号（加密存储，非必须）',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '绑定邮箱（非必须）',
  `password_hash` varchar(128) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码加盐哈希值（禁止明文存储）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_phone` (`phone`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户私有信息表（敏感/隐私字段，独立权限管控）';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_shop_display` (
  `shop_id` bigint NOT NULL COMMENT '店铺ID（主键）',
  `user_id` bigint NOT NULL,
  `shop_name` varchar(100) NOT NULL COMMENT '店铺名称',
  `shop_type` tinyint NOT NULL COMMENT '店铺类型：1-旗舰店 2-专卖店 3-专营店 4-个人店',
  `shop_status` tinyint NOT NULL DEFAULT '1' COMMENT '店铺状态：0-关闭 1-正常 2-审核中 3-冻结',
  `logo_url` varchar(255) DEFAULT NULL COMMENT '店铺logo的url地址',
  `shop_score` decimal(3,2) DEFAULT '5.00' COMMENT '店铺评分',
  `create_time` datetime NOT NULL DEFAULT (now()) COMMENT '开店时间',
  PRIMARY KEY (`shop_id`),
  UNIQUE KEY `user_shop_display_shop_id_user_id_uindex` (`shop_id`,`user_id`),
  KEY `idx_shop_name` (`shop_name`),
  KEY `idx_shop_type` (`shop_type`),
  KEY `idx_shop_score` (`shop_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='内容主要供展示的店铺表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_shop_extend` (
  `shop_id` bigint NOT NULL COMMENT '店铺ID（外键关联tb_shop.shop_id）',
  `shop_desc` text COMMENT '店铺简介',
  `business_hours` varchar(100) DEFAULT NULL COMMENT '营业时间',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `contact_email` varchar(50) DEFAULT NULL COMMENT '联系邮箱',
  `address` varchar(255) DEFAULT NULL COMMENT '店铺地址',
  `update_time` datetime NOT NULL DEFAULT (now()) ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`shop_id`),
  UNIQUE KEY `uk_shop_id` (`shop_id`),
  KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='店铺扩展信息表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_shop_follow` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '关注主键ID',
  `user_id` bigint NOT NULL COMMENT '关注用户ID',
  `shop_id` bigint NOT NULL COMMENT '被关注店铺SPU_ID（核心！不存SKU）',
  `follow_status` tinyint NOT NULL DEFAULT '1' COMMENT '关注状态：1=已关注 0=已取关',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_spu` (`user_id`,`shop_id`) COMMENT '一个用户只能关注同一店铺一次',
  KEY `idx_user_id` (`user_id`) COMMENT '快速查用户的关注店铺',
  KEY `idx_spu_id` (`shop_id`) COMMENT '快速查商品的关注人数'
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='淘宝风格-用户关注店铺表';

-- ===================== 订单模块表（补充：原初始化脚本缺少 order 模块，导致集成测试查表报 Table doesn't exist） =====================

CREATE TABLE IF NOT EXISTS `user_order` (
  `order_id` bigint NOT NULL COMMENT '订单ID（雪花ID，ASSIGN_ID）',
  `order_sn` varchar(64) DEFAULT NULL COMMENT '订单编号',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺ID',
  `price` decimal(10,2) DEFAULT NULL COMMENT '实付金额',
  `receiver_name` varchar(100) DEFAULT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) DEFAULT NULL COMMENT '收货人手机号',
  `receiver_address` varchar(255) DEFAULT NULL COMMENT '收货地址',
  `address_status` int DEFAULT NULL COMMENT '地址状态',
  `logistics_company` varchar(100) DEFAULT NULL COMMENT '物流公司',
  `logistics_no` varchar(100) DEFAULT NULL COMMENT '运单号',
  `send_time` datetime DEFAULT NULL COMMENT '发货时间',
  `finish_time` datetime DEFAULT NULL COMMENT '确认收货时间',
  `order_status` int DEFAULT NULL COMMENT '订单状态',
  `operate_status` int DEFAULT NULL COMMENT '操作状态',
  `pay_status` int DEFAULT NULL COMMENT '支付状态',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `pay_sn` varchar(100) DEFAULT NULL COMMENT '支付流水号',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `is_deleted` int DEFAULT '0' COMMENT '逻辑删除',
  `version` int DEFAULT '0' COMMENT '乐观锁',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户订单表';

CREATE TABLE IF NOT EXISTS `user_order_item` (
  `order_id` bigint NOT NULL COMMENT '关联订单ID',
  `spu_id` bigint DEFAULT NULL COMMENT '商品SPU ID',
  `sku_id` bigint DEFAULT NULL COMMENT '商品SKU ID',
  `goods_name` varchar(255) DEFAULT NULL COMMENT '商品名称快照',
  `goods_img` varchar(255) DEFAULT NULL COMMENT '商品主图',
  `sku_spec` varchar(255) DEFAULT NULL COMMENT '规格',
  `per_price` decimal(10,2) DEFAULT NULL COMMENT '单价',
  `buy_num` int DEFAULT NULL COMMENT '购买数量',
  `is_comment` int DEFAULT '0' COMMENT '是否评价',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `is_deleted` int DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`order_id`,`spu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单明细表';

CREATE TABLE IF NOT EXISTS `user_address` (
  `address_id` bigint NOT NULL AUTO_INCREMENT COMMENT '地址主键ID',
  `user_id` bigint DEFAULT NULL COMMENT '所属用户ID',
  `receiver_name` varchar(100) DEFAULT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) DEFAULT NULL COMMENT '收货人手机号',
  `province` varchar(50) DEFAULT NULL COMMENT '省份',
  `city` varchar(50) DEFAULT NULL COMMENT '城市',
  `district` varchar(50) DEFAULT NULL COMMENT '区/县',
  `detail_address` varchar(255) DEFAULT NULL COMMENT '详细地址',
  `is_default` int DEFAULT '0' COMMENT '1=默认 0=普通',
  `tag` varchar(20) DEFAULT NULL COMMENT '地址标签',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`address_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户收货地址表';

CREATE TABLE IF NOT EXISTS `order_refund` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `refund_no` varchar(64) DEFAULT NULL COMMENT '退款单号',
  `order_id` bigint DEFAULT NULL COMMENT '订单ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺ID',
  `refund_type` int DEFAULT NULL COMMENT '1-仅退款 2-退货退款',
  `refund_amount` decimal(10,2) DEFAULT NULL COMMENT '退款金额',
  `refund_reason` varchar(500) DEFAULT NULL COMMENT '退款原因',
  `evidence_images` varchar(1000) DEFAULT NULL COMMENT '凭证图片',
  `status` int DEFAULT NULL COMMENT '退款状态',
  `apply_time` datetime DEFAULT NULL COMMENT '申请时间',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  `return_waybill` varchar(100) DEFAULT NULL COMMENT '退货运单号',
  `return_express_company` varchar(100) DEFAULT NULL COMMENT '退货快递公司',
  `receive_time` datetime DEFAULT NULL COMMENT '收货时间',
  `refund_channel` varchar(50) DEFAULT NULL COMMENT '退款渠道',
  `refund_no_channel` varchar(100) DEFAULT NULL COMMENT '渠道退款单号',
  `refund_time` datetime DEFAULT NULL COMMENT '退款时间',
  `fail_reason` varchar(500) DEFAULT NULL COMMENT '失败原因',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单退款表';

CREATE TABLE IF NOT EXISTS `order_address_modify` (
  `order_id` bigint NOT NULL COMMENT '订单号主键',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺ID',
  `old_name` varchar(100) DEFAULT NULL COMMENT '原收货人姓名',
  `old_phone` varchar(20) DEFAULT NULL COMMENT '原收货人手机号',
  `old_detail` varchar(255) DEFAULT NULL COMMENT '原详细地址',
  `new_name` varchar(100) DEFAULT NULL COMMENT '新收货人姓名',
  `new_phone` varchar(20) DEFAULT NULL COMMENT '新收货人手机号',
  `new_detail` varchar(255) DEFAULT NULL COMMENT '新详细地址',
  `status` int DEFAULT NULL COMMENT '状态',
  `expire_time` datetime DEFAULT NULL COMMENT '申请过期时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单地址修改申请表';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MQ死信消息记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
