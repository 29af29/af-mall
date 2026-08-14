package com.afei.mall.user.service;

import com.afei.mall.user.domain.dto.UserUpdateDTO;
import com.afei.mall.user.domain.po.User;
import com.afei.mall.user.domain.vo.UserVO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

public interface UserService extends IService<User> {

    /**
     * 获取用户信息
     */
    UserVO getUserInfo(Long userId);

    /**
     * 更新用户信息
     */
    UserVO updateUserInfo(Long userId, UserUpdateDTO dto);

    /**
     * 上传用户头像
     */
    String uploadAvatar(Long userId, MultipartFile file);

}
