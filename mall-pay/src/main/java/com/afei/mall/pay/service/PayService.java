package com.afei.mall.pay.service;

import com.afei.mall.pay.domain.dto.PayCallbackDTO;
import com.afei.mall.pay.domain.dto.PayCreateDTO;
import com.afei.mall.pay.domain.po.PaymentInfo;
import com.afei.mall.pay.domain.vo.PayCreateVO;
import com.afei.mall.pay.domain.vo.PayStatusVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface PayService extends IService<PaymentInfo> {
    PayCreateVO pay(String token, PayCreateDTO dto);

    void callback(PayCallbackDTO dto);

    PayStatusVO status(String token, Long orderId);
}
