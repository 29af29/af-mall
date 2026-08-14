package com.afei.mall.product.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SkuSaveDTO {

    @NotBlank(message = "SKU 标题不能为空")
    private String title;        // SKU 标题，如"12+256 雅丹黑"

    @NotNull(message = "价格不能为空")
    private Long price;          // 售价（单位：分）

    @NotNull(message = "库存不能为空")
    private Integer stock;       // 库存数量

    private String images;       // SKU 图片 URL

    private String ownSpec;      // 自有规格 JSON，如 {"颜色":"雅丹黑"}
}
