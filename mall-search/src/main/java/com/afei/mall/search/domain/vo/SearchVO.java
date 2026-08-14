package com.afei.mall.search.domain.vo;

import lombok.Data;

@Data
public class SearchVO {

    private Long id;
    private String name;
    private String caption;
    private String brandName;
    private Long price;
    private String image;
    private Boolean saleable;
}
