package com.afei.mall.search.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import com.afei.common.result.PageResult;
import com.afei.mall.search.domain.doc.SpuDoc;
import com.afei.mall.search.domain.dto.SearchDTO;
import com.afei.mall.search.domain.vo.SearchVO;
import com.afei.mall.search.service.SpuSearchService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SpuSearchServiceImpl implements SpuSearchService {

    private final ElasticsearchTemplate elasticsearchTemplate;

    public SpuSearchServiceImpl(ElasticsearchTemplate elasticsearchTemplate) {
        this.elasticsearchTemplate = elasticsearchTemplate;
    }

    @Override
    public PageResult<SearchVO> search(SearchDTO dto) {
        NativeQuery query = buildQuery(dto);
        SearchHits<SpuDoc> hits = elasticsearchTemplate.search(query, SpuDoc.class);

        List<SearchVO> records = hits.getSearchHits().stream()
                .map(hit -> {
                    SpuDoc doc = hit.getContent();
                    SearchVO vo = new SearchVO();
                    vo.setId(doc.getId());
                    vo.setName(doc.getName());
                    vo.setCaption(doc.getCaption());
                    vo.setBrandName(doc.getBrandName());
                    vo.setPrice(doc.getPrice());
                    vo.setImage(doc.getImage());
                    vo.setSaleable(doc.getSaleable());
                    return vo;
                })
                .collect(Collectors.toList());

        return new PageResult<>(records, hits.getTotalHits(), dto.getPageNum(), dto.getPageSize());
    }

    @Override
    public List<String> suggest(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return Collections.emptyList();
        }
        NativeQuery query = NativeQuery.builder()
                .withQuery(QueryBuilders.matchPhrasePrefix(m -> m
                        .field("name").query(prefix)))
                .withPageable(PageRequest.of(0, 10))
                .build();
        SearchHits<SpuDoc> hits = elasticsearchTemplate.search(query, SpuDoc.class);
        return hits.getSearchHits().stream()
                .map(hit -> hit.getContent().getName())
                .distinct()
                .limit(10)
                .collect(Collectors.toList());
    }

    private NativeQuery buildQuery(SearchDTO dto) {
        BoolQuery.Builder bool = QueryBuilders.bool();

        // 只搜上架商品
        bool.filter(QueryBuilders.term(t -> t.field("saleable").value(true)));

        // 关键词：匹配 name 和 caption
        if (StringUtils.hasText(dto.getKey())) {
            bool.must(QueryBuilders.multiMatch(m -> m
                    .fields("name", "caption")
                    .query(dto.getKey())));
        }

        // 品牌过滤
        if (StringUtils.hasText(dto.getBrandName())) {
            bool.filter(QueryBuilders.term(t -> t.field("brandName").value(dto.getBrandName())));
        }

        // 价格区间
        if (dto.getPriceMin() != null && dto.getPriceMax() != null) {
            bool.filter(QueryBuilders.range(r -> r
                    .number(n -> n.field("price")
                            .gte(dto.getPriceMin().doubleValue())
                            .lte(dto.getPriceMax().doubleValue()))));
        } else if (dto.getPriceMin() != null) {
            bool.filter(QueryBuilders.range(r -> r
                    .number(n -> n.field("price")
                            .gte(dto.getPriceMin().doubleValue()))));
        } else if (dto.getPriceMax() != null) {
            bool.filter(QueryBuilders.range(r -> r
                    .number(n -> n.field("price")
                            .lte(dto.getPriceMax().doubleValue()))));
        }

        Query query = bool.build()._toQuery();

        return NativeQuery.builder()
                .withQuery(query)
                .withPageable(PageRequest.of(dto.getPageNum() - 1, dto.getPageSize()))
                .build();
    }
}
