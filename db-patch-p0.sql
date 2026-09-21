-- ============================================================
-- 阿飞商城 · P0 修复配套建表脚本（幂等，可重复执行）
-- 执行库：afei_order / afei_pay
-- 说明：
--   1. order_stock_retry：库存回补本地消息表
--      取消订单/超时关单回补库存失败时登记，定时任务（StockRestoreRetryTask）补偿重试
--   2. pay_message：支付成功消息本地消息表
--      支付回调与支付单更新同事务写入，定时任务（PayMessageSendTask）可靠投递 MQ
-- ============================================================

USE afei_order;

CREATE TABLE IF NOT EXISTS `order_stock_retry` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `sku_id` BIGINT NOT NULL COMMENT 'SKU ID',
  `num` INT NOT NULL DEFAULT 0 COMMENT '回补数量',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0=待回补 1=已回补 2=回补失败(人工处理)',
  `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
  `last_error` VARCHAR(500) DEFAULT NULL COMMENT '最近一次错误信息',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `send_time` DATETIME DEFAULT NULL COMMENT '回补成功时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  UNIQUE KEY `uk_order_sku` (`order_id`, `sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存回补本地消息表';

USE afei_pay;

CREATE TABLE IF NOT EXISTS `pay_message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `pay_no` VARCHAR(64) NOT NULL COMMENT '支付流水号',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0=待发送 1=已发送 2=发送失败(人工处理)',
  `retry_count` INT NOT NULL DEFAULT 0 COMMENT '重试次数',
  `last_error` VARCHAR(500) DEFAULT NULL COMMENT '最近一次错误信息',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `send_time` DATETIME DEFAULT NULL COMMENT '发送成功时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付消息本地消息表';
