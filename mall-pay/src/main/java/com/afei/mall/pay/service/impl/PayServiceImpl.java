package com.afei.mall.pay.service.impl;

import com.afei.common.exception.BusinessException;
import com.afei.common.feign.OrderFeignClient;
import com.afei.common.feign.dto.OrderInfoDTO;
import com.afei.common.result.Result;
import com.afei.mall.pay.domain.dto.PayCallbackDTO;
import com.afei.mall.pay.domain.dto.PayCreateDTO;
import com.afei.mall.pay.domain.po.PayMessage;
import com.afei.mall.pay.domain.po.PaymentInfo;
import com.afei.mall.pay.domain.vo.PayCreateVO;
import com.afei.mall.pay.domain.vo.PayStatusVO;
import com.afei.mall.pay.mapper.PayMessageMapper;
import com.afei.mall.pay.mapper.PaymentInfoMapper;
import com.afei.mall.pay.service.PayService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PayService {

    private final OrderFeignClient orderFeignClient;
    private final PayMessageMapper payMessageMapper;

    @Value("${pay.callback-secret:afei-mall-callback-secret-2024}")
    private String callbackSecret;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayCreateVO pay(Long userId, PayCreateDTO dto) {
        // 1. 调 order 模块获取订单信息
        OrderInfoDTO order;
        try {
            Result<OrderInfoDTO> result = orderFeignClient.orderDetail(dto.getOrderId(), userId);
            if (result == null) {
                throw new BusinessException("订单服务调用失败");
            }
            if (result.getCode() != 200 || result.getData() == null) {
                // 透传真实错误信息（如"无权查看该订单"），避免统一掩盖成"订单不存在"
                throw new BusinessException(result.getMessage() != null ? result.getMessage() : "订单不存在");
            }
            order = result.getData();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用订单服务异常", e);
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
        // 1. 验签：防止伪造回调，签名 = HMAC-SHA256(secret, payNo|tradeNo|status)
        String expectSign = sign(dto.getPayNo(), dto.getTradeNo(), dto.getStatus());
        if (!MessageDigest.isEqual(expectSign.getBytes(StandardCharsets.UTF_8), dto.getSign().getBytes(StandardCharsets.UTF_8))) {
            log.warn("支付回调验签失败: payNo={}", dto.getPayNo());
            throw new BusinessException("回调签名校验失败");
        }

        // 2. 根据 payNo 查支付记录
        PaymentInfo payment = lambdaQuery().eq(PaymentInfo::getTransactionId, dto.getPayNo()).one();
        if (payment == null) {
            throw new BusinessException("支付记录不存在");
        }
        if ("SUCCESS".equals(payment.getTradeState())) {
            return; // 幂等，已处理过
        }

        // 3. 更新支付记录；同一事务内写入本地消息表（pay_message），保证支付成功事件不丢
        payment.setTradeState(dto.getStatus());
        payment.setPaymentTime(LocalDateTime.now());
        payment.setUpdateTime(LocalDateTime.now());
        payment.setCallbackContent("{\"tradeNo\":\"" + dto.getTradeNo() + "\",\"sign\":\"" + dto.getSign() + "\"}");
        updateById(payment);

        if ("SUCCESS".equals(dto.getStatus())) {
            PayMessage message = new PayMessage();
            message.setPayNo(payment.getTransactionId());
            message.setOrderNo(payment.getOrderNo());
            message.setStatus(0);
            message.setRetryCount(0);
            message.setCreateTime(LocalDateTime.now());
            payMessageMapper.insert(message);
            log.info("支付成功消息已写入本地消息表: orderNo={}", payment.getOrderNo());
        }
    }

    /**
     * 模拟支付网关回调（演示用）：自动生成合法签名后走真实回调链路
     * 生产环境由真实支付平台回调（同样验签），本接口应下线或仅内网可用
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mockPay(String payNo) {
        PayCallbackDTO dto = new PayCallbackDTO();
        dto.setPayNo(payNo);
        dto.setTradeNo("MOCK" + System.currentTimeMillis());
        dto.setStatus("SUCCESS");
        dto.setSign(sign(dto.getPayNo(), dto.getTradeNo(), dto.getStatus()));
        callback(dto);
        log.info("模拟支付成功: payNo={}", payNo);
    }

    /**
     * 回调签名计算：HMAC-SHA256(secret, payNo|tradeNo|status)
     */
    private String sign(String payNo, String tradeNo, String status) {
        try {
            String raw = payNo + "|" + tradeNo + "|" + status;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(callbackSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("回调签名计算失败", e);
            throw new BusinessException("回调签名计算失败");
        }
    }

    @Override
    public PayStatusVO status(Long userId, Long orderId) {
        // 1. 调 order 模块获取订单号
        OrderInfoDTO order;
        try {
            Result<OrderInfoDTO> result = orderFeignClient.orderDetail(orderId, userId);
            if (result == null) {
                throw new BusinessException("订单服务调用失败");
            }
            if (result.getCode() != 200 || result.getData() == null) {
                throw new BusinessException(result.getMessage() != null ? result.getMessage() : "订单不存在");
            }
            order = result.getData();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用订单服务异常", e);
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
