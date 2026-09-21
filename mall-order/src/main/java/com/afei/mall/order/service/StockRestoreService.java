package com.afei.mall.order.service;

import com.afei.common.feign.ProductFeignClient;
import com.afei.common.result.Result;
import com.afei.mall.order.domain.po.OrderInfo;
import com.afei.mall.order.domain.po.OrderItem;
import com.afei.mall.order.domain.po.OrderStockRetry;
import com.afei.mall.order.mapper.OrderItemMapper;
import com.afei.mall.order.mapper.OrderStockRetryMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 库存回补服务：取消订单 / 超时关单时回补 SKU 库存
 * <p>
 * 回补失败写入 order_stock_retry 本地消息表（status=0），由 StockRestoreRetryTask 定时补偿重试，
 * 超过最大重试次数标记失败待人工处理，保证"扣了库存就一定还回来"。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockRestoreService {

    private static final int MAX_RETRY = 5;

    private final OrderItemMapper orderItemMapper;
    private final OrderStockRetryMapper orderStockRetryMapper;
    private final ProductFeignClient productFeignClient;

    /**
     * 回补订单所有 SKU 库存；单条失败立即登记待重试记录
     */
    public void restore(OrderInfo order) {
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        for (OrderItem item : items) {
            boolean ok = tryRestore(item.getSkuId(), item.getQuantity());
            if (!ok) {
                registerRetry(order.getId(), item.getSkuId(), item.getQuantity());
            }
        }
    }

    /**
     * 定时补偿：处理待回补记录（status=0 且未超重试上限）
     */
    public void retryPending() {
        List<OrderStockRetry> pending = orderStockRetryMapper.selectList(
                new LambdaQueryWrapper<OrderStockRetry>()
                        .eq(OrderStockRetry::getStatus, 0)
                        .lt(OrderStockRetry::getRetryCount, MAX_RETRY)
                        .last("LIMIT 20"));
        for (OrderStockRetry record : pending) {
            boolean ok = tryRestore(record.getSkuId(), record.getNum());
            record.setRetryCount(record.getRetryCount() + 1);
            if (ok) {
                record.setStatus(1);
                record.setSendTime(LocalDateTime.now());
                record.setLastError(null);
                log.info("库存回补补偿成功: orderId={}, skuId={}", record.getOrderId(), record.getSkuId());
            } else {
                record.setLastError("第" + record.getRetryCount() + "次回补失败");
                if (record.getRetryCount() >= MAX_RETRY) {
                    record.setStatus(2);
                    log.error("库存回补多次失败，待人工处理: orderId={}, skuId={}", record.getOrderId(), record.getSkuId());
                }
            }
            orderStockRetryMapper.updateById(record);
        }
    }

    /**
     * 单条 SKU 回补：调用商品服务内部接口，异常也视为失败（不抛出，避免影响关单主流程）
     */
    private boolean tryRestore(Long skuId, Integer num) {
        try {
            Result<Void> result = productFeignClient.restoreStock(skuId, Map.of("num", num));
            if (result != null && result.getCode() == 200) {
                log.info("回补库存成功: skuId={}, num={}", skuId, num);
                return true;
            }
            log.warn("回补库存失败: skuId={}, msg={}", skuId, result != null ? result.getMessage() : "null");
            return false;
        } catch (Exception e) {
            log.error("回补库存异常: skuId={}", skuId, e);
            return false;
        }
    }

    /**
     * 登记待重试记录（已存在则重置为待回补）
     */
    private void registerRetry(Long orderId, Long skuId, Integer num) {
        try {
            OrderStockRetry record = orderStockRetryMapper.selectOne(
                    new LambdaQueryWrapper<OrderStockRetry>()
                            .eq(OrderStockRetry::getOrderId, orderId)
                            .eq(OrderStockRetry::getSkuId, skuId));
            if (record == null) {
                record = new OrderStockRetry();
                record.setOrderId(orderId);
                record.setSkuId(skuId);
                record.setNum(num);
                record.setStatus(0);
                record.setRetryCount(0);
                record.setCreateTime(LocalDateTime.now());
                orderStockRetryMapper.insert(record);
            } else {
                record.setStatus(0);
                orderStockRetryMapper.updateById(record);
            }
            log.warn("回补库存登记重试: orderId={}, skuId={}", orderId, skuId);
        } catch (Exception e) {
            log.error("登记库存回补重试失败: orderId={}, skuId={}", orderId, skuId, e);
        }
    }
}
