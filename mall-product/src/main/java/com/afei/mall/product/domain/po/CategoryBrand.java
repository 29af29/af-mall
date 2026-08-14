package com.afei.mall.product.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("category_brand")
public class CategoryBrand {

    private Long categoryId;

    private Long brandId;
}
