package com.afei.mall.auth.service.impl;

import com.afei.common.constant.RedisKey;
import com.afei.common.exception.BusinessException;
import com.afei.common.jwt.JwtUtils;
import com.afei.mall.auth.domain.dto.LoginDTO;
import com.afei.mall.auth.domain.dto.RegisterDTO;
import com.afei.mall.auth.domain.po.User;
import com.afei.mall.auth.domain.vo.LoginVO;
import com.afei.mall.auth.domain.vo.UserVO;
import com.afei.mall.auth.mapper.AuthMapper;
import com.afei.mall.auth.service.AuthService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class AuthServiceImpl extends ServiceImpl<AuthMapper, User> implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void register(RegisterDTO registerDTO) {
        Long count = this.count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, registerDTO.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        String encodedPassword = passwordEncoder.encode(registerDTO.getPassword());
        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);
        user.setPassword(encodedPassword);
        user.setRole("USER");  // 默认角色
        this.save(user);
    }

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        User user = this.baseMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, loginDTO.getUsername()));
        if (user == null) {
            throw new BusinessException("用户名不存在");
        }
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        user.setLastLoginTime(LocalDateTime.now());
        this.updateById(user);

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId(), user.getUsername(), user.getRole());
        return LoginVO.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .lastLoginTime(user.getLastLoginTime())
                .build();
    }

    @Override
    public void logout(String token) {
        // 计算 Token 剩余有效时间，加入 Redis 黑名单
        Claims claims = jwtUtils.parseToken(token);
        if (claims == null) {
            throw new BusinessException("Token 无效");
        }
        long ttl = claims.getExpiration().getTime() - System.currentTimeMillis();
        if (ttl > 0) {
            redisTemplate.opsForValue()
                    .set(RedisKey.TOKEN_BLACKLIST_PREFIX + token, "1", Duration.ofMillis(ttl));
        }
    }

    @Override
    public LoginVO refreshToken(String refreshToken) {
        Claims claims = jwtUtils.parseToken(refreshToken);
        if (claims == null) {
            throw new BusinessException("RefreshToken 无效或已过期");
        }
        Long userId = claims.get("userId", Long.class);
        String username = claims.get("username", String.class);
        String role = claims.get("role", String.class);

        if (userId == null || username == null) {
            throw new BusinessException("RefreshToken 内容不完整");
        }

        String newToken = jwtUtils.generateToken(userId, username, role);
        return LoginVO.builder()
                .token(newToken)
                .userId(userId)
                .username(username)
                .build();
    }

    @Override
    public UserVO getUserInfo(String token) {
        Long userId = jwtUtils.getUserId(token);
        if (userId == null) {
            throw new BusinessException("Token 无效");
        }
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
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
