package com.afei.mall.product.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkuVO {
    private Long id;         // SKU ID
    private Long spuId;      // 所属 SPU ID
    private String title;    // SKU 标题，如"Mate 60 Pro 12+256 雅丹黑"
    private Long price;      // 售价（单位：分）
    private Integer stock;   // 库存数量
    private String images;   // SKU 图片 URL
    private String ownSpec;  // 自有规格 JSON，如 {"颜色":"雅丹黑","内存":"12+256"}
    private Boolean enable;  // 是否启用：true=启用 false=禁用
}
