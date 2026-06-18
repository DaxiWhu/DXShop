package com.daxi.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.daxi.domain.entity.MqDeadLetterMessage;
import com.daxi.mapper.order.MqDeadLetterMessageMapper;
import com.daxi.result.Result;
import com.daxi.service.MqAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MQ死信消息管理接口
 * 用于查看和处理死信消息
 */
@Slf4j
@RestController
@RequestMapping("/api/mq/dead-letter")
@RequiredArgsConstructor
public class MqDeadLetterController {
    
    private final MqDeadLetterMessageMapper deadLetterMessageMapper;
    private final MqAlertService mqAlertService;
    
    /**
     * 分页查询死信消息
     */
    @GetMapping("/page")
    public Result<Page<MqDeadLetterMessage>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String businessType,
            @RequestParam(required = false) Integer status) {
        
        Page<MqDeadLetterMessage> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<MqDeadLetterMessage> wrapper = new LambdaQueryWrapper<>();
        
        if (businessType != null && !businessType.isEmpty()) {
            wrapper.eq(MqDeadLetterMessage::getBusinessType, businessType);
        }
        
        if (status != null) {
            wrapper.eq(MqDeadLetterMessage::getStatus, status);
        }
        
        wrapper.orderByDesc(MqDeadLetterMessage::getCreateTime);
        
        Page<MqDeadLetterMessage> result = deadLetterMessageMapper.selectPage(page, wrapper);
        return Result.success(result);
    }
    
    /**
     * 获取未处理的死信消息数量
     */
    @GetMapping("/unprocessed-count")
    public Result<Long> getUnprocessedCount() {
        long count = mqAlertService.getUnprocessedCount();
        return Result.success(count);
    }
    
    /**
     * 根据ID查询死信消息详情
     */
    @GetMapping("/{id}")
    public Result<MqDeadLetterMessage> getById(@PathVariable Long id) {
        MqDeadLetterMessage message = deadLetterMessageMapper.selectById(id);
        if (message == null) {
            return Result.fail("记录不存在");
        }
        return Result.success(message);
    }
    
    /**
     * 标记为已处理
     */
    @PostMapping("/handle/{id}")
    public Result<Void> handle(
            @PathVariable Long id,
            @RequestParam String handler,
            @RequestParam(required = false) String remark) {
        
        MqDeadLetterMessage message = deadLetterMessageMapper.selectById(id);
        if (message == null) {
            return Result.fail("记录不存在");
        }
        
        message.setStatus(1);
        message.setHandler(handler);
        message.setHandleTime(LocalDateTime.now());
        message.setHandleRemark(remark);
        message.setUpdateTime(LocalDateTime.now());
        
        deadLetterMessageMapper.updateById(message);
        log.info("死信消息已标记为处理, id: {}, handler: {}", id, handler);
        
        return Result.success();
    }
    
    /**
     * 忽略死信消息
     */
    @PostMapping("/ignore/{id}")
    public Result<Void> ignore(
            @PathVariable Long id,
            @RequestParam String handler,
            @RequestParam(required = false) String remark) {
        
        MqDeadLetterMessage message = deadLetterMessageMapper.selectById(id);
        if (message == null) {
            return Result.fail("记录不存在");
        }
        
        message.setStatus(2);
        message.setHandler(handler);
        message.setHandleTime(LocalDateTime.now());
        message.setHandleRemark(remark);
        message.setUpdateTime(LocalDateTime.now());
        
        deadLetterMessageMapper.updateById(message);
        log.info("死信消息已忽略, id: {}, handler: {}", id, handler);
        
        return Result.success();
    }
    
    /**
     * 批量删除已处理的死信消息
     */
    @DeleteMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.fail("ID列表不能为空");
        }
        
        // 只允许删除已处理或已忽略的消息
        LambdaQueryWrapper<MqDeadLetterMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(MqDeadLetterMessage::getId, ids)
               .in(MqDeadLetterMessage::getStatus, 1, 2);
        
        List<MqDeadLetterMessage> messages = deadLetterMessageMapper.selectList(wrapper);
        if (messages.size() != ids.size()) {
            return Result.fail("存在未处理的消息，无法删除");
        }
        
        deadLetterMessageMapper.deleteByIds(ids);
        log.info("批量删除死信消息成功, 数量: {}", ids.size());
        
        return Result.success();
    }
}
