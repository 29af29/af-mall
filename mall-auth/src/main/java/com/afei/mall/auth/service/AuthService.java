package com.afei.mall.auth.service;

import com.afei.mall.auth.domain.dto.LoginDTO;
import com.afei.mall.auth.domain.dto.RegisterDTO;
import com.afei.mall.auth.domain.po.User;
import com.afei.mall.auth.domain.vo.LoginVO;
import com.afei.mall.auth.domain.vo.UserVO;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AuthService extends IService<User> {
    /**
     * 注册用户
     * @param registerDTO
     */
    void register(RegisterDTO registerDTO);

    /**
     * 登录用户
     * @param loginDTO
     * @return
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 退出登录：Token 加入 Redis 黑名单
     */
    void logout(String token);

    /**
     * 用 refreshToken 换取新的 access token
     */
    LoginVO refreshToken(String refreshToken);

    /**
     * 获取当前登录用户信息
     */
    UserVO getUserInfo(String token);
}
