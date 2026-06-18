package com.daxi.domain.ao;

import com.daxi.constants.ValidationConstants;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.util.List;

import static com.daxi.response.OrderResponse.PARAM_ERROR;

@Data
public class UserShopInsertAO implements ValidationConstants {
    @Data
    public static class CategoryAttrAO {
        @NotNull(message = PARAM_ERROR)
        @Min(value = MIN_ID, message = PARAM_ERROR)
        private Long templateId;
        
        @NotNull(message = PARAM_ERROR)
        @Length(max = MAX_SPEC_VALUE_LENGTH, message = PARAM_ERROR)
        private String paramValue;
    }

    @NotNull(message = PARAM_ERROR)
    @Min(value = MIN_ID, message = PARAM_ERROR)
    private Long categoryId;
    
    @Length(max = MAX_BRAND_LENGTH, message = PARAM_ERROR)
    private String brand;
    
    /**
     * 商品标题
     */
    @NotNull(message = PARAM_ERROR)
    @Length(max = MAX_GOODS_TITLE_LENGTH, message = PARAM_ERROR)
    private String title;
    
    /**
     * 副标题/卖点
     */
    @NotBlank(message = PARAM_ERROR)
    @Length(max = MAX_GOODS_SUBTITLE_LENGTH, message = PARAM_ERROR)
    private String subTitle;
    
    /**
     * 主图URL
     */
    @NotBlank(message = PARAM_ERROR)
    @Length(max = MAX_URL_LENGTH, message = PARAM_ERROR)
    private String mainImg;
    
    /**
     * sku最低价格（供展示,自动计算）
     */
    @NotNull(message = PARAM_ERROR)
    @Min(value = MIN_BUY_NUM, message = PARAM_ERROR)
    private BigDecimal yuan;
    
    /**
     * 状态：1=上架 2=下架 3=删除
     */
    @NotNull(message = PARAM_ERROR)
    @Min(value = MIN_AUDIT_RESULT, message = PARAM_ERROR)
    private Integer status;

    /**
     * 需要的分类商品属性
     */
    @NotEmpty(message = PARAM_ERROR)
    private List<CategoryAttrAO> categoryAttrs;
}
