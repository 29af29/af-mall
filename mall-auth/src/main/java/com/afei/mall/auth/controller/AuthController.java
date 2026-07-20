package com.afei.mall.auth.controller;

import com.afei.common.result.Result;
import com.afei.mall.auth.domain.dto.RegisterDTO;
import com.afei.mall.auth.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证管理")
@Slf4j
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 注册
     * @param registerDTO
     * @return
     */
    @PostMapping("/register")
    public Result register(RegisterDTO registerDTO){
        log.info("注册请求: {}", registerDTO);
        authService.register(registerDTO);
        return Result.success();
    }
}
