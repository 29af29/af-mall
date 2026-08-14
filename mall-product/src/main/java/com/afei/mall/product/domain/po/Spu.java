package com.afei.mall.product.domain.po;

import com.afei.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("spu")
public class Spu extends BaseEntity {

    private String name;

    private String caption;

    private Long brandId;

    private Long category1Id;

    private Long category2Id;

    private Long category3Id;

    private String pics;

    private String description;

    private Integer saleable;  // 0=下架 1=上架
}
