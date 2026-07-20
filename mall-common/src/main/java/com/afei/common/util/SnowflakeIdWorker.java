package com.afei.common.util;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.lang.Snowflake;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SnowflakeIdWorker {
    private final Snowflake snowflake;

    public SnowflakeIdWorker(@Value("${snowflake.worker-id:1}") long workerId,
                             @Value("${snowflake.datacenter-id:1}") long datacenterId) {
        this.snowflake = IdUtil.getSnowflake(workerId, datacenterId);
    }

    public long nextId() {
        return snowflake.nextId();
    }

    public String nextIdStr() {
        return String.valueOf(snowflake.nextId());
    }

    /**
     * 生成订单号：前缀 + 雪花ID
     */
    public String generateOrderNo() {
        return "OM" + nextIdStr();
    }

    /**
     * 生成支付单号
     */
    public String generatePayNo() {
        return "PAY" + nextIdStr();
    }
}
