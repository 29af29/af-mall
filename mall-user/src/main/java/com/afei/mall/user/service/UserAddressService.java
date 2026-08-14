package com.afei.mall.user.service;

import com.afei.mall.user.domain.dto.AddressSaveDTO;
import com.afei.mall.user.domain.po.UserAddress;
import com.afei.mall.user.domain.vo.UserAddressVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface UserAddressService extends IService<UserAddress> {

    List<UserAddressVO> getAddressList(Long userId);

    void addAddress(Long userId, AddressSaveDTO dto);

    void updateAddress(Long userId, Long id, AddressSaveDTO dto);

    void deleteAddress(Long userId, Long id);

    void setDefaultAddress(Long userId, Long id);
}
