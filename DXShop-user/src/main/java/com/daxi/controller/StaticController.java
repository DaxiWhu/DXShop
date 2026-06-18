package com.daxi.controller;

import com.daxi.result.Result;
import com.daxi.service.StaticService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order-data")
@RequiredArgsConstructor
public class StaticController {
    private final @NonNull  StaticService staticService;
    @DeleteMapping("/{orderId}")
    public Result<Void> invalidateOrderData(@PathVariable Long orderId) {
        staticService.invalidateOrderData(orderId);
        return Result.success();
    }
}
