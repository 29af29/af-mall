package com.afei.mall.notify.controller;

import com.afei.common.exception.BusinessException;
import com.afei.common.result.PageResult;
import com.afei.common.result.Result;
import com.afei.mall.notify.domain.vo.NotifyVO;
import com.afei.mall.notify.service.NotifyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/notify")
@RestController
@Tag(name = "站内信通知接口")
@RequiredArgsConstructor
public class NotifyController {

    private final NotifyService notifyService;

    @GetMapping("/list")
    @Operation(summary = "查询我的通知列表")
    public Result<PageResult<NotifyVO>> list(@RequestHeader("authorization") String authorization,
                                             @RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(notifyService.page(extractToken(authorization), pageNum, pageSize));
    }

    @GetMapping("/unread")
    @Operation(summary = "未读通知数量")
    public Result<Long> unreadCount(@RequestHeader("authorization") String authorization) {
        return Result.success(notifyService.unreadCount(extractToken(authorization)));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记单条已读")
    public Result<Void> markRead(@RequestHeader("authorization") String authorization,
                                 @PathVariable Long id) {
        notifyService.markRead(extractToken(authorization), id);
        return Result.success();
    }

    @PutMapping("/read-all")
    @Operation(summary = "全部标记已读")
    public Result<Void> markAllRead(@RequestHeader("authorization") String authorization) {
        notifyService.markAllRead(extractToken(authorization));
        return Result.success();
    }

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BusinessException("未登录");
        }
        return authHeader.substring(7);
    }
}
