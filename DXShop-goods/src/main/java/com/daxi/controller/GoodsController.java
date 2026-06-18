package com.daxi.controller;

import cn.hutool.core.collection.CollUtil;
import com.daxi.annotation.AnonymousAccess;
import com.daxi.domain.ao.GetSpuPageAO;
import com.daxi.domain.ao.UserShopInsertAO;
import com.daxi.domain.ao.UserShopSpuUpdateAO;
import com.daxi.domain.dto.GoodsDetailDTO;
import com.daxi.domain.dto.GoodsSimpleDTO;
import com.daxi.domain.dto.GoodsToChangeDTO;
import com.daxi.result.Result;
import com.daxi.service.IGoodsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.daxi.response.CommonResponse.ID_ERROR;
import static com.daxi.response.CommonResponse.PARAM_EMPTY;
import static com.daxi.response.CommonResponse.SERVER_BUSY;
import static com.daxi.response.GoodsResponse.GOODS_NOT_EXIST;
import static com.daxi.response.OrderResponse.PARAM_ERROR;

@Validated
@RestController
@RequestMapping("/goods")
@RequiredArgsConstructor
public class GoodsController {
    private final @NonNull IGoodsService IGoodsService;

    public static final int MIN_ID_SCALE=1;
    public static final int ONCE_GOODS_QUERY_NUMBER=20;


    /**首页简要商品介绍加载*/
    @GetMapping("/simple")
    @AnonymousAccess
    public Result<List<GoodsSimpleDTO>> getSimpleGoodsByIds(
            @Valid
            @RequestParam
            @Size(max = ONCE_GOODS_QUERY_NUMBER, message =PARAM_ERROR)
            List<@Min(value = MIN_ID_SCALE, message = ID_ERROR) Long> ids) {
        List<GoodsSimpleDTO> goodsByIds = IGoodsService.getGoodsSimpleByIds(ids);
        if(CollUtil.isEmpty(goodsByIds)){
            return Result.fail(SERVER_BUSY);
        }
        return Result.success(goodsByIds);
    }


    /**商品详细展示1*/
    @GetMapping("/detail/{spuId}")
    @AnonymousAccess
    public Result<GoodsDetailDTO> getDetailGoodsByIds(
             @PathVariable
             @NotNull(message = PARAM_EMPTY)
             @Valid @Min(value = MIN_ID_SCALE, message = ID_ERROR)Long spuId) {
        GoodsDetailDTO goodsById = IGoodsService.getDetailGoodsById(spuId);
        if(goodsById==null){
            return Result.fail(GOODS_NOT_EXIST);
        }
        return Result.success(goodsById);
    }

    /**在商家修改商品的时候提供展示1*/
    @GetMapping("/shop/spu")
    public Result<List<GoodsToChangeDTO>> getGoodsForUserShopByShopId(
            @RequestBody
            @Valid GetSpuPageAO ao) {
        return Result.success(IGoodsService.getSpuIdByshopId(ao));
    }
    /**修改商品1*/
    @PutMapping("/shop/spu/{spuId}")
    public Result<Void> updateSpu(
            @PathVariable
            @Valid @Min(value = MIN_ID_SCALE, message = ID_ERROR)Long spuId,
            @Valid @RequestBody UserShopSpuUpdateAO ao) throws Exception {
        IGoodsService.updateSpu(spuId, ao);
        return Result.success();
    }
    /**添加商品1*/
    @PostMapping("/shop/spu")
    public Result<Void> addSpu(
            @Valid
            @RequestBody UserShopInsertAO ao) {
        IGoodsService.addSpu(ao);
        return Result.success();
    }

}
