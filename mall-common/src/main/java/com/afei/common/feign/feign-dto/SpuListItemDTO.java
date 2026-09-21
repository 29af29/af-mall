package com.afei.common.feign.dto;

import lombok.Data;

/**
 * 跨服务使用的 SPU 简略信息（用于 ES 同步 / 跨服务传输）
 * 不引用 mall-product 模块，避免循环依赖
 */
@Data
public class SpuListItemDTO {
    private Long id;
    private String name;
    private String caption;
    private String brandName;
    private Long minPrice;     // 最低 SKU 价格（分）
    private String mainImage;
    private Boolean saleable;
}
