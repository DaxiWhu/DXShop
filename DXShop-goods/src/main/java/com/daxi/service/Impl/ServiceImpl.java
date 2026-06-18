package com.daxi.service.Impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.daxi.Exception.BusinessException;
import com.daxi.domain.ao.UserShopSpuUpdateAO;
import com.daxi.domain.entity.GoodsCustomParam;
import com.daxi.domain.entity.GoodsImage;
import com.daxi.domain.entity.GoodsTag;
import com.daxi.domain.entity.GoodsTagRelation;
import com.daxi.mapper.GoodsCustomParamMapper;
import com.daxi.mapper.GoodsImageMapper;
import com.daxi.mapper.GoodsTagMapper;
import com.daxi.mapper.GoodsTagRelationMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.daxi.limit.GoodsLimit.MAX_CUSTOM_ATTR_NUMBER;
import static com.daxi.limit.GoodsLimit.MAX_IMAGE_NUMBER;
import static com.daxi.limit.GoodsLimit.MAX_TAG_NUMBER;
import static com.daxi.response.GoodsResponse.moreThanMaxCustomAttrNumber;
import static com.daxi.response.GoodsResponse.moreThanMaxTagNumber;
import static com.daxi.response.ImageResponse.moreThanMaxImageNumber;

@Service
@RequiredArgsConstructor
public class ServiceImpl {
    private final @NonNull GoodsTagMapper goodsTagMapper;
    private final @NonNull GoodsCustomParamMapper goodsCustomParamMapper;
    private final @NonNull GoodsTagRelationMapper goodsTagRelationMapper;
    private final @NonNull GoodsImageMapper goodsImageMapper;

