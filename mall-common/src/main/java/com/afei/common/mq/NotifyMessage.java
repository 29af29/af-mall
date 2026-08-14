package com.afei.common.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 站内信通知消息：业务事件发生时发给 notify 模块
 * 消费端写入 notify_record 本地消息表（保证可靠投递）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotifyMessage implements Serializable {

    /** 业务类型：1=下单 2=支付 3=发货 4=系统 */
    public static final Integer TYPE_ORDER = 1;
    public static final Integer TYPE_PAY = 2;
    public static final Integer TYPE_DELIVERY = 3;
    public static final Integer TYPE_SYSTEM = 4;

    private Long userId;       // 接收人（站内信 target）
    private String title;      // 标题
    private String content;    // 内容
    private Integer type;      // 业务类型（写入 notify_record.business_type）
    private String businessId; // 业务ID（订单号等，写入 notify_record.business_id）
}