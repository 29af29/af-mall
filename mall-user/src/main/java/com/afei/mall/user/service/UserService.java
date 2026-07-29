package com.afei.mall.user.service;

import com.afei.mall.user.domain.dto.UserUpdateDTO;
import com.afei.mall.user.domain.po.User;
import com.afei.mall.user.domain.vo.UserVO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

public interface UserService extends IService<User> {

    /**
     * 获取用户信息
     * @param token
     * @return
     */
    UserVO getUserInfo(String token);

    /**
     * 更新用户信息
     * @param token
     * @param dto
     * @return
     */
    UserVO updateUserInfo(String token, UserUpdateDTO dto);

    /**
     * 上传用户头像
     * @param token
     * @param file
     * @return
     */
    String uploadAvatar(String token, MultipartFile file);

}
