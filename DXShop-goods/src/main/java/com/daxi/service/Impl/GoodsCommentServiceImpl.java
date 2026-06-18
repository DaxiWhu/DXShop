package com.daxi.service.Impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.daxi.Exception.BusinessException;
import com.daxi.converter.GoodsCommentStatToBo;
import com.daxi.domain.ao.GoodsCommentAO;
import com.daxi.domain.bo.GoodsCommentBO;
import com.daxi.domain.dto.SendCommentDTO;
import com.daxi.domain.entity.GoodsComment;
import com.daxi.domain.entity.GoodsCommentStat;
import com.daxi.domain.entity.GoodsSpu;
import com.daxi.domain.entity.GoodsTagCommentRelation;
import com.daxi.feign.OrderFeignClient;
import com.daxi.feign.UserFeignClient;
import com.daxi.mapper.GoodsCommentMapper;
import com.daxi.mapper.GoodsCommentStatMapper;
import com.daxi.mapper.GoodsSpuMapper;
import com.daxi.mapper.GoodsTagCommentRelationMapper;
import com.daxi.service.IGoodsCommentService;
import com.daxi.util.OssUtil;
import com.daxi.util.UserUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.daxi.limit.GoodsLimit.COMMENT_SHOW;
import static com.daxi.response.CommonResponse.SERVER_BUSY;
import static com.daxi.response.UserResponse.NOT_LOGIN;

@Service
@RequiredArgsConstructor
public class GoodsCommentServiceImpl implements IGoodsCommentService {
    private final @NonNull GoodsCommentMapper goodsCommentMapper;
    private final @NonNull GoodsCommentStatMapper goodsCommentStatMapper;
    private final @NonNull GoodsTagCommentRelationMapper goodsTagCommentRelationMapper;
    private final @NonNull GoodsCommentStatToBo goodsCommentStatToBo;
    private final @NonNull UserFeignClient userFeignClient;
    private final @NonNull OrderFeignClient orderFeignClient;
    private final @NonNull OssUtil ossUtil;
    private final @NonNull GoodsSpuMapper goodsSpuMapper;
    @Override
    @Transactional(readOnly = true)
    public GoodsCommentBO getGoodsCommentById(Long id) {
        List<GoodsComment> comments = goodsCommentMapper.getGoodsCommentById(id);
        if(CollUtil.isEmpty(comments)){
            return null;
        }
        //把匿名处理了
        comments.forEach(comment -> {
            if(comment.getIsAnonymous() == 1){
                comment.setUserName("匿名用户");
            }
        });
        for(GoodsComment comment : comments){
            List<String> pictures= JSONUtil.toList(comment.getPictures(), String.class);
            List<String> tempAccessUrls = ossUtil.getTempAccessUrls(pictures);
            comment.setPictures(JSONUtil.toJsonStr(tempAccessUrls));
        }
        //把全量评论放入
        GoodsCommentBO commentBO = new GoodsCommentBO();
        commentBO.setCommentList(comments);
        //获取标签
        List<GoodsCommentStat> commentStats = goodsCommentStatMapper.getGoodsCommentStatById(id);
        if(CollUtil.isEmpty(commentStats)){
            return commentBO;
        }

        List<GoodsCommentBO.GoodsCommentStatBO> commentStatBOs = goodsCommentStatToBo.toBoList(commentStats);
        //获取标签的相关评论
        List<GoodsTagCommentRelation> tagCommentRelations = goodsTagCommentRelationMapper.getRelationById(id);
        Map<Long, GoodsComment> commentMap = comments
                .stream()
                .collect(Collectors.toMap(GoodsComment::getCommentId, Function.identity()));
        Map<Long, List<Long>> relationMap = tagCommentRelations
                .stream()
                .collect(
                        Collectors.groupingBy(
                                GoodsTagCommentRelation::getTagId,
                                Collectors.mapping(GoodsTagCommentRelation::getCommentId, Collectors.toList())
                        ));
        //开始从Map里面取评论
        commentStatBOs.forEach(
                commentStatBO -> {
                    List<Long> commentIds = relationMap.get(commentStatBO.getCommentStat().getId());
                    commentStatBO.setComments(CollUtil.newArrayList());
                    commentIds.forEach(
                            commentId -> {
                                GoodsComment comment = commentMap.get(commentId);
                                commentStatBO.getComments().add(comment);
                            }
                    );
                    commentStatBO.getComments().sort((o1,o2)->
                            o2.getCreatedAt().compareTo(o1.getCreatedAt()));
                }
        );
        List<GoodsCommentBO.GoodsCommentStatBO> result = commentStatBOs.stream().filter(
                commentStatBO -> !CollectionUtils.isEmpty(commentStatBO.getComments())).toList();
        commentBO.setCommentStats(result);
        return commentBO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sendComment(GoodsCommentAO ao) {
        Long userId= UserUtil.getLocalUserId();
        if(userId==null){
            throw new BusinessException(NOT_LOGIN);
        }
        LambdaQueryWrapper<GoodsSpu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GoodsSpu::getSpuId,ao.getSpuId()).select(GoodsSpu::getShopId);
        GoodsSpu spu = goodsSpuMapper.selectOne(queryWrapper);
        if(spu==null){
            throw new BusinessException(SERVER_BUSY);
        }
        SendCommentDTO informationForSendCommentFromUser = userFeignClient.getInformationForSendComment(userId, spu.getShopId(), ao.getOrderId(), ao.getSpuId(), ao.getSkuId());
        SendCommentDTO informationForSendCommentFromOrder = orderFeignClient.getInformationForSendComment(userId, ao.getOrderId(), ao.getSpuId(), ao.getSkuId());

        /**从两个统计表里面把两个关系字段查出来，然后再进行写入*/
        if(informationForSendCommentFromUser==null){
            throw new BusinessException(SERVER_BUSY);
        }
        GoodsComment goodsComment = new GoodsComment();
        goodsComment.setSpuId(ao.getSpuId());
        goodsComment.setSkuId(ao.getSkuId());
        goodsComment.setSkuDsc(informationForSendCommentFromUser.getSkuSpec());
        goodsComment.setUserId(userId);
        goodsComment.setUserName(informationForSendCommentFromUser.getUserName());
        goodsComment.setUserAvatar(informationForSendCommentFromUser.getUserAvatar());
        goodsComment.setOrderId(ao.getOrderId());
        goodsComment.setContent(ao.getContent());
        goodsComment.setScore(ao.getScore());
        goodsComment.setIsAnonymous(ao.getIsAnonymous() != null ? ao.getIsAnonymous() : 0);
        // 将图片列表转换为JSON字符串
        if (ao.getPictures() != null && !ao.getPictures().isEmpty()) {
            goodsComment.setPictures(cn.hutool.json.JSONUtil.toJsonStr(ao.getPictures()));
        }
        // 设置复购状态（从用户服务获取）
        goodsComment.setIsRepurchase(informationForSendCommentFromUser.getIsRepurchase() != null ? informationForSendCommentFromUser.getIsRepurchase() : 0);
        goodsComment.setIsShopReturnCustomer(informationForSendCommentFromUser.getIsShopReturnCustomer() != null ? informationForSendCommentFromUser.getIsShopReturnCustomer() : 0);
        goodsComment.setStatus(COMMENT_SHOW);
            
        // 插入评论
        goodsCommentMapper.insert(goodsComment);
    }
}
