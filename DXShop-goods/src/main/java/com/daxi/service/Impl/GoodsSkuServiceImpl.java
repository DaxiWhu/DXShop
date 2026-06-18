package com.daxi.service.Impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.daxi.Exception.BusinessException;
import com.daxi.domain.ao.UserShopSkuAO;
import com.daxi.domain.ao.UserShopSkuSpecAO;
import com.daxi.domain.ao.UserShopSpecNameAO;
import com.daxi.domain.bo.GoodsSkuBO;
import com.daxi.domain.bo.GoodsSkuSpecBO;
import com.daxi.domain.dto.UserCartSkuDTO;
import com.daxi.domain.entity.GoodsSku;
import com.daxi.domain.entity.GoodsSkuSpecRef;
import com.daxi.domain.entity.GoodsSpecName;
import com.daxi.domain.entity.GoodsSpecValue;
import com.daxi.domain.entity.GoodsSpu;
import com.daxi.mapper.GoodsSkuMapper;
import com.daxi.mapper.GoodsSkuSpecRefMapper;
import com.daxi.mapper.GoodsSpecNameMapper;
import com.daxi.mapper.GoodsSpecValueMapper;
import com.daxi.mapper.GoodsSpuMapper;
import com.daxi.service.IGoodsSkuService;
import com.daxi.util.PriceUtil;
import com.daxi.util.UserUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.daxi.limit.GoodsLimit.SPU_ON_SALE;
import static com.daxi.response.CommonResponse.PARAM_EMPTY;
import static com.daxi.response.CommonResponse.SERVER_BUSY;
import static com.daxi.response.OrderResponse.PARAM_ERROR;
import static com.daxi.response.UserResponse.NOT_LOGIN;


