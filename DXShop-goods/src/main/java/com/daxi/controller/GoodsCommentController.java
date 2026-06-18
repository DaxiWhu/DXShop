package com.daxi.controller;

import com.daxi.domain.ao.GoodsCommentAO;
import com.daxi.domain.dto.GoodsCommentDTO;
import com.daxi.result.Result;
import com.daxi.converter.GoodsCommentBoToDto;
import com.daxi.domain.bo.GoodsCommentBO;
import com.daxi.service.IGoodsCommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.daxi.response.CommonResponse.ID_ERROR;
import static com.daxi.response.CommonResponse.PARAM_EMPTY;

@RestController
@RequestMapping("/goods/comment")
@RequiredArgsConstructor
public class GoodsCommentController {
    private final @NonNull IGoodsCommentService goodsCommentService;
    private final @NonNull GoodsCommentBoToDto goodsCommentBoToDto;


    public static final int MIN_ID_SCALE=1;
    /**
     * 获取评论
     */
    @GetMapping("/{spuId}")
    public Result<GoodsCommentDTO> getGoodsCommentById(
            @PathVariable
            @NotNull(message = PARAM_EMPTY)
            @Valid @Min(value = MIN_ID_SCALE, message = ID_ERROR) Long spuId) {
        GoodsCommentBO goodsCommentById = goodsCommentService.getGoodsCommentById(spuId);
        GoodsCommentDTO goodsCommentDTO = goodsCommentBoToDto.toGoodsCommentDTO(goodsCommentById);
        return Result.success(goodsCommentDTO);
    }


    /**
     * 发送评论（只有根据已确定收货订单跳转才能进行评论）
     */
    @PostMapping()
    public Result<Void> sendComment(
            @RequestBody
            @Valid @NotNull(message = PARAM_EMPTY)GoodsCommentAO ao){
        goodsCommentService.sendComment(ao);
        return Result.success();
    }

}
