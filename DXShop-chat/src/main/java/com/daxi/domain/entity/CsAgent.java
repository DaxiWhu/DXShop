package com.daxi.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@TableName("cs_agent")
@Data
public class CsAgent {
    @TableId(type = IdType.INPUT)
    private Long id;
    private String username;
    private String passwordHash;
    private String nickname;
    private String avatarUrl;
    private Integer status;
    private LocalDateTime createTime;
}
