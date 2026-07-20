package com.afei.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private List<T> records;
    private Long total;
    private Integer current;
    private Integer size;
    private Integer pages;

    public PageResult() {
    }

    public PageResult(List<T> records, Long total, Integer current, Integer size) {
        this.records = records;
        this.total = total;
        this.current = current;
        this.size = size;
        this.pages = (int) Math.ceil((double) total / size);
    }

    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(
                page.getRecords(),
                page.getTotal(),
                (int) page.getCurrent(),
                (int) page.getSize()
        );
    }
}
