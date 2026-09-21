package com.afei.mall.order.task;

import com.afei.mall.order.service.StockRestoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 库存回补补偿任务：每 30 秒扫描待回补记录（order_stock_retry，status=0）重试
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockRestoreRetryTask {

    private final StockRestoreService stockRestoreService;

    @Scheduled(fixedDelay = 30000)
    public void retry() {
        try {
            stockRestoreService.retryPending();
        } catch (Exception e) {
            log.error("库存回补补偿任务执行异常", e);
        }
    }
}
