package com.afei.mall.pay.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayCreateVO {
    private String payNo;   // 支付流水号
    private String payUrl;  // 支付链接（假支付）
    private Long amount;    // 支付金额（分）
}
