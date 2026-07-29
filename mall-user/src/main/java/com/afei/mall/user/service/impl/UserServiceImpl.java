package com.afei.mall.user.service.impl;

import com.afei.common.exception.BusinessException;
import com.afei.common.jwt.JwtUtils;
import com.afei.common.util.AliOssUtil;
import com.afei.mall.user.domain.dto.UserUpdateDTO;
import com.afei.mall.user.domain.po.User;
import com.afei.mall.user.domain.vo.UserVO;
import com.afei.mall.user.mapper.UserMapper;
import com.afei.mall.user.service.UserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    private final JwtUtils jwtUtils;
    private final AliOssUtil aliOssUtil;

    @Override
    public UserVO getUserInfo(String token) {
        User user = getLoginUser(token);
        return toVO(user);
    }

    @Override
    public UserVO updateUserInfo(String token, UserUpdateDTO dto) {
        User user = getLoginUser(token);
        BeanUtils.copyProperties(dto, user);
        this.updateById(user);
        return toVO(user);
    }

    @Override
    public String uploadAvatar(String token, MultipartFile file) {
        String url = aliOssUtil.upload(file);
        User user = getLoginUser(token);
        user.setAvatar(url);
        this.updateById(user);
        return url;
    }



    private User getLoginUser(String token) {
        Long userId = jwtUtils.getUserId(token);
        if (userId == null) {
            throw new BusinessException("Token 无效");
        }
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    private UserVO toVO(User user) {
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .phone(user.getPhone())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .gender(user.getGender())
                .status(user.getStatus())
                .lastLoginTime(user.getLastLoginTime())
                .createTime(user.getCreateTime())
                .build();
    }
}
