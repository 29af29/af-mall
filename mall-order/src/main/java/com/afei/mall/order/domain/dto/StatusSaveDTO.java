package com.afei.mall.order.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusSaveDTO {

    @NotNull(message = "订单状态不能为空")
    @Min(value = 1, message = "状态值无效")
    @Max(value = 7, message = "状态值无效")
    private Integer status;  // 1=待付款 2=已付款 3=已发货 4=已签收 5=已取消 6=退款中 7=已退款
}
