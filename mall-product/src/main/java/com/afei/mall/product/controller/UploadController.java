package com.afei.mall.product.controller;

import com.afei.common.exception.BusinessException;
import com.afei.common.result.Result;
import com.afei.common.util.AliOssUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
@Tag(name = "文件上传")
@AllArgsConstructor
public class UploadController {

    private final AliOssUtil aliOssUtil;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "图片上传到 OSS")
    public Result<Map<String, String>> upload(@RequestHeader("Authorization") String authHeader,
                                              @RequestPart("file") MultipartFile file) {
        extractToken(authHeader);
        if (file.isEmpty()) {
            throw new BusinessException("请选择要上传的文件");
        }
        String url = aliOssUtil.upload(file);
        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        return Result.success(data);
    }

    private void extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException("未登录");
        }
    }
}
