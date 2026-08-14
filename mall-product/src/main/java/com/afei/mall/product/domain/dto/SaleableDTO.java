package com.afei.mall.product.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SaleableDTO {

    @NotNull(message = "上架状态不能为空")
    private Boolean saleable;  // true=上架 false=下架
}