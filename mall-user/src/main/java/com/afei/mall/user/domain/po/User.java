package com.afei.mall.user.domain.po;

import com.afei.common.base.BaseEntity;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user")
public class User extends BaseEntity {

    private String username;

    private String password;

    private String nickname;

    private String phone;

    private String email;

    private String avatar;

    private Integer gender;  // 0=未知 1=男 2=女

    private Integer status;  // 0=禁用 1=正常

    private String role;    // USER=普通用户 ADMIN=管理员

    private LocalDateTime lastLoginTime;
}
