package com.afei.mall.product.domain.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpuSaveDTO {

    @Size(max = 100, message = "商品名称最长100个字符")
    private String name;            // 商品名称（新增时 service 层必填校验）

    @Size(max = 200, message = "副标题最长200个字符")
    private String caption;         // 副标题/卖点

    private Long brandId;           // 品牌 ID（新增时 service 层必填校验）

    private Long category3Id;       // 三级分类 ID（新增时 service 层必填校验）

    private String mainImage;       // 主图 URL

    private String detail;          // 商品详情（富文本 HTML）

    private List<SkuSaveDTO> skus;  // SKU 列表
}
