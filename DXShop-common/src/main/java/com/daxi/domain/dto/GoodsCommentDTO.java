package com.daxi.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GoodsCommentDTO {
    @Data
    public static class Comment{
        /** sku描述*/
        private String skuDsc;
        /** 用户ID */
        private Long userId;
        /** 用户名 （如果匿名的话就是匿名用户）*/
        private String userName;
        /** 用户头像 */
        private String userAvatar;
        /** 评价内容 */
        private String content;
        /** 评分（1-5分） */
        private Integer score;
        /** 评论图片（JSON数组格式） */
        private String pictures;
        /** 是否商品复购：1=是 0=否 */
        private Integer isRepurchase;
        /** 是否店铺回头客：1=是 0=否 */
        private Integer isShopReturnCustomer;
        /** 评论时间 */
        private LocalDateTime commentAt;
    }
    @Data
    public static class CommentTag {
        /** 主键ID */
        private Long id;
        /** 标签名称（如：外观好看） */
        private String tagName;
        /** 统计数量 */
        private Integer count;
        List<Comment> comments;
    }
    List<Comment> allComment;
    List<CommentTag> commentTags;
}
