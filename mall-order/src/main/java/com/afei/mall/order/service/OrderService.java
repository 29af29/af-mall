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

    /**
     * 订单超时关闭（MQ 延迟消息 / 定时任务 兜底均可调用）
     * 幂等：仅 status=1 的订单会被关闭；状态机保护防并发重复回补
     */
    void timeoutClose(Long orderId);
}
