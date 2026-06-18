package com.daxi.response;

public class GoodsResponse {
    public static final String GOODS_NOT_EXIST = "没有找到对应的商品";
    public static final String GOODS_SOME_NOT_EXIST = "部分商品未找到";
    public static final String GOODS_NOT_ON_SALE = "商品不可售";
    public static final String moreThanMaxTagNumber(Integer size){
        return "商品标签数量不能超过"+size;
    };
    public static final String moreThanMaxCustomAttrNumber(Integer size){
        return "商品自定义属性数量不能超过"+size;
    };
    public static final String NOT_ENOUGH_GOODS_SPEC_NAME="应该提交的属性数量不正确";

    public static final String LACK_STOCK="库存不足";

}