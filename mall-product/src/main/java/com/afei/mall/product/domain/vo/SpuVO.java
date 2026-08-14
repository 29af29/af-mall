package com.afei.mall.product.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpuVO {
    private Long id;             // SPU ID
    private String name;         // 商品名称，如"华为 Mate 60 Pro"
    private String caption;      // 副标题/卖点，如"卫星通话 麒麟芯片"
    private Long brandId;        // 品牌 ID
    private String brandName;    // 品牌名称，已关联查询
    private Long category3Id;    // 三级分类 ID（最末级）
    private String mainImage;    // 商品主图 URL（从 pics 取第一张）
    private String detail;       // 商品详情（富文本 HTML）
    private Boolean saleable;    // 是否上架：true=上架 false=下架
    private List<SkuVO> skus;    // 关联的 SKU 列表
}
