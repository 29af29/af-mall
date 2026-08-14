package com.afei.mall.user.service.impl;

import com.afei.common.exception.BusinessException;
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

    private final AliOssUtil aliOssUtil;

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = getLoginUser(userId);
        return toVO(user);
    }

    @Override
    public UserVO updateUserInfo(Long userId, UserUpdateDTO dto) {
        User user = getLoginUser(userId);
        BeanUtils.copyProperties(dto, user);
        this.updateById(user);
        return toVO(user);
    }

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        String url = aliOssUtil.upload(file);
        User user = getLoginUser(userId);
        user.setAvatar(url);
        this.updateById(user);
        return url;
    }

    private User getLoginUser(Long userId) {
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
                .role(user.getRole())
                .lastLoginTime(user.getLastLoginTime())
                .createTime(user.getCreateTime())
                .build();
    }
}