    @Transactional(rollbackFor = Exception.class)
    public void updateAttrs(
            List<Long> counts,
            UserShopSpuUpdateAO ao,
            Long spuId) {

        /**
         *检查属性数量是否过大
         * */
        int attrCount=counts.get(2).intValue();
        if(ao.getCategoryAttrUpdates()!=null){
            attrCount+=ao.getCategoryAttrUpdates().size();
        }
        if(ao.getCustomAttrAdds()!=null){
            attrCount+=ao.getCustomAttrAdds().size();
        }
        if(ao.getCustomAttrDeletes()!=null){
            attrCount-=ao.getCustomAttrDeletes().size();
        }
        if(attrCount>MAX_CUSTOM_ATTR_NUMBER){
            throw new BusinessException(moreThanMaxCustomAttrNumber(MAX_CUSTOM_ATTR_NUMBER));
        }
        /**
         * 更新分类属性 - 使用 MP 的 updateById
         * */
            // 更新自定义属性 - 使用 MP 的 updateById
        if(CollUtil.isNotEmpty(ao.getCustomAttrUpdates())){
            for (UserShopSpuUpdateAO.CustomAttrAO attrAO : ao.getCustomAttrUpdates()) {
                GoodsCustomParam customParam = new GoodsCustomParam();
                customParam.setId(attrAO.getId());
                customParam.setAttrName(attrAO.getAttrName());
                customParam.setAttrValue(attrAO.getAttrValue());
                customParam.setSort(attrAO.getSort());
                goodsCustomParamMapper.updateById(customParam);
            }
        }

        // 删除自定义属性 - 使用 MP 的 deleteByIds（新版 API）
        if(CollUtil.isNotEmpty(ao.getCustomAttrDeletes())){
            goodsCustomParamMapper.deleteByIds(ao.getCustomAttrDeletes());
        }

        // 新增自定义属性 - 使用 MP 的 insert
        if(CollUtil.isNotEmpty(ao.getCustomAttrAdds())){
            for (UserShopSpuUpdateAO.CustomAttrAO attrAO : ao.getCustomAttrAdds()) {
                GoodsCustomParam customParam = new GoodsCustomParam();
                customParam.setSpuId(spuId);
                customParam.setAttrName(attrAO.getAttrName());
                customParam.setAttrValue(attrAO.getAttrValue());
                customParam.setSort(attrAO.getSort());
                goodsCustomParamMapper.insert(customParam);
            }
        }
    }
    @Transactional(rollbackFor = Exception.class)
    public void updateImgs(
            List<Long> counts,
            UserShopSpuUpdateAO ao,
            Long spuId){

        int imgCount = counts.get(0).intValue();

        if(ao.getImgUpdates()!=null){
            imgCount+=ao.getImgUpdates().size();
        }
        if(ao.getImgAdds()!=null) {
            imgCount+=ao.getImgAdds().size();
        }
        if(ao.getImgDeletes()!=null) {
            imgCount-=ao.getImgDeletes().size();
        }
        if(imgCount>MAX_IMAGE_NUMBER){
            throw new BusinessException(moreThanMaxImageNumber(MAX_IMAGE_NUMBER));
        }
        // 更新商品图片 - 使用 MP 的 updateById（JDBC 批处理优化）
        if(CollUtil.isNotEmpty(ao.getImgUpdates())) {
            for (UserShopSpuUpdateAO.ImageAO imageAO : ao.getImgUpdates()) {
                GoodsImage goodsImage = new GoodsImage();
                goodsImage.setImgId(imageAO.getImgId());
                goodsImage.setImgUrl(imageAO.getImgUrl());
                goodsImage.setDescription(imageAO.getDescription());
                goodsImage.setSort(imageAO.getSort());
                goodsImage.setIsMain(imageAO.getIsMain());
                goodsImageMapper.updateById(goodsImage);
            }
        }
        // 删除商品图片 - 使用 MP 的 deleteBatchIds
        if(CollUtil.isNotEmpty(ao.getImgDeletes())){
            goodsImageMapper.deleteByIds(ao.getImgDeletes());
        }

        // 新增商品图片 - 使用 MP 的 insert
        if(CollUtil.isNotEmpty(ao.getImgAdds())){
            for (UserShopSpuUpdateAO.ImageAO imageAO : ao.getImgAdds()) {
                GoodsImage goodsImage = new GoodsImage();
                goodsImage.setSpuId(spuId);
                goodsImage.setImgUrl(imageAO.getImgUrl());
                goodsImage.setDescription(imageAO.getDescription());
                goodsImage.setSort(imageAO.getSort());
                goodsImage.setIsMain(imageAO.getIsMain());
                goodsImageMapper.insert(goodsImage);
            }
        }

    }
    @Transactional(rollbackFor = Exception.class)
    public void updateTags(
            List<Long> counts,
            UserShopSpuUpdateAO ao,
            Long spuId){

        /**
         *检查标签数量是否过大
         * */
        int tagCount = counts.get(1).intValue();
        if(ao.getTagAdds()!=null) {
            tagCount+=ao.getTagAdds().size();
        }
        if(ao.getTagDeletes()!=null) {
            tagCount-=ao.getTagDeletes().size();
        }

        if(tagCount>MAX_TAG_NUMBER){
            throw new BusinessException(moreThanMaxTagNumber(MAX_TAG_NUMBER));
        }
        // 删除商品标签关联 - 使用 MP 的 LambdaQueryWrapper
        if(CollUtil.isNotEmpty(ao.getTagDeletes())){
            LambdaQueryWrapper<GoodsTagRelation> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(GoodsTagRelation::getSpuId, spuId)
                    .in(GoodsTagRelation::getTagId, ao.getTagDeletes());
            goodsTagRelationMapper.delete(wrapper);
        }
        if(CollUtil.isNotEmpty(ao.getTagAdds())){
            // 查询已存在的标签
            LambdaQueryWrapper<GoodsTag> tagWrapper = new LambdaQueryWrapper<>();
            tagWrapper.in(GoodsTag::getTagName,
                    ao.getTagAdds().stream()
                            .map(UserShopSpuUpdateAO.TagAO::getTagName)
                            .distinct()
                            .toList());
            List<GoodsTag> existTags = goodsTagMapper.selectList(tagWrapper);
            // 提取已存在的标签名
            Set<String> existTagNames = existTags.stream()
                    .map(GoodsTag::getTagName)
                    .collect(Collectors.toSet());

            // 分离需要新增的标签和已存在的标签
            List<UserShopSpuUpdateAO.TagAO> newTags = ao.getTagAdds().stream()
                    .filter(tag -> !existTagNames.contains(tag.getTagName()))
                    .toList();
            // 为已存在的标签建立关联
            if (CollUtil.isNotEmpty(existTags)) {
                List<Long> existTagIds = existTags.stream()
                        .map(GoodsTag::getId)
                        .toList();
                for (Long tagId : existTagIds) {
                    GoodsTagRelation relation = new GoodsTagRelation();
                    relation.setSpuId(spuId);
                    relation.setTagId(tagId);
                    goodsTagRelationMapper.insert(relation);
                }
            }

            // 插入新标签并建立关联
            if (CollUtil.isNotEmpty(newTags)) {
                for (UserShopSpuUpdateAO.TagAO tagAO : newTags) {
                    GoodsTag newTag = new GoodsTag();
                    newTag.setTagName(tagAO.getTagName());
                    newTag.setSort(tagAO.getSort());
                    goodsTagMapper.insert(newTag);

                    // 为新标签建立关联
                    GoodsTagRelation relation = new GoodsTagRelation();
                    relation.setSpuId(spuId);
                    relation.setTagId(newTag.getId());
                    goodsTagRelationMapper.insert(relation);
                }
            }
        }

    }
}
