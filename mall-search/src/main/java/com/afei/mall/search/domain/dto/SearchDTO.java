package com.afei.mall.search.domain.dto;

import lombok.Data;

@Data
public class SearchDTO {

    /** 搜索关键词 */
    private String key;

    /** 品牌名过滤 */
    private String brandName;

    /** 价格区间（分） */
    private Long priceMin;
    private Long priceMax;

    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
