package com.afei.mall.user.service.impl;

import com.afei.common.exception.BusinessException;
import com.afei.mall.user.domain.dto.AddressSaveDTO;
import com.afei.mall.user.domain.po.UserAddress;
import com.afei.mall.user.domain.vo.UserAddressVO;
import com.afei.mall.user.mapper.UserAddressMapper;
import com.afei.mall.user.service.UserAddressService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress> implements UserAddressService {

    @Override
    public List<UserAddressVO> getAddressList(Long userId) {
        List<UserAddress> addressList = this.lambdaQuery()
                .eq(UserAddress::getUserId, userId)
                .list();
        return addressList.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public void addAddress(Long userId, AddressSaveDTO dto) {
        clearDefaultIfNeeded(userId, dto.getIsDefault());
        UserAddress address = new UserAddress();
        BeanUtils.copyProperties(dto, address);
        address.setUserId(userId);
        this.save(address);
    }

    @Override
    public void updateAddress(Long userId, Long id, AddressSaveDTO dto) {
        UserAddress address = this.getById(id);
        if (address == null) {
            throw new BusinessException("地址不存在");
        }
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该地址");
        }
        clearDefaultIfNeeded(userId, dto.getIsDefault());
        BeanUtils.copyProperties(dto, address);
        this.updateById(address);
    }

    @Override
    public void deleteAddress(Long userId, Long id) {
        UserAddress address = this.getById(id);
        if (address == null) {
            throw new BusinessException("地址不存在");
        }
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该地址");
        }
        this.removeById(id);
    }

    @Override
    public void setDefaultAddress(Long userId, Long id) {
        UserAddress address = this.getById(id);
        if (address == null) {
            throw new BusinessException("地址不存在");
        }
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该地址");
        }
        // 先清掉所有默认地址，再把当前地址设为默认
        this.update(new LambdaUpdateWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .set(UserAddress::getIsDefault, 0));
        address.setIsDefault(1);
        this.updateById(address);
    }

    private void clearDefaultIfNeeded(Long userId, Integer isDefault) {
        if (isDefault != null && isDefault == 1) {
            this.update(new LambdaUpdateWrapper<UserAddress>()
                    .eq(UserAddress::getUserId, userId)
                    .set(UserAddress::getIsDefault, 0));
        }
    }

    private UserAddressVO toVO(UserAddress address) {
        return UserAddressVO.builder()
                .id(address.getId())
                .userId(address.getUserId())
                .receiverName(address.getReceiverName())
                .receiverPhone(address.getReceiverPhone())
                .province(address.getProvince())
                .city(address.getCity())
                .district(address.getDistrict())
                .detailAddress(address.getDetailAddress())
                .isDefault(address.getIsDefault())
                .createTime(address.getCreateTime())
                .build();
    }
}
