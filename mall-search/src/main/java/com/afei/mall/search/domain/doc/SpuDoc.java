package com.afei.mall.search.domain.doc;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 商品搜索索引文档（对应 ES 索引 spu）
 * name/caption 使用 IK 中文分词：索引时 ik_max_word（细粒度），搜索时 ik_smart（粗粒度）
 */
@Data
@Document(indexName = "spu")
public class SpuDoc {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String name;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
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
