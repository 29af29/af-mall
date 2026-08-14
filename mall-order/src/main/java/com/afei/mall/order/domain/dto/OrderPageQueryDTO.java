package com.afei.mall.order.domain.dto;

import com.afei.common.base.BasePageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderPageQueryDTO extends BasePageQuery {

    private Integer status;  // 订单状态：1=待付款 2=已付款 3=已发货 4=已签收 5=已关闭
}
