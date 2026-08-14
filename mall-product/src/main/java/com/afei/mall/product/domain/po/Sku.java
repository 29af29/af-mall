package com.afei.mall.product.domain.po;

import com.afei.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sku")
public class Sku extends BaseEntity {

    private Long spuId;

    private String name;

    private Long price;   // 单位:分

    private Integer stock;

    private String images;

    private String spec;  // 规格JSON {"颜色":"黑色","尺码":"XL"}

    private Integer status;  // 0=禁用 1=启用
}
