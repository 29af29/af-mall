package com.afei.mall.cart.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartSelectDTO {
    @NotNull(message = "选中状态不能为空")
    private Boolean selected;
}
