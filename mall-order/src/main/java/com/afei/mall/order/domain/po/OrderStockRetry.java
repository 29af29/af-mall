package com.afei.mall.order.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存回补本地消息表：取消订单 / 超时关单回补库存失败时登记，由定时任务补偿重试，保证库存不丢
 * status：0=待回补 1=已回补 2=回补失败（人工处理）
 */
@Data
@TableName("order_stock_retry")
public class OrderStockRetry {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long skuId;

    private Integer num;

    private Integer status;

    private Integer retryCount;

    private String lastError;

    private LocalDateTime createTime;

    private LocalDateTime sendTime;
}
