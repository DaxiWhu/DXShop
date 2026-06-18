package com.daxi.service;

import com.daxi.domain.ao.GoodsCommentAO;
import com.daxi.domain.bo.GoodsCommentBO;

public interface IGoodsCommentService {
    GoodsCommentBO getGoodsCommentById(Long id);

    void sendComment(GoodsCommentAO ao);
}
