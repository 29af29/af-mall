package com.afei.mall.pay.service.impl;

import com.afei.common.exception.BusinessException;
import com.afei.common.feign.OrderFeignClient;
import com.afei.common.feign.dto.OrderInfoDTO;
import com.afei.common.jwt.JwtUtils;
import com.afei.common.mq.MqConfig;
import com.afei.common.mq.OrderPaidMessage;
import com.afei.common.result.Result;
import com.afei.mall.pay.domain.dto.PayCallbackDTO;
import com.afei.mall.pay.domain.dto.PayCreateDTO;
import com.afei.mall.pay.domain.po.PaymentInfo;
import com.afei.mall.pay.domain.vo.PayCreateVO;
import com.afei.mall.pay.domain.vo.PayStatusVO;
import com.afei.mall.pay.mapper.PaymentInfoMapper;
import com.afei.mall.pay.service.PayService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class PayServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PayService {

    private final JwtUtils jwtUtils;
    private final OrderFeignClient orderFeignClient;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayCreateVO pay(String token, PayCreateDTO dto) {
        // 1. 调 order 模块获取订单信息
        OrderInfoDTO order;
        try {
            Result<OrderInfoDTO> result = orderFeignClient.orderDetail(dto.getOrderId(), "Bearer " + token);
            if (result == null || result.getCode() != 200 || result.getData() == null) {
                throw new BusinessException("订单不存在");
            }
            order = result.getData();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("获取订单信息失败");
        }

        // 2. 校验订单状态必须是待付款
        if (order.getStatus() != 1) {
            throw new BusinessException("订单状态不正确，无法支付");
        }

        // 3. 生成支付流水号并存入数据库
        String transactionId = "PAY" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 8);

        PaymentInfo payment = new PaymentInfo();
        payment.setOrderNo(order.getOrderNo());
        payment.setTransactionId(transactionId);
        payment.setPaymentType(dto.getPayType());
        payment.setTotalAmount(order.getPayAmount());
        payment.setTradeState("NOTPAY");
        payment.setCreateTime(LocalDateTime.now());
        save(payment);

        // 4. 返回
        return PayCreateVO.builder()
                .payNo(transactionId)
                .payUrl("/api/pay/mock?payNo=" + transactionId)
                .amount(order.getPayAmount())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void callback(PayCallbackDTO dto) {
        // 1. 根据 payNo 查支付记录
        PaymentInfo payment = lambdaQuery().eq(PaymentInfo::getTransactionId, dto.getPayNo()).one();
        if (payment == null) {
            throw new BusinessException("支付记录不存在");
        }
        if ("SUCCESS".equals(payment.getTradeState())) {
            return; // 幂等，已处理过
        }

        // 2. 更新支付记录
        payment.setTradeState(dto.getStatus());
        payment.setPaymentTime(LocalDateTime.now());
        payment.setCallbackContent(dto.getSign());
        updateById(payment);

        // 3. 发 MQ 消息通知订单模块
        if ("SUCCESS".equals(dto.getStatus())) {
            OrderPaidMessage msg = new OrderPaidMessage(payment.getOrderNo(), payment.getTransactionId());
            rabbitTemplate.convertAndSend(MqConfig.ORDER_PAID_QUEUE, msg);
            log.info("支付成功消息已发送: orderNo={}", payment.getOrderNo());
        }
    }

    @Override
    public PayStatusVO status(String token, Long orderId) {
        // 1. 调 order 模块获取订单号
        OrderInfoDTO order;
        try {
            Result<OrderInfoDTO> result = orderFeignClient.orderDetail(orderId, "Bearer " + token);
            if (result == null || result.getCode() != 200 || result.getData() == null) {
                throw new BusinessException("订单不存在");
            }
            order = result.getData();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("获取订单信息失败");
        }

        // 2. 查支付记录
        PaymentInfo payment = lambdaQuery().eq(PaymentInfo::getOrderNo, order.getOrderNo()).one();
        if (payment == null) {
            return PayStatusVO.builder()
                    .orderId(orderId)
                    .amount(order.getPayAmount())
                    .payStatus(1)
                    .payStatusText("未支付")
                    .build();
        }

        Integer payStatus = "SUCCESS".equals(payment.getTradeState()) ? 2 : 1;
        String text = "SUCCESS".equals(payment.getTradeState()) ? "已支付" : "未支付";

        return PayStatusVO.builder()
                .payNo(payment.getTransactionId())
                .orderId(orderId)
                .amount(payment.getTotalAmount())
                .payStatus(payStatus)
                .payStatusText(text)
                .payTime(payment.getPaymentTime())
                .build();
    }
}
