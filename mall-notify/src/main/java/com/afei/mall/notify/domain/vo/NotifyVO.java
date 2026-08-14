package com.afei.mall.notify.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotifyVO {
    private Long id;
    private Integer type;
    private String title;
    private String content;
    private Integer isRead;
    private String businessType;
    private String businessId;
    private LocalDateTime createTime;
}