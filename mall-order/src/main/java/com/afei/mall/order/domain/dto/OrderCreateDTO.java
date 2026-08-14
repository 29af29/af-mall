package com.afei.mall.order.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateDTO {

    @NotNull(message = "订单商品不能为空")
    private List<OrderItemDTO> orderItems;       // 下单商品列表

    @NotBlank(message = "收货人不能为空")
    private String receiverName;                  // 收货人姓名

    @NotBlank(message = "收货手机号不能为空")
    private String receiverPhone;                 // 收货手机号

    @NotBlank(message = "收货地址不能为空")
    private String receiverAddress;               // 收货详细地址

    @NotNull(message = "总金额不能为空")
    private Long totalAmount;                     // 订单总金额（分）

    @NotNull(message = "实付金额不能为空")
    private Long payAmount;                       // 实付金额（分）

    private String remark;                        // 订单备注

    @Data
    public static class OrderItemDTO {
        @NotNull(message = "SKU ID 不能为空")
        private Long skuId;                   // SKU ID

        @NotNull(message = "数量不能为空")
        @Min(value = 1, message = "数量至少为 1")
        private Integer num;                  // 购买数量
    }
}
