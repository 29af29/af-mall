package com.afei.mall.order.controller;

import com.afei.common.result.Result;
import com.afei.mall.order.domain.po.OrderInfo;
import com.afei.mall.order.mapper.OrderInfoMapper;
import com.afei.mall.order.service.OrderService;
import com.afei.mall.order.task.OrderTimeoutTask;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单管理后台接口（仅管理员）
 * <p>
 * 用于运维场景：手动清理积压的"待付款超时订单"，避免依赖单一 MQ 延迟消息。
 */
@RestController
@RequestMapping("/admin/order")
@Tag(name = "订单管理后台")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderInfoMapper orderInfoMapper;
    private final OrderService orderService;
    private final OrderTimeoutTask orderTimeoutTask;

    /**
     * 扫描超时未支付订单数量
     */
    @GetMapping("/timeout/count")
    @Operation(summary = "统计超时未支付订单数")
    public Result<Map<String, Object>> countExpired(@RequestParam(defaultValue = "30") Integer minutes) {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(minutes);
        Long count = orderInfoMapper.selectCount(
                Wrappers.<OrderInfo>lambdaQuery()
                        .eq(OrderInfo::getStatus, 1)
                        .lt(OrderInfo::getCreateTime, threshold));
        Map<String, Object> data = new HashMap<>();
        data.put("minutes", minutes);
        data.put("threshold", threshold);
        data.put("count", count);
        return Result.success(data);
    }

    /**
     * 手动触发超时关单清理
     */
    @PostMapping("/timeout/clear")
    @Operation(summary = "手动清理超时未支付订单")
    public Result<Map<String, Object>> clearExpired(@RequestParam(defaultValue = "30") Integer minutes) {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(minutes);
        List<OrderInfo> expired = orderInfoMapper.selectList(
                Wrappers.<OrderInfo>lambdaQuery()
                        .eq(OrderInfo::getStatus, 1)
                        .lt(OrderInfo::getCreateTime, threshold)
                        .orderByAsc(OrderInfo::getCreateTime)
                        .last("LIMIT 500"));
        int success = 0, failed = 0;
        for (OrderInfo order : expired) {
            try {
                orderService.timeoutClose(order.getId());
                success++;
            } catch (Exception e) {
                failed++;
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("scanned", expired.size());
        data.put("success", success);
        data.put("failed", failed);
        return Result.success(data);
    }

    /**
     * 直接调用定时任务方法（用于测试或紧急触发）
     */
    @PostMapping("/timeout/task")
    @Operation(summary = "触发 OrderTimeoutTask 兜底任务")
    public Result<Void> triggerTask() {
        orderTimeoutTask.scan();
        return Result.success();
    }
}