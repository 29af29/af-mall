package com.afei.mall.order.service;

import com.afei.common.result.PageResult;
import com.afei.mall.order.domain.dto.OrderCreateDTO;
import com.afei.mall.order.domain.dto.OrderPageQueryDTO;
import com.afei.mall.order.domain.dto.StatusSaveDTO;
import com.afei.mall.order.domain.vo.OrderCreateVO;
import com.afei.mall.order.domain.vo.OrderDetailVO;
import com.afei.mall.order.domain.vo.OrderPageVO;
import jakarta.validation.Valid;

public interface OrderService {
    OrderCreateVO createOrder(Long userId, OrderCreateDTO orderCreateDTO);

    PageResult<OrderPageVO> orderPage(Long userId, @Valid OrderPageQueryDTO orderPageQueryDTO);

    OrderDetailVO orderDetail(Long userId, Long id);

    void cancelOrder(Long userId, Long id);

    void updateStatus(Long id, StatusSaveDTO dto);
    
    void updateStatusByOrderNo(String orderNo, StatusSaveDTO dto);
}
