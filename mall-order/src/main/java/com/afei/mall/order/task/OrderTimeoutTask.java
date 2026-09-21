package com.afei.mall.order.task;

import com.afei.mall.order.domain.po.OrderInfo;
import com.afei.mall.order.mapper.OrderInfoMapper;
import com.afei.mall.order.service.OrderService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单超时兜底任务：每分钟扫描 status=1 且 create_time < now - 30 分钟 的订单执行关单
 * <p>
 * 解决 MQ 延迟消息不可靠的问题（容器重启、插件 bug、消息丢失等）：
 * - MQ 延迟消息：正常情况下 30 分钟触发（主链路）
 * - 本定时任务：兜底扫描，最坏延迟 60 秒
 * <p>
 * 幂等保护：service.timeoutClose 内部用 status=1 → 5 条件更新 CAS，并发安全
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

    /** 订单超时阈值：30 分钟（与 MQ 延迟消息保持一致） */
    private static final int TIMEOUT_MINUTES = 30;

    /** 单次扫描上限，防止大量历史订单一次性触发回补风暴 */
    private static final int SCAN_LIMIT = 100;

    private final OrderInfoMapper orderInfoMapper;
    private final OrderService orderService;

    @Scheduled(fixedDelay = 60000)
    public void scan() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(TIMEOUT_MINUTES);
        List<OrderInfo> expired = orderInfoMapper.selectList(
                Wrappers.<OrderInfo>lambdaQuery()
                        .eq(OrderInfo::getStatus, 1)
                        .lt(OrderInfo::getCreateTime, threshold)
                        .orderByAsc(OrderInfo::getCreateTime)
                        .last("LIMIT " + SCAN_LIMIT));

        if (expired.isEmpty()) {
            return;
        }
        log.info("扫描到 {} 个超时未支付订单，开始关单", expired.size());
        int success = 0, skipped = 0, failed = 0;
        for (OrderInfo order : expired) {
            try {
                orderService.timeoutClose(order.getId());
                success++;
            } catch (Exception e) {
                failed++;
                log.error("定时任务关单失败: orderId={}, orderNo={}", order.getId(), order.getOrderNo(), e);
            }
        }
        log.info("订单超时兜底任务完成: 扫描={}, 关闭={}, 跳过={}, 失败={}",
                expired.size(), success, skipped, failed);
    }
}