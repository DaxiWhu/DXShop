package com.daxi.service.Impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.daxi.Exception.BusinessException;
import com.daxi.converter.GoodsBoToGoodsToChangeDtoConverter;
import com.daxi.converter.GoodsCustomParamToDtoConverter;
import com.daxi.converter.GoodsImageToDtoConverter;
import com.daxi.domain.ao.GetSpuPageAO;
import com.daxi.domain.ao.UserShopInsertAO;
import com.daxi.domain.ao.UserShopSpuUpdateAO;
import com.daxi.domain.bo.GoodsCategoryAttrBO;
import com.daxi.domain.bo.GoodsTagBO;
import com.daxi.domain.dto.GoodsCategoryAttrDTO;
import com.daxi.domain.dto.GoodsDetailDTO;
import com.daxi.domain.dto.GoodsSimpleDTO;
import com.daxi.domain.dto.GoodsTagDTO;
import com.daxi.domain.dto.GoodsToChangeDTO;
import com.daxi.domain.entity.GoodsCategoryParam;
import com.daxi.domain.entity.GoodsCustomParam;
import com.daxi.domain.entity.GoodsImage;
import com.daxi.domain.entity.GoodsParam;
import com.daxi.domain.entity.GoodsSpu;
import com.daxi.feign.UserFeignClient;
import com.daxi.mapper.GoodsCategoryParamMapper;
import com.daxi.mapper.GoodsCustomParamMapper;
import com.daxi.mapper.GoodsImageMapper;
import com.daxi.mapper.GoodsParamMapper;
import com.daxi.mapper.GoodsSpuMapper;
import com.daxi.mapper.GoodsTagMapper;
import com.daxi.service.IGoodsService;
import com.daxi.util.PriceUtil;
import com.daxi.util.UserUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.daxi.response.CommonResponse.TRY_AGAIN;
import static com.daxi.response.GoodsResponse.GOODS_NOT_EXIST;
import static com.daxi.response.GoodsResponse.NOT_ENOUGH_GOODS_SPEC_NAME;
import static com.daxi.response.UserResponse.NOT_LOGIN;


@Service
@Slf4j
@RequiredArgsConstructor
public class GoodsServiceImpl implements IGoodsService {

    private final @NonNull GoodsSpuMapper goodsSpuMapper;
    private final @NonNull GoodsImageMapper goodsImageMapper;
    private final @NonNull GoodsTagMapper goodsTagMapper;
    private final @NonNull GoodsCustomParamMapper goodsCustomParamMapper;
    private final @NonNull GoodsParamMapper goodsParamMapper;
    private final @NonNull GoodsCategoryParamMapper goodsCategoryParamMapper;
    private final @NonNull GoodsImageToDtoConverter goodsImageToDtoConverter;
    private final @NonNull GoodsCustomParamToDtoConverter goodsCustomParamToDtoConverter;
    private final @NonNull GoodsBoToGoodsToChangeDtoConverter goodsBoToGoodsToChangeDtoConverter;
    private final @NonNull ServiceImpl service;
    private final @NonNull UserFeignClient userFeignClient;

    @Override
    @Transactional(readOnly = true)
    public GoodsDetailDTO getDetailGoodsById(Long id) {

        GoodsDetailDTO goodsSpu = goodsSpuMapper.getDetailGoodsById(id);
        if(goodsSpu== null){
            throw new BusinessException(GOODS_NOT_EXIST);
        }
        //最终组装
        LambdaQueryWrapper<GoodsImage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GoodsImage::getSpuId, id).orderByDesc(GoodsImage::getSort);
        List<GoodsImage> images = goodsImageMapper.selectList(queryWrapper);
        goodsSpu.setImgs(images == null ? Collections.emptyList() : goodsImageToDtoConverter.toDtos(images));

        List<GoodsTagDTO> tags = goodsTagMapper.listTagsBySpuId(id);
        goodsSpu.setTags(tags == null ? Collections.emptyList() : tags);

        List<GoodsCategoryAttrDTO> categoryAttrs = goodsSpuMapper.listCategoryAttrsBySpuId(id);
        goodsSpu.setCategoryAttrs(categoryAttrs==null?Collections.emptyList():categoryAttrs);

        LambdaQueryWrapper<GoodsCustomParam> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(GoodsCustomParam::getSpuId,id)
                .orderByDesc(GoodsCustomParam::getSort)
                .select(
                        GoodsCustomParam::getAttrName,
                        GoodsCustomParam::getAttrValue
                );

