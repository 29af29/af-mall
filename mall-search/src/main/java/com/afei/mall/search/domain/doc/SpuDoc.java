package com.afei.mall.search.domain.doc;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 商品搜索索引文档（对应 ES 索引 spu）
 * 说明：如需中文分词，安装 IK 插件后，将 name/caption 的 analyzer 改为 ik_max_word
 */
@Data
@Document(indexName = "spu")
public class SpuDoc {

    @Id
    private Long id;

    @Field(type = FieldType.Text)
    private String name;

    @Field(type = FieldType.Text)
    private String caption;

    @Field(type = FieldType.Keyword)
    private String brandName;

    @Field(type = FieldType.Long)
    private Long price;

    @Field(type = FieldType.Keyword, index = false)
    private String image;

    @Field(type = FieldType.Boolean)
    private Boolean saleable;
}
