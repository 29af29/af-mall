package com.afei.mall.pay.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付消息本地消息表：支付成功回调时与支付单更新同事务写入，由定时任务可靠投递到 MQ
 * status：0=待发送 1=已发送 2=发送失败（人工处理）
 */
@Data
@TableName("pay_message")
public class PayMessage {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String payNo;

    private String orderNo;

    private Integer status;

    private Integer retryCount;

    private String lastError;

    private LocalDateTime createTime;

    private LocalDateTime sendTime;
}
