package com.afei.mall.notify.domain.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知记录（本地消息表）
 * 字段对齐数据库 notify_record 表：
 * id/type/target/title/content/status/is_read/retry_count/business_type/business_id/create_time/send_time/is_deleted
 */
@Data
@TableName("notify_record")
public class NotifyRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发送渠道：1=短信 2=邮件 3=站内信 */
    private Integer type;

    /** 通知目标：手机号/邮箱/用户ID */
    private String target;

    private String title;
    private String content;

    /** 状态：0=待发送 1=已发送 2=发送失败 */
    private Integer status;

    /** 0=未读 1=已读 */
    private Integer isRead;

    /** 重试次数 */
    private Integer retryCount;

    /** 业务类型（来自 NotifyMessage.type） */
    private String businessType;

    /** 业务ID（订单号等） */
    private String businessId;

    private LocalDateTime createTime;
    private LocalDateTime sendTime;
}