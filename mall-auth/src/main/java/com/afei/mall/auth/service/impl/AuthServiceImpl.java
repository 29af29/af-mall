package com.afei.mall.auth.service.impl;

import com.afei.mall.auth.domain.dto.RegisterDTO;
import com.afei.mall.auth.domain.po.User;
import com.afei.mall.auth.mapper.AuthMapper;
import com.afei.mall.auth.service.AuthService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

public class AuthServiceImpl extends ServiceImpl<AuthMapper, User> implements AuthService {
    /**
     * 注册
     * @param registerDTO
     */
    @Override
    public void register(RegisterDTO registerDTO) {

    }
}
