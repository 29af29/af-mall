package com.afei.mall.search.service;

import com.afei.common.result.PageResult;
import com.afei.mall.search.domain.dto.SearchDTO;
import com.afei.mall.search.domain.vo.SearchVO;

import java.util.List;

public interface SpuSearchService {

    PageResult<SearchVO> search(SearchDTO dto);

    /**
     * 搜索建议（自动补全）：返回以 prefix 开头的商品名列表
     */
    List<String> suggest(String prefix);
}
