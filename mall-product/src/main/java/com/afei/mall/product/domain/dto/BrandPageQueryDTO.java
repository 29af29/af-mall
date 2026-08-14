package com.afei.mall.product.domain.dto;

import com.afei.common.base.BasePageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BrandPageQueryDTO extends BasePageQuery {

    private String name;  // 品牌名模糊查询
}