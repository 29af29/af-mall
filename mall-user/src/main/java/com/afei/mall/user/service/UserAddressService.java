package com.afei.mall.user.service;

import com.afei.mall.user.domain.dto.AddressSaveDTO;
import com.afei.mall.user.domain.po.UserAddress;
import com.afei.mall.user.domain.vo.UserAddressVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface UserAddressService extends IService<UserAddress> {

    List<UserAddressVO> getAddressList(String token);

    void addAddress(String token, AddressSaveDTO dto);

    void updateAddress(String token, Long id, AddressSaveDTO dto);

    void deleteAddress(String token, Long id);

    void setDefaultAddress(String token, Long id);
}
