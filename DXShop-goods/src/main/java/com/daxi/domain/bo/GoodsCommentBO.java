package com.daxi.domain.bo;

import com.daxi.domain.entity.GoodsComment;
import com.daxi.domain.entity.GoodsCommentStat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class GoodsCommentBO {
    private List<GoodsComment> commentList;     //全部评论

    private List<GoodsCommentStatBO> commentStats;
    @Data
    public static class GoodsCommentStatBO {
        GoodsCommentStat commentStat;
        List<GoodsComment> comments;
    }
}
