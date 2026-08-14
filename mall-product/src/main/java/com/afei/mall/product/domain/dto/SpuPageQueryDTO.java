package com.afei.mall.product.domain.dto;

import com.afei.common.base.BasePageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SpuPageQueryDTO extends BasePageQuery {

    private Long category3Id;  // 三级分类 ID

    private Long brandId;      // 品牌 ID

    private Boolean saleable;  // 是否上架（文档: saleable, Boolean）

    private String key;        // 关键词（文档: key）
}
