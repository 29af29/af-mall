package com.afei.mall.search.service;

import com.afei.common.feign.ProductFeignClient;
import com.afei.common.feign.dto.SpuListItemDTO;
import com.afei.common.result.PageResult;
import com.afei.mall.search.domain.doc.SpuDoc;
import com.afei.mall.search.repository.SpuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * ES 全量同步服务
 * 用法：商品数据异常时（如 SQL 直插、跨服务同步链断），管理员调用一次把 MySQL 全部 SPU 灌进 ES
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EsSyncService {

    private final ProductFeignClient productFeignClient;
    private final SpuRepository spuRepository;

    /** 每页大小，够大减少请求次数 */
    private static final int PAGE_SIZE = 200;

    /**
     * 把 MySQL 所有上架商品同步到 ES
     * @return 同步数量
     */
    public int syncAllFromDb() {
        log.info("[ES 全量同步] 开始...");
        long start = System.currentTimeMillis();

        List<SpuDoc> allDocs = new ArrayList<>();
        int pageNum = 1;
        int total = 0;

        while (true) {
            PageResult<SpuListItemDTO> page = productFeignClient
                    .spuPage(pageNum, PAGE_SIZE, true)
                    .getData();

            if (page == null || page.getRecords() == null || page.getRecords().isEmpty()) {
                break;
            }

            List<SpuDoc> docs = page.getRecords().stream()
                    .map(this::toDoc)
                    .toList();
            allDocs.addAll(docs);

            total += docs.size();
            log.info("[ES 全量同步] 第 {} 页，本页 {} 条，累计 {} 条", pageNum, docs.size(), total);

            if (total >= page.getTotal()) {
                break;
            }
            pageNum++;
        }

        if (allDocs.isEmpty()) {
            log.warn("[ES 全量同步] MySQL 无上架商品，跳过");
            return 0;
        }

        spuRepository.saveAll(allDocs);
        long ms = System.currentTimeMillis() - start;
        log.info("[ES 全量同步] 完成，共 {} 条，耗时 {} ms", total, ms);
        return total;
    }

    private SpuDoc toDoc(SpuListItemDTO src) {
        SpuDoc doc = new SpuDoc();
        doc.setId(src.getId());
        doc.setName(src.getName());
        doc.setCaption(src.getCaption());
        doc.setBrandName(src.getBrandName());
        doc.setPrice(src.getMinPrice());
        doc.setImage(src.getMainImage());
        doc.setSaleable(src.getSaleable());
        return doc;
    }
}
