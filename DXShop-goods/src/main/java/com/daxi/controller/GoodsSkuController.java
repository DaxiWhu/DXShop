package com.daxi.controller;

import com.daxi.annotation.InternalApi;
import com.daxi.converter.GoodsSkuBoToDto;
import com.daxi.converter.GoodsSkuBoToToChangeDTO;
import com.daxi.domain.ao.UserShopSkuAO;
import com.daxi.domain.ao.UserShopSkuSpecAO;
import com.daxi.domain.ao.UserShopSpecNameAO;
import com.daxi.domain.bo.GoodsSkuBO;
import com.daxi.domain.dto.GoodsSkuDTO;
import com.daxi.domain.dto.GoodsSkuToChangeDTO;
import com.daxi.domain.dto.UserCartSkuDTO;
import com.daxi.result.Result;
import com.daxi.service.IGoodsSkuService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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
import static com.daxi.response.OrderResponse.PARAM_ERROR;

@RestController
@RequestMapping("/goods/sku")
@RequiredArgsConstructor
public class GoodsSkuController {
    private final @NonNull IGoodsSkuService goodsSkuService;
    private final @NonNull GoodsSkuBoToDto goodsSkuBoToDto;
    private final @NonNull GoodsSkuBoToToChangeDTO goodsSkuBoToToChangeDTO;

    public static final int ONCE_GOODS_QUERY_NUMBER= 20;
    public static final int MIN_ID_SCALE=1;
    public static final int SPEC_NAME_MAX_NUMBER=5;
    /**
     * 选具体规格时获取相应的展示1
     */
    @GetMapping("/{spuId}")
    public Result<GoodsSkuDTO> getSkuChoiceById(
            @PathVariable
            @NotNull(message = PARAM_EMPTY)
            @Valid @Min(value = MIN_ID_SCALE, message = ID_ERROR)Long spuId) {
        GoodsSkuBO skuById = goodsSkuService.getSkuChoice(spuId);
        GoodsSkuDTO skuDTO = goodsSkuBoToDto.toDto(skuById);
        return Result.success(skuDTO);
    }
/**商家修改的时候提 供展示*/
    @GetMapping("/shop/{spuId}")
    public Result<GoodsSkuToChangeDTO> getSkuForUserShopToChangeById(
            @PathVariable
            @NotNull(message = PARAM_EMPTY)
            @Valid @Min(value = MIN_ID_SCALE, message = ID_ERROR)Long spuId) {
        GoodsSkuBO skuById = goodsSkuService.getSkuToChangeById(spuId);
        GoodsSkuToChangeDTO dto = goodsSkuBoToToChangeDTO.toDto(skuById);
        return Result.success(dto);
    }
    /**
     * 规格名的新增，只允许在商品刚刚创建的时候操作1
     * */
    @PostMapping("/sku-spec-name/{spuId}")
    public Result<Void> addSkuSpecName(
            @Valid @NotNull
            @Min(value = MIN_ID_SCALE, message = ID_ERROR)
            @PathVariable Long spuId,
            @Valid @NotNull @RequestBody
            @Size(max =SPEC_NAME_MAX_NUMBER, message = PARAM_EMPTY )
            List<UserShopSpecNameAO> name){
        goodsSkuService.addSpecName(spuId,name);
        return Result.success();
    }
    /**
     * 增加或者更新商品规格值1
     * */
    @PostMapping("/sku-spec-value/{spuId}")
    public Result<Void> addAndUpdateSkuSpec(
            @Valid @Min(value = MIN_ID_SCALE, message = ID_ERROR)
            @PathVariable Long spuId,
            @Valid @RequestBody UserShopSkuSpecAO ao){
        goodsSkuService.addAndUpdateSkuSpec(spuId,ao);
        return Result.success();
    }
    /**
     * 增加商品的sku
     * 如果想要删除sku，请把status设置为0
     * 1
     * */
    @PutMapping("/sku/{spuId}")
    public Result<Void> addAndUpdateSku(
            @NotNull
            @Valid @Min(value = MIN_ID_SCALE, message = ID_ERROR)
            @PathVariable Long spuId,
            @NotNull
            @Valid @RequestBody
            @Size(max =SPEC_NAME_MAX_NUMBER, message = PARAM_ERROR )
            List<UserShopSkuAO> aos){
        goodsSkuService.addAndUpdateSku(spuId,aos);
        return Result.success();
    }

    /**
     * 为了加入购物车获取sku信息1
     * */
    @GetMapping("/api")
    @InternalApi
    public UserCartSkuDTO getSkuForUserCartById(
            @NotNull
            @Valid @Min(value = MIN_ID_SCALE, message = ID_ERROR)
            @RequestParam
            Long skuId,
            @NotNull
            @Valid @Min(value = MIN_ID_SCALE, message = PARAM_ERROR)
            @RequestParam
            Integer buyNum) {
        return goodsSkuService.getSkuForUserCartById(skuId, buyNum);
    }
}