@Service
@RequiredArgsConstructor
public class GoodsSkuServiceImpl implements IGoodsSkuService {
    private final @NonNull GoodsSkuMapper goodsSkuMapper;
    private final @NonNull GoodsSpuMapper goodsSpuMapper;
    private final @NonNull GoodsSpecValueMapper goodsSpecValueMapper;
    private final @NonNull GoodsSpecNameMapper goodsSpecNameMapper;
    private final @NonNull GoodsSkuSpecRefMapper goodsSkuSpecRefMapper;
    /**
     * 可提供给comment的controller使用，用于展示评论的人买了什么规格的商品
     *
     * @param sku的id
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public GoodsSkuBO getSkuToChangeById(Long spuId) {
        Long shopId= UserUtil.getLocalShopId();
        if(shopId==null){
            throw new BusinessException(NOT_LOGIN);
        }
        LambdaQueryWrapper<GoodsSpu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(GoodsSpu::getSpuId,spuId)
                .eq(GoodsSpu::getShopId, shopId)
                .select(
                        GoodsSpu::getSpuId
                );
        GoodsSpu goodsSpu = goodsSpuMapper.selectOne(queryWrapper);
        if(goodsSpu==null){
            throw new BusinessException(SERVER_BUSY);
        }

        GoodsSkuBO skuSpec=new GoodsSkuBO();
        List<GoodsSkuBO.Sku> skus = goodsSkuMapper.listSkuToChangeBySpuId(spuId);

        if (CollUtil.isEmpty(skus)) {
            throw new BusinessException(SERVER_BUSY);
        }

        List<Long> ids = skus.stream().map(GoodsSkuBO.Sku::getSkuId).toList();
        List<GoodsSkuSpecBO> specs = goodsSkuMapper.listSkuSpecToChangeBySkuIds(ids);
        Map<Long, List<Long>> collect = specs.stream()
                .flatMap(spec -> spec.getSpecValues().stream())
                .collect(
                        Collectors.groupingBy(
                                GoodsSkuSpecBO.SkuSpecValue::getSkuId,
                                Collectors.mapping(GoodsSkuSpecBO.SkuSpecValue::getSpecValueId, Collectors.toList()))
                );
        skuSpec.setSpecList(specs);
        skus.forEach(sku -> sku.setSpecIds(collect.get(sku.getSkuId())));
        skuSpec.setSkuList(skus);
        return skuSpec;
    }

    /**
     * 选择具体sku
     *
     * @param spuId
     * @return
     */
    @Override
    @Transactional(readOnly = true)
    public GoodsSkuBO getSkuChoice(Long spuId) {
        GoodsSkuBO skuSpec=new GoodsSkuBO();
        List<GoodsSkuBO.Sku> skus = goodsSkuMapper.listSkuBySpuId(spuId);

        if (CollUtil.isEmpty(skus)) {
            throw new BusinessException(SERVER_BUSY);
        }
        List<Long> ids = skus.stream().map(GoodsSkuBO.Sku::getSkuId).toList();
        List<GoodsSkuSpecBO> specs = goodsSkuMapper.listSkuSpecBySkuIds(ids);
        Map<Long, List<Long>> collect = specs.stream()
                .flatMap(spec -> spec.getSpecValues().stream())
                .collect(
                        Collectors.groupingBy(
                                GoodsSkuSpecBO.SkuSpecValue::getSkuId,
                                Collectors.mapping(GoodsSkuSpecBO.SkuSpecValue::getSpecValueId, Collectors.toList()))
                );
        skuSpec.setSpecList(specs);
        skus.forEach(sku -> sku.setSpecIds(collect.get(sku.getSkuId())));
        skuSpec.setSkuList(skus);
        return skuSpec;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSpecName(Long spuId, List<UserShopSpecNameAO> name) {
        Long shopId= UserUtil.getLocalShopId();
        if(shopId==null){
            throw new BusinessException(NOT_LOGIN);
        }
        LambdaQueryWrapper<GoodsSpu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(GoodsSpu::getSpuId,spuId)
                .eq(GoodsSpu::getShopId, shopId)
                .select(GoodsSpu::getSpuId,GoodsSpu::getStatus);
        GoodsSpu goodsSpu = goodsSpuMapper.selectOne(queryWrapper);
        if(goodsSpu==null){
            throw new BusinessException(SERVER_BUSY);
        }
        //检查是否正在销售或者已经有specName了
        if(goodsSpu.getStatus()==SPU_ON_SALE){
            throw new BusinessException(PARAM_ERROR);
        }
        LambdaQueryWrapper<GoodsSpecName> specNameQueryWrapper = new LambdaQueryWrapper<>();
        specNameQueryWrapper.eq(GoodsSpecName::getSpuId, spuId);
        if(goodsSpecNameMapper.selectCount(specNameQueryWrapper)>0){
            throw new BusinessException(PARAM_ERROR);
        }
        List<GoodsSpecName> specNames = new ArrayList<>();

        for(UserShopSpecNameAO nameAO:name){
            GoodsSpecName goodsSpecName = new GoodsSpecName();
            goodsSpecName.setSpuId(spuId);
            goodsSpecName.setSpecName(nameAO.getSpecName());
            goodsSpecName.setSort(nameAO.getSort());
           specNames.add(goodsSpecName);
        }
        goodsSpecNameMapper.insert(specNames);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAndUpdateSkuSpec(Long spuId, UserShopSkuSpecAO ao) {
        Long shopId = UserUtil.getLocalShopId();
        if (shopId == null) {
            throw new BusinessException(NOT_LOGIN);
        }
        LambdaQueryWrapper<GoodsSpu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(GoodsSpu::getSpuId, spuId)
                .eq(GoodsSpu::getShopId, shopId)
                .select(GoodsSpu::getSpuId, GoodsSpu::getStatus);
        GoodsSpu goodsSpu = goodsSpuMapper.selectOne(queryWrapper);
        if (goodsSpu == null) {
            throw new BusinessException(SERVER_BUSY);
        }
        if (CollUtil.isEmpty(ao.getSpecUpdates()) || CollUtil.isEmpty(ao.getSpecAdds())) {
            throw new BusinessException(PARAM_EMPTY);
        }
// 处理更新
        if (CollUtil.isNotEmpty(ao.getSpecUpdates())) {
            List<GoodsSpecValue> updateList = new ArrayList<>();
            for (UserShopSkuSpecAO.SpecAO specUpdate : ao.getSpecUpdates()) {
                GoodsSpecValue specValue = new GoodsSpecValue();
                specValue.setSpecId(specUpdate.getNameId());
                specValue.setId(specUpdate.getValueId());
                specValue.setSpecValue(specUpdate.getSpecValue());
                specValue.setSort(specUpdate.getValueSort());
                updateList.add(specValue);
            }
            goodsSpecValueMapper.updateById(updateList); // 或使用 batch update
        }

// 处理新增
        if (CollUtil.isNotEmpty(ao.getSpecAdds())) {
            List<GoodsSpecValue> insertList = new ArrayList<>();
            for (UserShopSkuSpecAO.SpecAO specAdd : ao.getSpecAdds()) {
                GoodsSpecValue specValue = new GoodsSpecValue();
                specValue.setSpecId(specAdd.getNameId());
                // 注意:新增时不要设置id,让数据库自动生成
                specValue.setSpecValue(specAdd.getSpecValue());
                specValue.setSort(specAdd.getValueSort());
                insertList.add(specValue);
            }
            goodsSpecValueMapper.insert(insertList);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addAndUpdateSku(Long spuId, List<UserShopSkuAO> aos) {
        Long shopId = UserUtil.getLocalShopId();
        if (shopId == null) {
            throw new BusinessException(NOT_LOGIN);
        }
        LambdaQueryWrapper<GoodsSpu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(GoodsSpu::getSpuId, spuId)
                .eq(GoodsSpu::getShopId, shopId)
                .select(GoodsSpu::getSpuId, GoodsSpu::getStatus);
        GoodsSpu goodsSpu = goodsSpuMapper.selectOne(queryWrapper);
        if (goodsSpu == null) {
            throw new BusinessException(SERVER_BUSY);
        }
        
        List<UserShopSkuAO> updateAos = aos.stream().filter(ao -> ao.getSkuId() != null).toList();
        List<UserShopSkuAO> addAos = aos.stream().filter(ao -> ao.getSkuId() == null).toList();
        
        // 使用 MyBatis-Plus 批量更新
        if(!CollUtil.isEmpty(updateAos)){
            List<GoodsSku> updateList = updateAos.stream().map(ao -> {
                GoodsSku sku = new GoodsSku();
                sku.setSkuId(ao.getSkuId());
                sku.setSpuId(spuId);
                sku.setPrice(PriceUtil.yuanToFen(ao.getYuan()));
                sku.setStock(ao.getStock());
                sku.setBarCode(ao.getBarCode());
                sku.setSkuSpec(ao.getSkuSpec());
                sku.setStatus(ao.getStatus());
                return sku;
            }).toList();
            goodsSkuMapper.updateById(updateList);
        }
        
        // 使用 MyBatis-Plus 批量插入
        if(!CollUtil.isEmpty(addAos)){
            List<GoodsSku> insertList = addAos.stream().map(ao -> {
                GoodsSku sku = new GoodsSku();
                sku.setSpuId(spuId);
                sku.setPrice(PriceUtil.yuanToFen(ao.getYuan()));
                sku.setStock(ao.getStock());
                sku.setBarCode(ao.getBarCode());
                sku.setSkuSpec(ao.getSkuSpec());
                sku.setStatus(ao.getStatus());
                return sku;
            }).toList();
            goodsSkuMapper.insert(insertList);
            
            // 插入 SKU 规格关联关系
            List<GoodsSkuSpecRef> specRefList = new ArrayList<>();
            for (int i = 0; i < addAos.size(); i++) {
                UserShopSkuAO.SpecRelation specRelation = addAos.get(i).getSpecs();
                Long generatedSkuId = insertList.get(i).getSkuId(); // 获取数据库生成的主键
                    for (Long specValueId : specRelation.getSpecIds()) {
                        GoodsSkuSpecRef ref = new GoodsSkuSpecRef();
                        ref.setSkuId(generatedSkuId);
                        ref.setSpecValueId(specValueId);
                        specRefList.add(ref);
                    }

            }
            goodsSkuSpecRefMapper.insert(specRefList);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserCartSkuDTO getSkuForUserCartById(Long skuId, Integer buyNum) {
        LambdaQueryWrapper<GoodsSku> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .eq(GoodsSku::getSkuId,skuId)
                .select(
                        GoodsSku::getSpuId,
                        GoodsSku::getSkuSpec,
                        GoodsSku::getPrice
                );
        GoodsSku sku = goodsSkuMapper.selectOne(queryWrapper);
        if(sku==null){
            throw new BusinessException(PARAM_ERROR);
        }
        LambdaQueryWrapper<GoodsSpu> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1
                .eq(GoodsSpu::getSpuId,sku.getSpuId())
                .select(GoodsSpu::getTitle,GoodsSpu::getMainImg);
        GoodsSpu spu = goodsSpuMapper.selectOne(queryWrapper1);

        UserCartSkuDTO userCartSkuDTO = new UserCartSkuDTO();
        userCartSkuDTO.setSpuId(sku.getSpuId());
        userCartSkuDTO.setTitle(spu.getTitle());
        userCartSkuDTO.setMainImg(spu.getMainImg());
        userCartSkuDTO.setSkuSpec(sku.getSkuSpec());
        userCartSkuDTO.setPrice(PriceUtil.fenToYuan(sku.getPrice()*buyNum));

        return userCartSkuDTO;
    }

}
