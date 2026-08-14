package com.afei.mall.notify.controller;

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
    public Result<PageResult<NotifyVO>> list(@RequestHeader("X-User-Id") Long userId,
                                             @RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(notifyService.page(userId, pageNum, pageSize));
    }

    @GetMapping("/unread")
    @Operation(summary = "未读通知数量")
    public Result<Long> unreadCount(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(notifyService.unreadCount(userId));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "标记单条已读")
    public Result<Void> markRead(@RequestHeader("X-User-Id") Long userId,
                                 @PathVariable Long id) {
        notifyService.markRead(userId, id);
        return Result.success();
    }

    @PutMapping("/read-all")
    @Operation(summary = "全部标记已读")
    public Result<Void> markAllRead(@RequestHeader("X-User-Id") Long userId) {
        notifyService.markAllRead(userId);
        return Result.success();
    }
}
