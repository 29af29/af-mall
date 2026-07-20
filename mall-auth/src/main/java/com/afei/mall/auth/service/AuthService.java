package com.afei.mall.auth.service;

import com.afei.mall.auth.domain.dto.RegisterDTO;
import com.afei.mall.auth.domain.po.User;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AuthService extends IService<User> {
    /**
     * 注册
     * @param registerDTO
     */
    void register(RegisterDTO registerDTO);
}
