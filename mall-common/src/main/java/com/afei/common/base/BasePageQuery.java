package com.afei.common.base;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class BasePageQuery {
    @Min(1)
    private Integer pageNum = 1;
    @Min(1) @Max(100)
    private Integer pageSize = 10;

    public <T> Page<T> toPage() {
        return new Page<>(pageNum, pageSize);
    }
}
