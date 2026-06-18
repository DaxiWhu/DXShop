package com.daxi.domain.ao;

import com.daxi.constants.ValidationConstants;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UserShopSpuUpdateAO implements ValidationConstants {


    @Data
    public static class ImageAO {
        /** 图片ID */
        @Min(value = MIN_ID)
        private Long imgId;
        /** 图片URL */
        @Length(max = MAX_URL_LENGTH)
        private String imgUrl;
        /** 图片描述 */
        @Length(max = MAX_IMG_DESCRIPTION_LENGTH)
        private String description;
        /** 排序权重 */
        private Integer sort;
        /** 是否主图：1=是 0=否 */
        private Integer isMain;
    }
    @Data
    public static class TagAO {
        /** 标签ID */
        @Min(value = MIN_ID)
        private Long id;
        /** 标签名称（如：热销、新品、生日礼物） */
        @Length(max = MAX_TAG_LENGTH)
        private String tagName;
        /** 排序权重 */
        private Integer sort;
    }
    @Data
    public static class CategoryAttrAO {
        /** 属性ID （需要回查）*/
        @Min(value = MIN_ID)
        private Long id;
        @Length(max = MAX_ATTR_VALUE_LENGTH)
        private String attrValue;
    }
    @Data
    public static class CustomAttrAO {
        /** 属性ID （需要回查）*/
        @Min(value = MIN_ID)
        private Long id;
        @Length(max = MAX_ATTR_NAME_LENGTH)
        private String attrName;
        @Length(max = MAX_ATTR_VALUE_LENGTH)
        private String attrValue;
        private Integer sort;
    }
    /** 商品标题 */
    private String title;
    /** 副标题/卖点 */
    private String subTitle;
    /** 主图URL */
    private String mainImg;
    /** sku最低价格（供展示,自动计算） */
    private BigDecimal price;
    /** 状态：1=上架 2=下架 3=删除 */
    private Integer status;

    /** 要修改的图片（全都传） */
    @Size(max = MAX_GOODS_IMAGE_NUMBER)
    private List<ImageAO> imgUpdates;
    /** 删除的图片 */
    @Size(max = MAX_GOODS_IMAGE_NUMBER)
    private List<Long> imgDeletes;
    /**要新增的图片*/
    @Size(max = MAX_GOODS_IMAGE_NUMBER)
    private List<ImageAO> imgAdds;

    /** 删除的标签 */
    @Size(max=MAX_GOODS_TAG_NUMBER)
    private List<Long> tagDeletes;
    /** 要新增的商品标签 */
    @Size(max=MAX_GOODS_TAG_NUMBER)
    private List<TagAO> tagAdds;

    /** 要修改的分类商品属性 */
    private List<CategoryAttrAO> categoryAttrUpdates;

    /** 要修改的自定义商品属性 */
    @Size(max = MAX_GOODS_CUSTOM_ATTR_NUMBER)
    private List<CustomAttrAO> customAttrUpdates;
    /** 删除的自定义属性 */
    @Size(max = MAX_GOODS_CUSTOM_ATTR_NUMBER)
    private List<Long> customAttrDeletes;
    /** 要新增的自定义商品属性 */
    @Size(max = MAX_GOODS_CUSTOM_ATTR_NUMBER)
    private List<CustomAttrAO> customAttrAdds;
}
