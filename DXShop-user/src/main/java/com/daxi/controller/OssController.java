package com.daxi.controller;

import com.daxi.result.Result;
import com.daxi.domain.dto.OssPolicyDTO;
import com.daxi.util.OssUtil;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/oss")
@RequiredArgsConstructor
public class OssController {
    private final @NonNull OssUtil ossUtil;
    @PostMapping("/upload")
    Result<OssPolicyDTO> upload(String key) {
        return Result.success(ossUtil.getOssPolicy(key));
    }

}