        List<GoodsCustomParam> customParams=goodsCustomParamMapper.selectList(lambdaQueryWrapper);
        goodsSpu.setCustomAttrs(customParams==null?Collections.emptyList():
                goodsCustomParamToDtoConverter.toDtos(customParams));
        return goodsSpu;
    }




    @Override
    @Transactional(readOnly = true)
    public List<GoodsSimpleDTO> getGoodsSimpleByIds(List<Long> ids) {
        List<GoodsSimpleDTO> goodsSimpleByIds = goodsSpuMapper.getGoodsSimpleByIds(ids);
        if(CollUtil.isEmpty(goodsSimpleByIds)){
            return Collections.emptyList();
        }
        List<Long> spuIds = goodsSimpleByIds.stream().map(GoodsSimpleDTO::getSpuId).toList();
        Map<Long, List<GoodsTagBO>> tagsMap = goodsTagMapper.listTagsBySpuIds(spuIds)
                .stream()
                .collect(Collectors.groupingBy(GoodsTagBO::getSpuId));
        goodsSimpleByIds.forEach(
                goodsSimpleById -> {
                    List<GoodsTagBO> tags = tagsMap.getOrDefault(goodsSimpleById.getSpuId(), Collections.emptyList());
                    String tag = tags.stream()
                            .map(GoodsTagBO::getTagName)  // 提取标签名称
                            .filter(Objects::nonNull)     // 过滤null
                            .collect(Collectors.joining(","));
                    goodsSimpleById.setTags(tag);
                }
        );
        return goodsSimpleByIds;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoodsToChangeDTO> getSpuIdByshopId(GetSpuPageAO ao) {
        Long shopId= UserUtil.getLocalShopId();
        if(shopId==null){
            throw new BusinessException(NOT_LOGIN);
        }
        int offset = (ao.getPageNum() - 1) * ao.getPageSize();
        List<GoodsToChangeDTO> goodsSpus=goodsSpuMapper.getGoodsToChangeByShopId(shopId, offset, ao.getPageSize());
        if(CollUtil.isEmpty(goodsSpus)){
            return Collections.emptyList();
        }
        List<Long> ids=goodsSpus.stream().map(GoodsToChangeDTO::getSpuId).toList();

        Map<Long, List<GoodsToChangeDTO.ImageDTO>> imgsMap = goodsSpuMapper.listImgsBySpuIds(ids)
                .stream()
                .collect(Collectors.groupingBy(
                        GoodsImage::getSpuId,
                        Collectors.mapping(goodsBoToGoodsToChangeDtoConverter::toImageDto, Collectors.toList())));

        Map<Long, List<GoodsToChangeDTO.CategoryAttrDTO>> attrsMap = goodsSpuMapper.listCategoryAttrsBySpuIds(ids)
                .stream()
                .collect(Collectors.groupingBy(
                        GoodsCategoryAttrBO::getSpuId,
                        Collectors.mapping(
                                goodsBoToGoodsToChangeDtoConverter::toAttrDto,
                                Collectors.toList()
                        )));

        Map<Long, List<GoodsToChangeDTO.CustomParamDTO>> customParamsMap = goodsSpuMapper.listCustomParamsBySpuIds(ids)
                .stream()
                .collect(Collectors.groupingBy(
                        GoodsCustomParam::getSpuId,
                        Collectors.mapping(
                                goodsBoToGoodsToChangeDtoConverter::toCustomParamDto,
                                Collectors.toList()
                        )));

        Map<Long, List<GoodsToChangeDTO.TagDTO>> tagsMap = goodsTagMapper.listTagsBySpuIds(ids)
                .stream()
                .collect(Collectors.groupingBy(
                        GoodsTagBO::getSpuId,
                        Collectors.mapping(
                                goodsBoToGoodsToChangeDtoConverter::toTagDto,
                                Collectors.toList()
                        )));

        goodsSpus.forEach(
                goodsSpu -> {
                    goodsSpu.setImgs(imgsMap.getOrDefault(goodsSpu.getSpuId(), Collections.emptyList()));
                    goodsSpu.setCategoryAttrs(attrsMap.getOrDefault(goodsSpu.getSpuId(), Collections.emptyList()));
                    goodsSpu.setCustomAttrs(customParamsMap.getOrDefault(goodsSpu.getSpuId(), Collections.emptyList()));
                    goodsSpu.setTags(tagsMap.getOrDefault(goodsSpu.getSpuId(), Collections.emptyList()));
                }
        );
        return goodsSpus;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSpu(Long spuId, UserShopSpuUpdateAO ao) throws Exception {
        Long shopId= UserUtil.getLocalShopId();
        if(shopId==null){
            throw new BusinessException(NOT_LOGIN);
        }
        LambdaQueryWrapper<GoodsSpu> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(GoodsSpu::getSpuId,spuId)
                .eq(GoodsSpu::getShopId,shopId);
        GoodsSpu goodsSpu = goodsSpuMapper.selectOne(lambdaQueryWrapper);
        if(goodsSpu==null){
            throw new BusinessException(GOODS_NOT_EXIST);
        }
        Integer fen=PriceUtil.yuanToFen(ao.getPrice());

        List<Long> counts = goodsSpuMapper.countAllForUpdate(spuId);

        if(CollUtil.isEmpty(counts)){
            throw new BusinessException(TRY_AGAIN);
        }
        /**
        /**更新图片*/
        service.updateImgs(counts,ao,spuId);
        /**更新标签*/
        service.updateTags(counts,ao,spuId);
        /**更新自定义属性*/
        service.updateAttrs(counts,ao,spuId);

        GoodsSpu goodsSpu1 = new GoodsSpu();
        goodsSpu1.setSpuId(spuId);
        goodsSpu1.setTitle(ao.getTitle());
        goodsSpu1.setSubTitle(ao.getSubTitle());
        goodsSpu1.setMainImg(ao.getMainImg());
        goodsSpu1.setPrice(fen);
        goodsSpu1.setStatus(ao.getStatus());
        goodsSpuMapper.updateById(goodsSpu1);

        // 更新分类属性 - 使用 MP 的 updateById
        if(CollUtil.isNotEmpty(ao.getCategoryAttrUpdates())){
            for (UserShopSpuUpdateAO.CategoryAttrAO attrAO : ao.getCategoryAttrUpdates()) {
                GoodsParam goodsParam = new GoodsParam();
                goodsParam.setId(attrAO.getId());
                goodsParam.setParamValue(attrAO.getAttrValue());
                goodsParamMapper.updateById(goodsParam);
            }
        }
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSpu(UserShopInsertAO ao) {
        Long shopId= UserUtil.getLocalShopId();
        String shopName=userFeignClient.getShopName(shopId);
        // 使用 MP 插入 SPU
        GoodsSpu goodsSpu = new GoodsSpu();
        goodsSpu.setShopId(shopId);
        goodsSpu.setShopName(shopName);
        goodsSpu.setCategoryId(ao.getCategoryId());
        goodsSpu.setBrand(ao.getBrand());
        goodsSpu.setTitle(ao.getTitle());
        goodsSpu.setSubTitle(ao.getSubTitle());
        goodsSpu.setMainImg(ao.getMainImg());
        goodsSpu.setPrice(PriceUtil.yuanToFen(ao.getYuan()));
        goodsSpu.setStatus(ao.getStatus());
        goodsSpuMapper.insert(goodsSpu);
            
        // 获取生成的 spuId
        Long spuId = goodsSpu.getSpuId();

        List<UserShopInsertAO.CategoryAttrAO> customAttrInserts = ao.getCategoryAttrs();
        LambdaQueryWrapper<GoodsCategoryParam> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(GoodsCategoryParam::getCategoryId, ao.getCategoryId())
                .select(GoodsCategoryParam::getParamId);
        List<GoodsCategoryParam> goodsCategoryParamss = goodsCategoryParamMapper.selectList(lambdaQueryWrapper);
        Integer size = goodsCategoryParamss.size();
        //比较两个
        List<Long> paramIds = goodsCategoryParamss
                .stream()
                .map(GoodsCategoryParam::getParamId)
                .toList();
        List<Long> templateIds = ao.getCategoryAttrs()
                .stream()
                .map(UserShopInsertAO.CategoryAttrAO::getTemplateId)
                .toList();
        if(CollUtil.isEqualList(paramIds,templateIds)){
            throw new BusinessException(NOT_ENOUGH_GOODS_SPEC_NAME);
        }
        if(!size.equals( customAttrInserts.size())){
            throw new BusinessException(NOT_ENOUGH_GOODS_SPEC_NAME);
        }

        // 使用 MP 批量插入商品参数
        if(CollUtil.isNotEmpty(customAttrInserts)){
            for (UserShopInsertAO.CategoryAttrAO attrAO : customAttrInserts) {
                GoodsParam goodsParam = new GoodsParam();
                goodsParam.setSpuId(spuId);
                goodsParam.setTemplateId(attrAO.getTemplateId());
                goodsParam.setParamValue(attrAO.getParamValue());
                goodsParamMapper.insert(goodsParam);
            }
        }
    }
}
