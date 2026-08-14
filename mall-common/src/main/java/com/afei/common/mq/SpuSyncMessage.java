package com.afei.common.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 商品同步消息：商品上下架/增删改时，product 发给 search 同步 ES
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpuSyncMessage implements Serializable {

    public static final String TYPE_ADD = "ADD";
    public static final String TYPE_UPDATE = "UPDATE";
    public static final String TYPE_DELETE = "DELETE";

    private Long spuId;          // SPU ID
    private String name;         // 商品名称
    private String caption;      // 副标题/卖点
    private String brandName;    // 品牌名
    private Long price;          // 最低价（分）
    private String image;        // 主图 URL
    private Boolean saleable;    // 是否上架
    private String type;         // ADD / UPDATE / DELETE
}
