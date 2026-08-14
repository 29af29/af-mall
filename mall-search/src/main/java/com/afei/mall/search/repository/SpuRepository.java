package com.afei.mall.search.repository;

import com.afei.mall.search.domain.doc.SpuDoc;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface SpuRepository extends ElasticsearchRepository<SpuDoc, Long> {
}
